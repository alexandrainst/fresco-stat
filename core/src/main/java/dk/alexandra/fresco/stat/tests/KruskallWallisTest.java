package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
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
public class KruskallWallisTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> observed;

  public KruskallWallisTest(List<List<DRes<SInt>>> observed) {
    this.observed = observed;
  }

  /**
   * If the test is to be applied on fixed point numbers (SFixed's), this method should be used to
   * transform the data,
   *
   * @param observed The data as fixed point numbers.
   * @return The input data scaled to integers to be used in the test.
   */
  public static List<List<DRes<SInt>>> fromSFixed(List<List<DRes<SFixed>>> observed) {
    return observed.stream().map(
        sample -> sample.stream().map(x -> x.out().getSInt()).collect(Collectors.toList()))
        .collect(
            Collectors.toList());
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    int groups = observed.size();
    int N = observed.stream().mapToInt(List::size).sum();

    return builder.seq(seq -> new Ranks(observed)
        .buildComputation(seq)).par((par, ranks) -> {
      List<DRes<SFixed>> squared = ranks.getFirst().stream()
          .map(rank -> FixedNumeric.using(par).mult(rank, rank))
          .collect(
              Collectors.toList());
      return Pair.lazy(squared, ranks.getSecond());
    }).par((par, squared) -> {
      List<DRes<SFixed>> squaredAverages = new ArrayList<>();
      for (int i = 0; i < groups; i++) {
        squaredAverages
            .add(FixedNumeric.using(par).div(squared.getFirst().get(i), observed.get(i).size()));
      }
      return Pair.lazy(squaredAverages, squared.getSecond());
    }).seq((seq, squaredAverages) -> {
      DRes<SFixed> h = AdvancedFixedNumeric.using(seq).sum(squaredAverages.getFirst());

      FixedNumeric numeric = FixedNumeric.using(seq);
      h = numeric.mult(12.0, h);
      h = numeric.div(h, N * (N + 1));
      h = numeric.sub(h, 3 * (N + 1));
      h = numeric.mult(squaredAverages.getSecond(), h);
      return h;
    });
  }
}
