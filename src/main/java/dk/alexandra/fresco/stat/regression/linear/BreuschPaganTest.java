package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Compute the test statistics for a Breusch-Pagan test on the result of a linear regression. The
 * null hypothesis is that homoskedasticity is present in the residuals, e.g. they have equal variance.
 * <p>
 * Note that test is valid only if the residuals are normally distributed (see ... test)
 * <p>
 * For <i>p</i> independent variables, the test statistics has a Chi-square distribution with <i>p</i>
 * degrees of freedom.
 */
public class BreuschPaganTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<ArrayList<DRes<SFixed>>> observations;
  private final List<DRes<SFixed>> residuals;
  private final DRes<SFixed> errorVariance;

  public BreuschPaganTest(List<ArrayList<DRes<SFixed>>> observations,
      List<DRes<SFixed>> residuals, DRes<SFixed> errorVariance) {
    this.observations = observations;
    this.residuals = residuals;
    this.errorVariance = errorVariance;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {

      DRes<SFixed> sReciprocal = AdvancedFixedNumeric.using(par)
          .reciprocal(errorVariance);

      List<DRes<SFixed>> eiSquared = new ArrayList<>();
      for (DRes<SFixed> ei : residuals) {
        eiSquared.add(FixedNumeric.using(par).mult(ei, ei));
      }
      return Pair.lazy(sReciprocal, eiSquared);

    }).par((par, sReciprocalAndResidualsSquared) -> {
      ArrayList<DRes<SFixed>> g = new ArrayList<>();
      for (DRes<SFixed> eiSquared : sReciprocalAndResidualsSquared.getSecond()) {
        g.add(FixedNumeric.using(par).mult(eiSquared, sReciprocalAndResidualsSquared.getFirst()));
      }
      return DRes.of(g);
    }).par((par, g) -> par.seq(new LinearRegression(observations, g, false))
    ).par((par, regressionResult) -> FixedNumeric.using(par)
        .mult(0.5, regressionResult.getModelSumOfSquares()));
  }
}
