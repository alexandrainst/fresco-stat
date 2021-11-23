package dk.alexandra.fresco.stat.filtered;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute a 1-dimensional histogram for a data set.
 */
public class HistogramFiltered implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> buckets;
  private final List<DRes<SInt>> data;
  private final List<DRes<SInt>> filter;

  /**
   * Given a list of upper bounds for buckets and a list of samples, this computation computes the
   * histogram for the given buckets. The last bucket contains all samples larger than the last
   * upper bound.
   *
   * @param buckets Soft upper bounds for buckets
   * @param data    List of samples
   */
  public HistogramFiltered(List<DRes<SInt>> buckets, List<DRes<SInt>> data, List<DRes<SInt>> filter) {
    this.buckets = buckets;
    this.data = data;
    this.filter = filter;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      Matrix<DRes<SInt>> c = new Matrix<>(buckets.size(), data.size(),
          (i,j) -> Comparison.using(par).compareLEQ(data.get(j), buckets.get(i)));
      return DRes.of(c);
    }).par((par, c) -> {
      Numeric numeric = par.numeric();
      Matrix<DRes<SInt>> filtered = new Matrix<>(buckets.size(), data.size(),
          (i,j) -> numeric.mult(c.getRow(i).get(j), filter.get(j)));
      DRes<SInt> n = AdvancedNumeric.using(par).sum(filter);
      return Pair.lazy(filtered, n);
    }).par((par, c) -> {
      List<DRes<SInt>> counts =
          c.getFirst().getRows().stream().map(r -> AdvancedNumeric.using(par).sum(r))
              .collect(Collectors.toList());
      counts.add(c.getSecond());
      return DRes.of(counts);
    }).seq((seq, counts) -> {
      for (int i = counts.size() - 1; i > 0; i--) {
        counts.set(i, seq.numeric().sub(counts.get(i), counts.get(i - 1)));
      }
      return DRes.of(counts);
    });
  }

}
