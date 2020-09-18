package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CoxRegression implements Computation<List<DRes<SReal>>, ProtocolBuilderNumeric> {

  private final List<SurvivalInfoDiscrete> data;
  private final int iterations;
  private final double alpha;
  private final double[] beta;

  /**
   * Estimate the coefficients of a Cox model on the given data using gradient descent.
   *
   * @param data       The data.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   * @param beta       The initial guess.
   */
  public CoxRegression(List<SurvivalInfoDiscrete> data, int iterations, double alpha,
      double[] beta) {
    this.data = data;
    this.iterations = iterations;
    this.alpha = alpha;
    this.beta = beta;
  }

  @Override
  public DRes<List<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.seq(seq -> {
      DRes<List<SurvivalInfoDiscrete>> sorted = new SortSurvivalInfoList(data)
          .buildComputation(seq);
      return sorted;
    }).par((par, sorted) -> {
      List<DRes<SReal>> initialBeta = Arrays.stream(beta).mapToObj(par.realNumeric()::known)
          .collect(
              Collectors.toList());
      return () -> new State(sorted, 0, initialBeta);
    }).whileLoop((state) -> state.iteration < iterations, (seq, state) -> {
      return seq.seq(sub -> {
        DRes<List<DRes<SReal>>> gradient = new CoxGradientDiscrete(state.data, state.beta)
            .buildComputation(sub);
        return gradient;
      }).seq((sub, gradient) -> {
        List<DRes<SReal>> delta = VectorUtils.scale(gradient, alpha, sub);
        List<DRes<SReal>> newBeta = VectorUtils.add(delta, state.beta, sub);
        return () -> new State(state.data, state.iteration + 1, newBeta);
      });
    }).seq((set, state) -> () -> state.beta);
  }

  private class State {

    private final List<SurvivalInfoDiscrete> data;
    private final int iteration;
    private final List<DRes<SReal>> beta;

    private State(List<SurvivalInfoDiscrete> data, int iteration, List<DRes<SReal>> beta) {
      this.data = data;
      this.iteration = iteration;
      this.beta = beta;
    }
  }
}
