package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MatrixUtils;

/**
 * Compute the Moore-Penrose pseudo inverse of a matrix <i>A</i>. Only the case where <i>A</i> have
 * either full column or row rank is covered by this function.
 */
public class MoorePenrosePseudoInverse implements
    Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;

  public MoorePenrosePseudoInverse(Matrix<DRes<SFixed>> a) {
    boolean fullColumnRank = a.getHeight() >= a.getWidth();
    assert (fullColumnRank); // TODO: Only this case is currently supported
    this.a = a;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> new QRDecomposition(a).buildComputation(seq)).seq(
        (seq, qr) -> new InvertUpperTriangularMatrix(MatrixUtils.transpose(qr.getSecond()))
            .buildComputation(seq)).seq((seq, rtinv) -> {
      FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(seq);
      return fixedLinearAlgebra.mult(DRes.of(MatrixUtils.transpose(rtinv)),
          fixedLinearAlgebra.mult(DRes.of(rtinv), DRes.of(MatrixUtils.transpose(a))));
    });

  }
}
