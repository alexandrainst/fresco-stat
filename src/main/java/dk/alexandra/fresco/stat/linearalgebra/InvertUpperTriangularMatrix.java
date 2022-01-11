package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MatrixUtils;

/**
 * Invert upper triangular matrix.
 */
public class InvertUpperTriangularMatrix implements
    Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> l;

  public InvertUpperTriangularMatrix(Matrix<DRes<SFixed>> l) {
    assert (l.getHeight() == l.getWidth());
    assert (l.getHeight() >= 1);
    this.l = l;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq ->
        new InvertLowerTriangularMatrix(MatrixUtils.transpose(l)).buildComputation(seq)).seq(
            (seq, ltInv) -> DRes.of(MatrixUtils.transpose(ltInv)));
  }


}
