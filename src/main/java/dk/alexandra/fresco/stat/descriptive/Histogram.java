package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Histogram implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private List<DRes<SInt>> buckets;
  private List<DRes<SInt>> data;

  /**
   * Given a list of upper bounds for buckets and a list of samples, this computation computes the
   * histogram for the given buckets. The last bucket contains all samples larger than the last
   * upper bound.
   *
   * @param buckets Soft upper bounds for buckets
   * @param data    List of samples
   */
  public Histogram(List<DRes<SInt>> buckets, List<DRes<SInt>> data) {
    this.buckets = buckets;
    this.data = data;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      Matrix<DRes<SInt>> c = new Matrix<>(buckets.size(), data.size(),
          i -> data.stream().map(x -> Comparison.using(par).compareLEQ(x, buckets.get(i)))
              .collect(Collectors.toCollection(ArrayList::new)));
      return () -> c;
    }).par((par, c) -> {
      List<DRes<SInt>> counts =
          c.getRows().stream().map(r -> AdvancedNumeric.using(par).sum(r)).collect(Collectors.toList());
      counts.add(par.numeric().known(data.size()));
      return () -> counts;
    }).seq((seq, counts) -> {
      for (int i = counts.size() - 1; i > 0; i--) {
        counts.set(i, seq.numeric().sub(counts.get(i), counts.get(i - 1)));
      }
      return () -> counts;
    });
  }

}
