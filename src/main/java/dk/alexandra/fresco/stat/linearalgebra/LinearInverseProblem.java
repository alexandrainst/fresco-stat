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
import java.util.List;

/**
 * Solve a linear inverse problem, eg. find x such that Ax = b where A is a matrix and b is a vector.
 */
public class LinearInverseProblem implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;
  private final ArrayList<DRes<SFixed>> b;

  public LinearInverseProblem(Matrix<DRes<SFixed>> a, ArrayList<DRes<SFixed>> b) {
    // TODO for now we can only handle square matrices, but when QR decomposition is generalized to
    // rectangular matrices, we may allow a.getHeight() <= a.getWidth().
    assert (a.getHeight() == a.getWidth());
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      FixedLinearAlgebra linearAlgebra = FixedLinearAlgebra.using(seq);
      return linearAlgebra.transpose(() -> a);
    }).seq((seq, at) -> new QRDecomposition(at).buildComputation(seq))
        .seq((seq, qr) -> {
          FixedLinearAlgebra linearAlgebra = FixedLinearAlgebra.using(seq);
          DRes<Matrix<DRes<SFixed>>> rt = linearAlgebra.transpose(qr::getSecond);
          return Pair.lazy(qr.getFirst(), rt);
        }).seq((seq, qrt) -> {
          DRes<ArrayList<DRes<SFixed>>> rtb = new ForwardSubstitution(qrt.getSecond().out(), b)
              .buildComputation(seq);
          return Pair.lazy(qrt.getFirst(), rtb);
        }).seq((seq, qrtb) -> {
          FixedLinearAlgebra linearAlgebra = FixedLinearAlgebra.using(seq);
          FixedNumeric numeric = FixedNumeric.using(seq);

          ArrayList<DRes<SFixed>> padded = qrtb.getSecond().out();
          for (int i = a.getHeight(); i < a.getWidth(); i++) {
            padded.add(numeric.known(0));
          }
          return linearAlgebra.vectorMult(() -> qrtb.getFirst(), () -> padded);
        });
  }
}
