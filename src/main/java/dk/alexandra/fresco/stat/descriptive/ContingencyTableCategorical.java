package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.util.SIntPair;
import dk.alexandra.fresco.stat.utils.ParallelIndicator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute a contingency table on a list of observations.
 */
public class ContingencyTableCategorical implements
    Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<SIntPair> data;
  private final int height;
  private final int width;

  /**
   * Given a list of observations, each consisting of two categorical values <i>(x,y)</i> with <i>0
   * &le; width</i> and <i>0 &le; y < height</i>, this computation outputs a contingency table,
   * where the <i>(i,j)</i>'th entry is the number of observations such that <i>x = i</i> and <i>y =
   * j</i>.
   */
  public ContingencyTableCategorical(List<SIntPair> data, int width, int height) {
    this.data = data;
    this.width = width;
    this.height = height;
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<Pair<DRes<List<DRes<SInt>>>, DRes<List<DRes<SInt>>>>> indicators = new ArrayList<>();
      for (SIntPair row : data) {
        ParallelIndicator leftIndicator = new ParallelIndicator(width, row.getFirst());
        ParallelIndicator rightIndicator = new ParallelIndicator(height, row.getSecond());
        indicators.add(
            new Pair<>(leftIndicator.buildComputation(par), rightIndicator.buildComputation(par)));
      }
      return DRes.of(indicators);
    }).seq((seq, indicators) -> {
      List<Pair<List<DRes<SInt>>, List<DRes<SInt>>>> data = indicators.stream()
          .map(pair -> new Pair<>(pair.getFirst().out(), pair.getSecond().out())).collect(
              Collectors.toList());
      return seq.seq(new ContingencyTable(data));
    });
  }
}
