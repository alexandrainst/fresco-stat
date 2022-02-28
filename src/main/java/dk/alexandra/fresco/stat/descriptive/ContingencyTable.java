package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute a contingency table on a list of observations.
 */
public class ContingencyTable implements Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<Pair<List<DRes<SInt>>, List<DRes<SInt>>>> data;
  private final int height;
  private final int width;

  /**
   * <p>
   *   The data is encoded as follows: Each row is an observation which consists of two attributes, each
   *   of which is encoded as a 0-1 vector with exactly one non-zero entry to indicate the value of the attribute.
   * </p>
   * <p>
   *   If the data is not encoded as a 0-1 vector, use {@link ContingencyTableCategorical} instead.
   * </p>
   */
  public ContingencyTable(List<Pair<List<DRes<SInt>>, List<DRes<SInt>>>> data) {
    this.data = data;
    this.width = data.get(0).getFirst().size();
    this.height = data.get(0).getSecond().size();
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par ->
        DRes.of(MatrixUtils.buildMatrix(width, height, (i, j) ->
                par.par(sub ->
                    DRes.of(data.stream().map(row ->
                        sub.numeric().mult(row.getFirst().get(i), row.getSecond().get(j)))
                        .collect(Collectors.toList()))).seq((seq, terms) ->
                    AdvancedNumeric.using(seq).sum(terms)
                )
            )
        ));
  }
}
