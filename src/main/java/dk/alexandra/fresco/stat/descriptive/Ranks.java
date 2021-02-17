package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Output ranks with averaged ties and correction term for Kruskall-Wallis.
 */
public class Ranks implements
    Computation<Pair<List<DRes<SFixed>>, Double>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> samples;

  public Ranks(List<List<DRes<SInt>>> samples) {
    this.samples = samples;
  }

  @Override
  public DRes<Pair<List<DRes<SFixed>>, Double>> buildComputation(
      ProtocolBuilderNumeric builder) {

    List<Pair<DRes<SInt>, List<DRes<SInt>>>> valuesWithClassIndicators = new ArrayList<>();

    // For each data point we add a vector indicating what group it belongs to
    int n = 0;
    for (int i = 0; i < samples.size(); i++) {
      List<DRes<SInt>> sample = samples.get(i);
      List<DRes<SInt>> indicators = new ArrayList<>();
      for (int j = 0; j < samples.size(); j++) {
        indicators.add(builder.numeric().known(i == j ? 1 : 0));
      }
      for (DRes<SInt> dataPoint : sample) {
        valuesWithClassIndicators.add(new Pair<>(dataPoint, indicators));
        n++;
      }
    }

    int finalN = n;
    return builder.seq(seq ->
        // Sort data points
        dk.alexandra.fresco.lib.common.collections.Collections.using(seq)
            .sort(valuesWithClassIndicators)
    ).seq((seq, sorted) -> {

      Collections.reverse(sorted);

      Pair<List<Pair<DRes<SInt>, List<DRes<SInt>>>>, DRes<List<Double>>> out;

      // Break ties by opening encrypted values. Note that this leaks the ranks of elements that are equal
      DRes<List<Double>> r1 = new LeakyBreakTies(
          sorted.stream().map(Pair::getFirst).collect(Collectors.toList())).buildComputation(seq);
      out = new Pair<>(sorted, r1);
      return DRes.of(out);
    }).par((par, dataAndRanks) -> {

      List<Double> ranks = dataAndRanks.getSecond().out();
      double g = 0.0;

      // Compute correction factor. First find groups of equal elements and count the number of
      // elements in each group.
      List<Integer> tiedGroups = new ArrayList<>();
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

      // Sum of ranks for each group
      List<DRes<SFixed>> totals = new ArrayList<>();
      for (int j = 0; j < samples.size(); j++) {
        List<DRes<SInt>> column = new ArrayList<>();
        for (Pair<DRes<SInt>, List<DRes<SInt>>> row : dataAndRanks.getFirst()) {
          column.add(row.getSecond().get(j));
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
