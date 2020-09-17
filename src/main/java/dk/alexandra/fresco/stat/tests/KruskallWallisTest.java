package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.Ranks;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Compute the Kruskall-Wallis test statistic on <i>k</i> groups, also known as one-way ANOVA on
 * ranks. The test statistic may be approximated by a 𝛘<sup>2</sup> distribution with <i>k-1</i>
 * degrees of freedom.</p>
 *
 * <p>The elements are ranked sequentially ignoring ties unless the averageTies parameter is set,
 * in which case ties are replaced with the average of the ranks of the elements it is tied with.
 * Setting averageTies to true returns the standard test statistics, but using false should not
 * affect the test statistic too much. If averageTies is set to true, the ranks of the ties will be
 * leaked to all parties.</p>
 */
public class KruskallWallisTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> observed;
  private boolean averageTies;

  public KruskallWallisTest(List<List<DRes<SInt>>> observed) {
    this(observed, false);
  }

  public KruskallWallisTest(List<List<DRes<SInt>>> observed, boolean averageTies) {
    this.observed = observed;
    this.averageTies = averageTies;
  }

  /**
   * If the test is to be applied on fixed point numbers (SReal's), this method should be used to
   * transform the data,
   *
   * @param observed The data as SReal's
   * @return The input data as SInts to be used in the test.
   */
  public static List<List<DRes<SInt>>> fromSReal(List<List<DRes<SReal>>> observed) {
    return observed.stream().map(
        sample -> sample.stream().map(x -> ((SFixed) x).getSInt()).collect(Collectors.toList()))
        .collect(
            Collectors.toList());
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    int groups = observed.size();
    int N = observed.stream().mapToInt(List::size).sum();

    return builder.seq(seq -> {
      DRes<Pair<List<DRes<SReal>>, Double>> ranks = new Ranks(observed, averageTies)
          .buildComputation(seq);
      return ranks;
    }).par((par, ranks) -> {
      List<DRes<SReal>> squared = ranks.getFirst().stream()
          .map(rank -> par.realNumeric().mult(rank, rank))
          .collect(
              Collectors.toList());
      return Pair.lazy(squared, ranks.getSecond());
    }).par((par, squared) -> {
      List<DRes<SReal>> squaredAverages = new ArrayList<>();
      for (int i = 0; i < groups; i++) {
        squaredAverages
            .add(par.realNumeric().div(squared.getFirst().get(i), observed.get(i).size()));
      }
      return Pair.lazy(squaredAverages, squared.getSecond());
    }).seq((seq, squaredAverages) -> {
      DRes<SReal> h = seq.realAdvanced().sum(squaredAverages.getFirst());

      h = seq.realNumeric().mult(12.0, h);
      h = seq.realNumeric().div(h, N * (N + 1));
      h = seq.realNumeric().sub(h, 3 * (N + 1));

      if (averageTies) {
        h = seq.realNumeric().mult(squaredAverages.getSecond(), h);
      }

      return h;
    });
  }
}
