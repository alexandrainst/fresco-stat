package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.VectorUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Output ranks and correction term for Kruskall-Wallis if ties have been averaged.
 */
public class Ranks implements
    Computation<Pair<List<DRes<SFixed>>, Double>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> samples;
  private final boolean averageTies;

  public Ranks(List<List<DRes<SInt>>> samples) {
    this(samples, false);
  }

  public Ranks(List<List<DRes<SInt>>> samples, boolean averageTies) {
    this.samples = samples;
    this.averageTies = averageTies;
  }

  @Override
  public DRes<Pair<List<DRes<SFixed>>, Double>> buildComputation(
      ProtocolBuilderNumeric builder) {

    List<Pair<DRes<SInt>, List<DRes<SInt>>>> valuesWithClassIndictators = new ArrayList<>();

    // For each data point we add a vector indicating what group it belongs to
    int n = 0;
    for (int i = 0; i < samples.size(); i++) {
      List<DRes<SInt>> sample = samples.get(i);
      List<DRes<SInt>> indicators = new ArrayList<>();
      for (int j = 0; j < samples.size(); j++) {
        indicators.add(builder.numeric().known(i == j ? 1 : 0));
      }
      for (DRes<SInt> dataPoint : sample) {
        valuesWithClassIndictators.add(new Pair<>(dataPoint, indicators));
        n++;
      }
    }

    int finalN = n;
    return builder.seq(seq ->
        // Sort data points
        dk.alexandra.fresco.lib.common.collections.Collections.using(seq)
            .sort(valuesWithClassIndictators)
    ).seq((seq, sorted) -> {

      Collections.reverse(sorted);

      Pair<List<Pair<DRes<SInt>, List<DRes<SInt>>>>, DRes<List<Double>>> out;

      if (averageTies) {

        // Break ties by opening entrypted values. Note that this leaks the ranks of elements that are equal
        DRes<List<Double>> r1 = new LeakyBreakTies(
            sorted.stream().map(Pair::getFirst).collect(Collectors.toList())).buildComputation(seq);
        out = new Pair(sorted, r1);

      } else {

        // Use sequential ranking where ties are ignored
        List<Double> r1 = IntStream.rangeClosed(1, finalN).mapToObj(Double::valueOf)
            .collect(Collectors.toCollection(ArrayList::new));
        DRes<List<Double>> inferred = () -> r1;
        out = new Pair(sorted, inferred);

      }
      return () -> out;
    }).par((par, dataAndRanks) -> {

      List<Double> ranks = dataAndRanks.getSecond().out();
      double g = 0.0;

      if (averageTies) {

        // Compute correction factor. First find groups of equal elements and count the number of
        // elements in each group.
        List<Integer> tiedGroups = new ArrayList<>();
        int groups = 0;
        int i = 1;
        while (i < ranks.size()) {
          if (sameRank(ranks.get(i), ranks.get(i - 1))) {
            // New group
            int count = 1;

            // Continue until we are no longer in group
            while (sameRank(ranks.get(i), ranks.get(i - 1))) {
              count++;
              i++;
            }
            tiedGroups.add(count);
          }
          i++;
        }

        for (Integer t : tiedGroups) {
          g += Math.pow(t, 3) - t;
        }
        g = g / (Math.pow(finalN, 3) - finalN);
        g = 1.0 / (1.0 - g);
      } else {
        // No correction
        g = 1.0;
      }

      // Sum of ranks for each group
      List<DRes<SFixed>> totals = new ArrayList<>();
      for (int i = 0; i < samples.size(); i++) {
        List<DRes<SInt>> column = new ArrayList<>();
        for (Pair<DRes<SInt>, List<DRes<SInt>>> row : dataAndRanks.getFirst()) {
          column.add(row.getSecond().get(i));
        }
        totals.add(VectorUtils.innerProductWithBitvectorPublic(column, ranks, par));
      }
      return Pair.lazy(totals, g);
    });
  }

  private boolean sameRank(double r1, double r2) {
    return Math.abs(r1 - r2) < 0.1; // Ranks differ at least 0.5
  }

}
