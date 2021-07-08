package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.stat.survival.SurvivalInfoContinuous;
import dk.alexandra.fresco.stat.survival.SurvivalInfoSorterContinuous;
import java.util.List;

/**
 * Estimate the coefficients of a Cox model on the given data using gradient descent.
 */
public class CoxRegressionContinuous extends CoxRegression<SurvivalInfoContinuous> {

  /**
   * Estimate the coefficients of a Cox model on the given data using gradient descent.
   *
   * @param data       The data.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   * @param beta       The initial guess.
   */
  public CoxRegressionContinuous(List<SurvivalInfoContinuous> data, int iterations, double alpha,
      double[] beta) {
    super(data, iterations, alpha, beta,
        (d, b) -> builder -> new CoxGradientContinuous(d, b).buildComputation(builder),
        SurvivalInfoSorterContinuous::new);
  }
}
