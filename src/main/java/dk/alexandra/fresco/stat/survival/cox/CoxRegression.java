package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.survival.SurvivalInfoSorter;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

class CoxRegression<T> implements Computation<List<DRes<SReal>>, ProtocolBuilderNumeric> {

  private final List<T> data;
  private final int iterations;
  private final double alpha;
  private final double[] beta;
  private final BiFunction<List<T>, List<DRes<SReal>>, Function<ProtocolBuilderNumeric, DRes<List<DRes<SReal>>>>> gradient;
  private final Function<List<T>, SurvivalInfoSorter<T>> sorterProvider;

  /**
   * Estimate the coefficients of a Cox model on the given data using gradient descent.
   *
   * @param data       The data.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   * @param beta       The initial guess.
   * @param gradient   Provide a computation which computes the gradient on the data
   * @param sorterProvider Provide a computation which sorts the data descending on time
   */
  CoxRegression(List<T> data, int iterations, double alpha,
      double[] beta,
      BiFunction<List<T>, List<DRes<SReal>>, Function<ProtocolBuilderNumeric, DRes<List<DRes<SReal>>>>> gradient,
      Function<List<T>, SurvivalInfoSorter<T>> sorterProvider) {
    this.data = data;
    this.iterations = iterations;
    this.alpha = alpha;
    this.beta = beta;
    this.gradient = gradient;
    this.sorterProvider = sorterProvider;
  }

  @Override
  public DRes<List<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.seq(seq -> {
      SurvivalInfoSorter<T> sorter = sorterProvider.apply(data);
      DRes<List<T>> sorted = sorter.buildComputation(seq);
      return sorted;
    }).par((par, sorted) -> {
      List<DRes<SReal>> initialBeta = Arrays.stream(beta).mapToObj(par.realNumeric()::known)
          .collect(
              Collectors.toList());
      return () -> new State(sorted, 0, initialBeta);
    }).whileLoop((state) -> state.iteration < iterations, (seq, state) -> {
      return seq.seq(sub -> {
        DRes<List<DRes<SReal>>> gradient = this.gradient.apply(state.data, state.beta).apply(sub);
        return gradient;
      }).seq((sub, gradient) -> {
        List<DRes<SReal>> delta = VectorUtils.scale(gradient, alpha, sub);
        List<DRes<SReal>> newBeta = VectorUtils.add(delta, state.beta, sub);
        return () -> new State(state.data, state.iteration + 1, newBeta);
      });
    }).seq((set, state) -> () -> state.beta);
  }

  private class State {

    private final List<T> data;
    private final int iteration;
    private final List<DRes<SReal>> beta;

    private State(List<T> data, int iteration, List<DRes<SReal>> beta) {
      this.data = data;
      this.iteration = iteration;
      this.beta = beta;
    }
  }
}
