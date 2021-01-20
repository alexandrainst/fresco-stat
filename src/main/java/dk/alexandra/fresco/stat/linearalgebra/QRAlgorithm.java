package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

/**
 * Compute eigenvalues of a matrix using the iterative QR algorithm.
 */
public class QRAlgorithm implements
    Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> l;
  private final int iterations;

  public QRAlgorithm(Matrix<DRes<SFixed>> l, int iterations) {
    assert (l.getHeight() == l.getWidth());
    this.l = l;
    this.iterations = iterations;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq ->
        () -> new State(() -> l, 0))
        .whileLoop(state -> state.k < iterations, (seq, state) -> {
          DRes<Matrix<DRes<SFixed>>> a = seq.seq(sub -> new QRDecomposition(
              state.a.out()).buildComputation(sub)).seq((sub, qr) -> {
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(sub);
            return fixedLinearAlgebra.mult(qr::getSecond, qr::getFirst);
          });
          return () -> new State(a, state.k + 1);
        }).seq((seq, state) -> () ->
            VectorUtils.listBuilder(l.getHeight(), i -> state.a.out().getRow(i).get(i)));
  }

  private static class State {

    private final DRes<Matrix<DRes<SFixed>>> a;
    private final int k;

    private State(DRes<Matrix<DRes<SFixed>>> a, int k) {
      this.a = a;
      this.k = k;
    }
  }
}
