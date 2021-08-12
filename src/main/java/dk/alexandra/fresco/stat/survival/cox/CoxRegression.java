package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.survival.SurvivalInfoSorter;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Estimate the coefficients of a Cox proportional hazards model on the given data using gradient descent.
 */
class CoxRegression<T> implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<T> data;
  private final int iterations;
  private final double alpha;
  private final double[] beta;
  private final BiFunction<List<T>, List<DRes<SFixed>>, Function<ProtocolBuilderNumeric, DRes<List<DRes<SFixed>>>>> gradient;
  private final Function<List<T>, SurvivalInfoSorter<T>> sorterProvider;

  /**
   * Estimate the coefficients of a Cox model on the given data using gradient descent.
   *
   * @param data           The data.
   * @param iterations     The number of iterations.
   * @param alpha          The learning rate.
   * @param beta           The initial guess.
   * @param gradient       Provide a computation which computes the gradient on the data
   * @param sorterProvider Provide a computation which sorts the data descending on time
   */
  CoxRegression(List<T> data, int iterations, double alpha,
      double[] beta,
      BiFunction<List<T>, List<DRes<SFixed>>, Function<ProtocolBuilderNumeric, DRes<List<DRes<SFixed>>>>> gradient,
      Function<List<T>, SurvivalInfoSorter<T>> sorterProvider) {
    this.data = data;
    this.iterations = iterations;
    this.alpha = alpha;
    this.beta = beta;
    this.gradient = gradient;
    this.sorterProvider = sorterProvider;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.seq(seq -> {
      SurvivalInfoSorter<T> sorter = sorterProvider.apply(data);
      return sorter.buildComputation(seq);
    }).par((par, sorted) -> {
      FixedNumeric numeric = FixedNumeric.using(par);
      List<DRes<SFixed>> initialBeta = Arrays.stream(beta).mapToObj(numeric::known)
          .collect(
              Collectors.toList());
      return DRes.of(new State(sorted, 0, initialBeta));
    }).whileLoop((state) -> state.iteration < iterations,
        (seq, state) -> seq.seq(sub -> this.gradient.apply(state.data, state.beta).apply(sub))
            .seq((sub, gradient) -> {
              List<DRes<SFixed>> delta = VectorUtils.scale(gradient, alpha, sub);
              List<DRes<SFixed>> newBeta = VectorUtils.add(delta, state.beta, sub);
              return DRes.of(new State(state.data, state.iteration + 1, newBeta));
            })).seq((set, state) -> DRes.of(state.beta));
  }

  private class State {

    private final List<T> data;
    private final int iteration;
    private final List<DRes<SFixed>> beta;

    private State(List<T> data, int iteration, List<DRes<SFixed>> beta) {
      this.data = data;
      this.iteration = iteration;
      this.beta = beta;
    }
  }
}
