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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute a 1-dimensional histogram for a data set. The input is <i>filtered</i>, meaning that besides
 * the data set, the input also consists of a secret 0-1-vector (a <code>filter</code>) which
 * indicates what entries of the data set should be included in the analysis
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

      // Compare all data points with the buckets
      Matrix<DRes<SInt>> c = new Matrix<>(buckets.size(), data.size(),
          (i,j) -> Comparison.using(par).compareLEQ(data.get(j), buckets.get(i)));
      return DRes.of(c);

    }).par((par, c) -> {

      // Filter so we only count data points as indicated by the filter
      Numeric numeric = par.numeric();
      Matrix<DRes<SInt>> filtered = new Matrix<>(buckets.size(), data.size(),
          (i,j) -> numeric.mult(c.getRow(i).get(j), filter.get(j)));
      DRes<SInt> n = AdvancedNumeric.using(par).sum(filter);
      return Pair.lazy(filtered, n);

    }).par((par, c) -> {

      // Compute cumulative sums
      List<DRes<SInt>> counts =
          c.getFirst().getRows().stream().map(AdvancedNumeric.using(par)::sum)
              .collect(Collectors.toList());
      counts.add(c.getSecond()); // Add the total as the final element
      return DRes.of(counts);

    }).seq((seq, counts) -> {

      // The histogram is the difference between the cumulative sums
      List<DRes<SInt>> histogram = new ArrayList<>();
      histogram.add(counts.get(0));
      for (int i = 1; i < counts.size(); i++) {
        histogram.add(seq.numeric().sub(counts.get(i), counts.get(i - 1)));
      }
      return DRes.of(histogram);

    });
  }

}
