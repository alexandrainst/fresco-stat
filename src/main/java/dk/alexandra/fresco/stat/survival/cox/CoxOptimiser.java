package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

// Maximise the score function of a cox model
public class CoxOptimiser implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> init;
  private final int iterations;
  private final double stepSize;
  private final Function<List<DRes<SFixed>>, CoxGradient.CoxGradientInternal> gradient;
  private final BiFunction<List<DRes<SFixed>>, CoxGradient.CoxGradientInternal.State, CoxHessianInternal> hessian;

  public CoxOptimiser(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
      List<DRes<SFixed>> init,
      int iterations, double stepSize) {
    this.init = init;
    this.iterations = iterations;
    this.stepSize = stepSize;
    this.gradient = b -> new CoxGradient.CoxGradientInternal(data, tiedGroups, b);

    // The computation of the Hessian reuses some of the intermediate values from the computation of the gradient (the "state" parameter)
    this.hessian = (b, state) -> new CoxHessianInternal(data, tiedGroups, b, state);
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> new State(init, 0)).whileLoop(
        state -> state.iteration < iterations, (root, state) -> root.seq(seq -> {

          // Compute the gradient
          return gradient.apply(state.beta).buildComputation(seq);

        }).seq((seq, g) -> {

          // The computation of the Hessian reuses some of the intermediate values from the computation of the gradient
          DRes<Matrix<DRes<SFixed>>> h = hessian.apply(state.beta, g.out()).buildComputation(seq);
          return Pair.lazy(g.out().result, h);

        }).seq((seq, gradientAndHessian) -> {

          // Compute the (pseudo)inverse of the Hessian
          DRes<Matrix<DRes<SFixed>>> hessianInverse =
              AdvancedLinearAlgebra.using(seq)
                  .moorePenrosePseudoInverse(gradientAndHessian.getSecond().out());
          return Pair.lazy(gradientAndHessian.getFirst(), hessianInverse);

        }).seq((seq, gradientAndHessianInverse) -> FixedLinearAlgebra.using(seq)
            .vectorMult(gradientAndHessianInverse.getSecond(),
                DRes.of(gradientAndHessianInverse.getFirst()))
        ).seq((seq, delta) -> {

          // Iterate, x_{i+1} = x_i + G * H^{-1} (Newton's method)
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

