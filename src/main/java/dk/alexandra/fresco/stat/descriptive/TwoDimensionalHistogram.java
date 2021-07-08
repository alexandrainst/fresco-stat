package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.util.List;

/**
 * Compute a two-dimensional histogram for a given two dimensional data set.
 */
public class TwoDimensionalHistogram
    implements Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets;
  private final List<Pair<DRes<SInt>, DRes<SInt>>> data;

  public TwoDimensionalHistogram(Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets,
      List<Pair<DRes<SInt>, DRes<SInt>>> data) {
    this.buckets = buckets;
    this.data = data;
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    Matrix<DRes<SInt>> dataMatrix = MatrixUtils.buildMatrix(data.size(), 2,
        (i, j) -> j == 0 ? data.get(i).getFirst() : data.get(i).getSecond());
    List<List<DRes<SInt>>> bucketsList = List.of(buckets.getFirst(), buckets.getSecond());
    return builder
        .seq(seq -> new MultiDimensionalHistogram(bucketsList, dataMatrix).buildComputation(seq))
        .seq((seq, histogram) -> DRes.of(MatrixUtils
            .buildMatrix(buckets.getFirst().size() + 1, buckets.getSecond().size() + 1,
                histogram::get)));
  }
}
