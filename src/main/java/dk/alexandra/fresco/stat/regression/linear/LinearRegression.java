package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import dk.alexandra.fresco.stat.descriptive.helpers.SSE;
import dk.alexandra.fresco.stat.linearalgebra.LinearInverseProblem;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Fit a linear model to the given dataset and output estimates for the coefficients and the regression
 * error variance (s<sup>2</sup>) which is equal to the regression standard error squared,
 * the coefficient of determination (R<sup>2</sup>) and the standard errors (squared) for each coefficient
 * estimate. (see {@link LinearRegressionResult}.
 */
public class LinearRegression implements
    Computation<LinearRegressionResult, ProtocolBuilderNumeric> {

  private final List<ArrayList<DRes<SFixed>>> observations;
  private final int n;
  private final int p;
  private final ArrayList<DRes<SFixed>> y;

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
  }

  @Override
  public DRes<LinearRegressionResult> buildComputation(ProtocolBuilderNumeric builder) {
    Matrix<DRes<SFixed>> x = new Matrix<>(n, p, new ArrayList<>(observations));

    State state = new State();
    return builder.par(par -> {
      state.estimates =  par.seq(new LinearInverseProblem(x, y));
      state.mean = par.seq(new SampleMean(y));
      return state;
    }).seq((seq, s) -> {
      state.yHat = FixedLinearAlgebra.using(seq).vectorMult(DRes.of(x), state.estimates);
      return state;
    }).pairInPar(
        (seq, s) -> seq.seq(new SSD(state.yHat.out(), state.mean)),
        (seq, s) -> seq.seq(new SSD(y, state.mean))
        ).seq((seq, ssmAndSst) -> {
          state.ssm = ssmAndSst.getFirst();
          state.sst = ssmAndSst.getSecond();
          state.sse = FixedNumeric.using(seq).sub(state.sst, state.ssm);
          return state;
    }).pairInPar(
        (seq, s) -> FixedNumeric.using(seq).div(state.sse, n-p),
        (seq, s) -> FixedNumeric.using(seq).div(state.ssm, state.sst)
    ).par((par, errorVarianceAndSampleCorrelation) -> {
      state.errorVariance = errorVarianceAndSampleCorrelation.getFirst();
      state.rSquared = errorVarianceAndSampleCorrelation.getSecond();
      state.adjustedRSquared = par.seq(seq ->
          FixedNumeric.using(seq).sub(1, FixedNumeric.using(seq)
              .mult((double) (n - 1) / (n - p), FixedNumeric.using(seq).sub(1, state.rSquared)))
      );

      // Compute std errors (squared) for all estimates
      state.errors = par.seq(sub ->
          FixedLinearAlgebra.using(sub).mult(DRes.of(MatrixUtils.transpose(x)), DRes.of(x))).seq(
          (sub, m) ->
              AdvancedLinearAlgebra.using(sub).moorePenrosePseudoInverse(m)
      ).par((sub, m) -> {
        FixedNumeric fixedNumeric = FixedNumeric.using(sub);
        return DRes.of(VectorUtils.listBuilder(m.getHeight(),
            i -> fixedNumeric.mult(state.errorVariance, m.getRow(i).get(i))));
      });

      state.F = par.seq(sub ->
          FixedNumeric.using(sub).div(state.ssm, state.sse)
      ).seq((sub, q) -> FixedNumeric.using(sub).mult((double) (n - p) / (p - 1), q));

//
//
//        DRes<Pair<SFixed, SFixed>> sAndR = seq
//            .seq(sub ->
//                FixedLinearAlgebra.using(sub).vectorMult(DRes.of(x),
//                    DRes.of(beta)))
//            .pairInPar(
//                // Compute s^2 and R^2 in parallel
//                (sub, yHat) -> sub.seq(b -> DRes.of(VectorUtils.sub(y, yHat, b)))
//                    .seq((b, e) -> FixedNumeric.using(b)
//                        .mult(1.0 / (n - p), AdvancedFixedNumeric.using(b).innerProduct(e, e))),
//                (sub, yHat) -> sub.seq(new SampleMean(y))
//                    .pairInPar((b, yBar) -> new SSD(yHat, yBar).buildComputation(b),
//                        (b, yBar) -> new SSD(y, yBar).buildComputation(b))
//                    .seq((b, ys) -> FixedNumeric.using(b).div(ys.getFirst(), ys.getSecond())));
//
//        DRes<ArrayList<DRes<SFixed>>> errors = seq.seq(sub ->
//          FixedLinearAlgebra.using(sub).mult(DRes.of(MatrixUtils.transpose(x)), DRes.of(x))).seq(
//            (sub, m) ->
//              AdvancedLinearAlgebra.using(sub).moorePenrosePseudoInverse(m)
//        ).par((sub, m) -> {
//          FixedNumeric fixedNumeric = FixedNumeric.using(sub);
//            return DRes.of(VectorUtils.listBuilder(m.getHeight(), i -> fixedNumeric.mult(sAndR.out().getFirst(), m.getRow(i).get(i))));
//          });
      return state;
    }).seq((seq, s) -> DRes.of(new LinearRegressionResult(s)));
  }

  private static class State implements DRes<State> {

    public DRes<SFixed> adjustedRSquared;
    public DRes<ArrayList<DRes<SFixed>>> yHat;
    private DRes<SFixed> mean;
    private DRes<ArrayList<DRes<SFixed>>> estimates;
    private DRes<SFixed> ssm, sse, sst;
    private DRes<SFixed> F;
    private DRes<SFixed> rSquared;
    private DRes<SFixed> errorVariance;
    private DRes<List<DRes<SFixed>>> errors;

    @Override
    public State out() {
      return this;
    }
  }

  public static class LinearRegressionResult {

    private final List<DRes<SFixed>> beta;
    private final DRes<SFixed> errorVariance;
    private final DRes<SFixed> rSquared;
    private final List<DRes<SFixed>> errors;
    private final DRes<SFixed> f;
    private final DRes<SFixed> adjustedRSquared;

    private LinearRegressionResult(List<DRes<SFixed>> beta, DRes<SFixed> errorVariance, List<DRes<SFixed>> errors, DRes<SFixed> rSquared, DRes<SFixed> correctedRSquared, DRes<SFixed> f) {
      this.beta = beta;
      this.errorVariance = errorVariance;
      this.errors = errors;
      this.rSquared = rSquared;
      this.adjustedRSquared = correctedRSquared;
      this.f = f;
    }

    public LinearRegressionResult(State state) {
      this(state.estimates.out(), state.errorVariance, state.errors.out(), state.rSquared, state.adjustedRSquared, state.F);
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

    /** The corrected coefficient of determination (R<sup>2</sup>) */
    public DRes<SFixed> getAdjustedRSquared() {
      return adjustedRSquared;
    }

    /** Standard errors (squared) for each coefficient estimate */
    public List<DRes<SFixed>> getStdErrorsSquared() {
      return errors;
    }

    /** The F test statistics for null hypothesis that all coefficients are simultaneously zero */
    public DRes<SFixed> getFTestStatistics() {
      return f;
    }

  }
}
