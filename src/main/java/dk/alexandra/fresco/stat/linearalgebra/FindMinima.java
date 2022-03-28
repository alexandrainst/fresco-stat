package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FindMinima implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> init;
  private final Function<List<DRes<SFixed>>, Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric>> gradient;
  private final Function<List<DRes<SFixed>>, Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric>> hessian;
  private final int iterations;
  private final double stepSize;

  public FindMinima(List<DRes<SFixed>> init,
      Function<List<DRes<SFixed>>, Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric>> gradient,
      Function<List<DRes<SFixed>>, Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric>> hessian,
      int iterations, double stepSize) {
    this.init = init;
    this.gradient = gradient;
    this.hessian = hessian;
    this.iterations = iterations;
    this.stepSize = stepSize;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> new State(init, 0)).whileLoop(
        state -> state.iteration < iterations, (root, state) -> root.par(par -> {
          DRes<ArrayList<DRes<SFixed>>> g = gradient.apply(state.beta).buildComputation(par);
          DRes<Matrix<DRes<SFixed>>> h = hessian.apply(state.beta).buildComputation(par);
          return Pair.lazy(g, h);
        }).seq((seq, gradientAndHessian) -> {
          DRes<Matrix<DRes<SFixed>>> hessianInverse =
              AdvancedLinearAlgebra.using(seq)
                  .moorePenrosePseudoInverse(gradientAndHessian.getSecond().out());
          return FixedLinearAlgebra.using(seq)
              .vectorMult(hessianInverse, gradientAndHessian.getFirst());
        }).seq((seq, delta) -> {
          List<DRes<SFixed>> deltaScaled = VectorUtils.scale(delta, stepSize, seq);
          List<DRes<SFixed>> beta = VectorUtils.sub(state.beta, deltaScaled, seq);
          return new State(beta, state.iteration + 1);
        })).seq((seq, state) -> DRes.of(state.beta));
  }

  private static class State implements DRes<State> {

    private final List<DRes<SFixed>> beta;
    private final int iteration;

    private State(List<DRes<SFixed>> beta, int iteration) {
      this.beta = beta;
      this.iteration = iteration;
    }

    @Override
    public State out() {
      return this;
    }
  }
}

