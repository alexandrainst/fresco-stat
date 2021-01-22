package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;

/**
 * Solve a linear inverse problem, eg. find an <i>x</i> such that Ax = b where A is an <i>mxn</i>-matrix
 * and <i>b</i> is an <i>n</i>-dimensional vector. If a system is overdetermined (m &ge; n), the
 * computation will find the <i>x</i> minimising &#x7c;&#x7c;<i>Ax - b</i>&#x7c;&#x7c;.
 */
public class LinearInverseProblem implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;
  private final ArrayList<DRes<SFixed>> b;
  private final boolean underDetermined;

  public LinearInverseProblem(Matrix<DRes<SFixed>> a, ArrayList<DRes<SFixed>> b) {
    underDetermined = a.getWidth() < a.getHeight();
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    Matrix<DRes<SFixed>> at = underDetermined ? a : MatrixUtils.transpose(a);

    return builder.seq(seq -> new QRDecomposition(at).buildComputation(seq))
        .seq((seq, qr) -> {
          Matrix<DRes<SFixed>> r1 =
              underDetermined ? qr.getSecond() : MatrixUtils.transpose(qr.getSecond());
          return Pair.lazy(qr.getFirst(), r1);
        }).seq((seq, qr1) -> {
          Matrix<DRes<SFixed>> q = qr1.getFirst();
          DRes<ArrayList<DRes<SFixed>>> x;
          if (underDetermined) {
            x = seq.seq(sub -> {

              // The transpose of the first n columns of q
              Matrix<DRes<SFixed>> q1 = MatrixUtils
                  .buildMatrix(a.getWidth(), q.getHeight(), (i, j) -> q.getRow(j).get(i));

              return FixedLinearAlgebra.using(sub).vectorMult(DRes.of(q1), DRes.of(b));
            }).seq(
                (sub, b1) -> new BackwardSubstitution(qr1.getSecond(), b1).buildComputation(sub));

          } else {
            x = seq.seq(sub -> new ForwardSubstitution(qr1.getSecond(), b)
                .buildComputation(sub)).seq((sub, rtb) -> {
              FixedNumeric numeric = FixedNumeric.using(sub);
              for (int i = a.getWidth(); i < a.getHeight(); i++) {
                rtb.add(numeric.known(0));
              }
              return FixedLinearAlgebra.using(sub).vectorMult(DRes.of(q), DRes.of(rtb));
            });
          }
          return x;
        });
  }
}
