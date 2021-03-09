package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import dk.alexandra.fresco.stat.linearalgebra.LinearInverseProblem;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;

public class LinearRegression implements
    Computation<LinearRegressionResult, ProtocolBuilderNumeric> {

  private final List<ArrayList<DRes<SFixed>>> observations;
  private final int n;
  private final int p;
  private final ArrayList<DRes<SFixed>> y;
  private final boolean computeErrors;

  public LinearRegression(List<ArrayList<DRes<SFixed>>> observations, ArrayList<DRes<SFixed>> y) {
    this(observations, y, true);
  }

  public LinearRegression(List<ArrayList<DRes<SFixed>>> observations, ArrayList<DRes<SFixed>> y,
      boolean computeErrors) {
    if (observations.stream().mapToInt(ArrayList::size).distinct().count() != 1) {
      throw new IllegalArgumentException(
          "Each observation must contain the same number of entries");
    }

    if (observations.size() != y.size()) {
      throw new IllegalArgumentException(
          "There must be the same number of observations and observed response variables");
    }

    this.observations = observations;
    this.n = observations.size();
    this.p = observations.get(0).size();
    this.y = y;

    this.computeErrors = computeErrors;
  }

  @Override
  public DRes<LinearRegressionResult> buildComputation(ProtocolBuilderNumeric builder) {
    Matrix<DRes<SFixed>> x = new Matrix<>(n, p, new ArrayList<>(observations));
    return builder.seq(new LinearInverseProblem(x, y))
        .seq((seq, beta) -> {

      if (computeErrors) {
        DRes<Pair<SFixed, SFixed>> sAndR = seq
            .seq(sub ->
                FixedLinearAlgebra.using(sub).vectorMult(DRes.of(x),
                    DRes.of(beta)))
            .pairInPar(
                // Compute s^2 and R^2 in parallel
                (sub, yHat) -> sub.seq(b -> DRes.of(VectorUtils.sub(y, yHat, b)))
                    .seq((b, e) -> FixedNumeric.using(b)
                        .mult(1.0 / (n - p), AdvancedFixedNumeric.using(b).innerProduct(e, e))),
                (sub, yHat) -> sub.seq(new SampleMean(y))
                    .pairInPar((b, yBar) -> new SSD(yHat, yBar).buildComputation(b),
                        (b, yBar) -> new SSD(y, yBar).buildComputation(b))
                    .seq((b, ys) -> FixedNumeric.using(b).div(ys.getFirst(), ys.getSecond())));

        // Compute std errors (squared) for all estimates
        DRes<ArrayList<DRes<SFixed>>> errors = seq.seq(sub ->
          FixedLinearAlgebra.using(sub).mult(DRes.of(MatrixUtils.transpose(x)), DRes.of(x))).seq(
            (sub, m) ->
              AdvancedLinearAlgebra.using(sub).moorePenrosePseudoInverse(m)
        ).par((sub, m) -> {
          FixedNumeric fixedNumeric = FixedNumeric.using(sub);
            return DRes.of(VectorUtils.listBuilder(m.getHeight(), i -> fixedNumeric.mult(sAndR.out().getFirst(), m.getRow(i).get(i))));
          });

        return () -> new LinearRegressionResult(beta, sAndR.out().getFirst(), errors.out(), sAndR.out().getSecond());
      }

      return DRes.of(new LinearRegressionResult(beta, null, null, null));
    });
  }

  public static class LinearRegressionResult {

    private final List<DRes<SFixed>> beta;
    private final DRes<SFixed> errorVariance;
    private final DRes<SFixed> rSquared;
    private final List<DRes<SFixed>> errors;

    private LinearRegressionResult(List<DRes<SFixed>> beta, DRes<SFixed> errorVariance, List<DRes<SFixed>> errors, DRes<SFixed> rSquared) {
      this.beta = beta;
      this.errorVariance = errorVariance;
      this.errors = errors;
      this.rSquared = rSquared;
    }

    /** Estimates for the coefficients */
    public List<DRes<SFixed>> getBeta() {
      return beta;
    }

    /** The regression error variance (s<sup>2</sup>) which is equal to the regression standard error squared */
    public DRes<SFixed> getErrorVariance() {
      return errorVariance;
    }

    /** The coefficient of determination (R<sup>2</sup>) */
    public DRes<SFixed> getRSquared() {
      return rSquared;
    }

    /** Standard errors (squared) for each coefficient estimate */
    public List<DRes<SFixed>> getStdErrorsSquared() {
      return errors;
    }

  }
}
