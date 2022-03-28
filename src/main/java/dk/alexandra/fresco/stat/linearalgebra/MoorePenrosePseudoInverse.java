package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
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
//    if (a.getWidth() == 1 && a.getHeight() == 1) {
//      return DRes.of(MatrixUtils.buildMatrix(1,1, (i,j) -> builder.seq(seq -> AdvancedFixedNumeric.using(seq).reciprocal(a.getRow(0).get(0)))));
//    }

    return builder.seq(seq -> new QRDecomposition(a).buildComputation(seq)).seq(
        (seq, qr) -> new InvertUpperTriangularMatrix(qr.getSecond())
            .buildComputation(seq)).seq((seq, rInverse) -> {
      FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(seq);
      return fixedLinearAlgebra.mult(DRes.of(rInverse),
          fixedLinearAlgebra
              .mult(DRes.of(MatrixUtils.transpose(rInverse)), DRes.of(MatrixUtils.transpose(a))));
    });

  }
}
