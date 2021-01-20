package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.stat.survival.SurvivalInfoSorterDiscrete;
import java.util.List;

public class CoxRegressionDiscrete extends CoxRegression<SurvivalInfoDiscrete> {

  /**
   * Estimate the coefficients of a Cox model on the given data using gradient descent.
   *
   * @param data       The data.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   * @param beta       The initial guess.
   */
  public CoxRegressionDiscrete(List<SurvivalInfoDiscrete> data, int iterations, double alpha,
      double[] beta) {
    super(data, iterations, alpha, beta,
        (d, b) -> builder -> new CoxGradientDiscrete(d, b).buildComputation(builder),
        SurvivalInfoSorterDiscrete::new);
  }
}
