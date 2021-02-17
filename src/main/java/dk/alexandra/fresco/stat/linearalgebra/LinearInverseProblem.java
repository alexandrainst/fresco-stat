package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.util.ArrayList;

/**
 * Solve a linear inverse problem, eg. find an <i>x</i> such that Ax = b where A is an
 * <i>mxn</i>-matrix and <i>b</i> is an <i>n</i>-dimensional vector. If a system is overdetermined
 * (m &ge; n), the computation will find the <i>x</i> minimising &#x7c;&#x7c;<i>Ax -
 * b</i>&#x7c;&#x7c;.
 */
public class LinearInverseProblem implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;
  private final ArrayList<DRes<SFixed>> b;
  private final boolean overDetermined;

  public LinearInverseProblem(Matrix<DRes<SFixed>> a, ArrayList<DRes<SFixed>> b) {
    overDetermined = a.getWidth() < a.getHeight();
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    Matrix<DRes<SFixed>> at = overDetermined ? a : MatrixUtils.transpose(a);

    return builder.seq(seq -> new QRDecomposition(at).buildComputation(seq))
        .seq((seq, qr) -> {
          Matrix<DRes<SFixed>> r1 =
              overDetermined ? qr.getSecond() : MatrixUtils.transpose(qr.getSecond());
          return Pair.lazy(qr.getFirst(), r1);
        }).seq((seq, qr1) -> {
          Matrix<DRes<SFixed>> q = qr1.getFirst();
          Matrix<DRes<SFixed>> r = qr1.getSecond();
          if (overDetermined) {
            return seq.seq(sub -> {
              // The transpose of the first n columns of q
              Matrix<DRes<SFixed>> q1 = MatrixUtils
                  .buildMatrix(a.getWidth(), q.getHeight(), (i, j) -> q.getRow(j).get(i));
              return FixedLinearAlgebra.using(sub).vectorMult(DRes.of(q1), DRes.of(b));
            }).seq(
                (sub, b1) -> new BackSubstitution(r, b1).buildComputation(sub));
          } else {
            DRes<ArrayList<DRes<SFixed>>> rtb = new ForwardSubstitution(r, b)
                .buildComputation(seq);
            return FixedLinearAlgebra.using(seq).vectorMult(DRes.of(q), rtb);
          }
        });
  }
}
