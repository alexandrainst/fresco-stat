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
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleSkewnessAndKurtosis;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import dk.alexandra.fresco.stat.linearalgebra.InvertUpperTriangularMatrix;
import dk.alexandra.fresco.stat.linearalgebra.QRDecomposition;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fit a linear model to the given dataset and output estimates for the coefficients and some model
 * diagnostics (see {@link LinearRegressionResult}).
 */
public class LinearRegression implements
    Computation<LinearRegressionResult, ProtocolBuilderNumeric> {

  private final List<ArrayList<DRes<SFixed>>> observations;
  private final int n;
  private final int p;
  private final ArrayList<DRes<SFixed>> y;
  private final boolean computeModelDiagnostics;

  public LinearRegression(List<ArrayList<DRes<SFixed>>> observations, ArrayList<DRes<SFixed>> y) {
    this(observations, y, true);
  }

  LinearRegression(List<ArrayList<DRes<SFixed>>> observations, ArrayList<DRes<SFixed>> y,
      boolean computeModelDiagnostics) {
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
    this.computeModelDiagnostics = computeModelDiagnostics;
  }

  @Override
  public DRes<LinearRegressionResult> buildComputation(ProtocolBuilderNumeric builder) {
    Matrix<DRes<SFixed>> x = new Matrix<>(n, p, new ArrayList<>(observations));

    State state = new State();
    return builder.seq(new QRDecomposition(x))
        .seq((seq, qr) -> {

          state.qr = qr;
          return new InvertUpperTriangularMatrix(state.qr.getSecond())
              .buildComputation(seq);

        }).seq((seq, rInverse) -> {

          FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(seq);
          state.qInverse = fixedLinearAlgebra
              .mult(DRes.of(rInverse), DRes.of(MatrixUtils.transpose(rInverse)));
          state.estimates = fixedLinearAlgebra
              .vectorMult(
                  fixedLinearAlgebra.mult(state.qInverse, DRes.of(MatrixUtils.transpose(x))),
                  DRes.of(y));
          return state;

        }).par((par, s) -> {

          state.mean = par.seq(new SampleMean(y));
          state.yHat = FixedLinearAlgebra.using(par).vectorMult(DRes.of(x), state.estimates);
          return state;

        }).par((par, s) -> {

          state.ssm = par.seq(new SSD(state.yHat.out(), state.mean));
          state.sst = par.seq(new SSD(y, state.mean));
          state.residuals = VectorUtils.sub(y, state.yHat.out(), par);
          return state;

        }).seq((b, st) -> {

          state.sse = FixedNumeric.using(b).sub(state.sst, state.ssm);

          return state;
        }).par((b, st) -> {

          if (computeModelDiagnostics) {
            // For some purposes, e.g. Breusch-Pagan test, we do not need the values computed below

            return b.par(par -> {

                // TODO: can we reuse SSE?
                state.skewnessAndKurtosis = par.seq(new SampleSkewnessAndKurtosis(state.residuals));

                // Compute regression error variance (s^2)
                state.errorVariance = FixedNumeric.using(par).div(state.sse, n - p);

                // Compute R^2
                state.rSquared = FixedNumeric.using(par).div(state.ssm, state.sst);

                // Compute the Durbin-Watson test statistics for independence of residuals
                state.durbinWatson = par.seq(new DurbinWatsonTest(state.residuals, state.sse));

              // Compute F-test statistics
              state.F = par.seq(sub ->
                  FixedNumeric.using(sub).div(state.ssm, state.sse)
              ).seq((sub, q) -> FixedNumeric.using(sub).mult((double) (n - p) / (p - 1), q));

              return state;
            }).par((par, s) -> {

                // Compute Jarque-Bera test statistics
                state.jb = par.seq(new JarqueBeraTest(n, state.skewnessAndKurtosis.out().getFirst(),
                    state.skewnessAndKurtosis.out().getSecond()));

                // Compute the adjusted R^2
                state.adjustedRSquared = par.seq(seq ->
                    FixedNumeric.using(seq).sub(1, FixedNumeric.using(seq)
                        .mult((double) (n - 1) / (n - p),
                            FixedNumeric.using(seq).sub(1, state.rSquared))));

                // Compute std errors (squared) for all estimates
                state.errors = par.seq(new InvertUpperTriangularMatrix(state.qr.getSecond())
                ).seq((seq, rInverse) -> {
                  FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(seq);
                  return fixedLinearAlgebra
                      .mult(DRes.of(rInverse), DRes.of(MatrixUtils.transpose(rInverse)));
                }).par((par2, qInverse) -> DRes.of(VectorUtils.listBuilder(qInverse.getHeight(),
                    i -> par2.seq(seq ->
                        AdvancedFixedNumeric.using(seq).sqrt(
                            FixedNumeric.using(seq)
                                .mult(state.errorVariance, qInverse.getRow(i).get(i)))))));

              // Compute the Breusch-Pagan test statistics to verify homoskedasticity of the residuals
              state.breuschPagan = par.seq(new BreuschPaganTest(observations, state.residuals, state.errorVariance));
              return state;

            }).par((par, s) -> {

                // Compute t-test statistics
                state.t = DRes.of(VectorUtils.listBuilder(p, i ->
                    FixedNumeric.using(par)
                        .div(state.estimates.out().get(i), state.errors.out().get(i))));
              return state;

            });

          } else {
            return state;
          }

        }).seq((seq, s) -> new LinearRegressionResult(s));
  }

  /** Class used to store results and intermediate results during computation */
  private static class State implements DRes<State> {

    public DRes<SFixed> breuschPagan;
    public DRes<Pair<SFixed, SFixed>> skewnessAndKurtosis;
    public DRes<SFixed> jb;
    public DRes<SFixed> omnibus;
    private DRes<SFixed> adjustedRSquared;
    private DRes<ArrayList<DRes<SFixed>>> yHat;
    private Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>> qr;
    private DRes<ArrayList<DRes<SFixed>>> t;
    private DRes<Matrix<DRes<SFixed>>> qInverse;
    private ArrayList<DRes<SFixed>> residuals;
    private DRes<SFixed> durbinWatson;
    private DRes<SFixed> mean;
    private DRes<ArrayList<DRes<SFixed>>> estimates;
    private DRes<SFixed> ssm, sse, sst;
    private DRes<SFixed> F;
    private DRes<SFixed> rSquared;
    private DRes<SFixed> errorVariance;
    private DRes<ArrayList<DRes<SFixed>>> errors;

    @Override
    public State out() {
      return this;
    }
  }

  /**
   * Instances of this class holds the result of a multiple linear regression
   */
  public static class LinearRegressionResult implements DRes<LinearRegressionResult> {

    private final List<DRes<SFixed>> beta;
    private final DRes<SFixed> errorVariance;
    private final DRes<SFixed> rSquared;
    private final List<DRes<SFixed>> errors;
    private final DRes<SFixed> f;
    private final DRes<SFixed> adjustedRSquared;
    private final List<DRes<SFixed>> t;
    private final List<DRes<SFixed>> residuals;
    private final DRes<SFixed> ssm;
    private final DRes<SFixed> durbinWatson;
    private final DRes<SFixed> breuschPagan;
    private final DRes<SFixed> skew;
    private final DRes<SFixed> kurtosis;
    private final DRes<SFixed> jarqueBera;

    private LinearRegressionResult(List<DRes<SFixed>> beta, DRes<SFixed> errorVariance,
        List<DRes<SFixed>> errors, DRes<SFixed> rSquared, DRes<SFixed> correctedRSquared,
        DRes<SFixed> f, List<DRes<SFixed>> t, List<DRes<SFixed>> residuals, DRes<SFixed> ssm,
        DRes<SFixed> durbinWatson, DRes<SFixed> breuschPagan, DRes<SFixed> skew, DRes<SFixed> kurtosis,
        DRes<SFixed> jarqueBera) {
      this.beta = beta;
      this.errorVariance = errorVariance;
      this.errors = errors;
      this.rSquared = rSquared;
      this.adjustedRSquared = correctedRSquared;
      this.f = f;
      this.t = t;
      this.residuals = residuals;
      this.ssm = ssm;
      this.durbinWatson = durbinWatson;
      this.breuschPagan = breuschPagan;
      this.skew = skew;
      this.kurtosis = kurtosis;
      this.jarqueBera = jarqueBera;
    }

    private LinearRegressionResult(State state) {
      this(state.estimates.out(),
          state.errorVariance,
          state.errors != null ? state.errors.out() : null, state.rSquared,
          state.adjustedRSquared,
          state.F, state.t != null ? state.t.out() : null,
          state.residuals,
          state.ssm,
          state.durbinWatson,
          state.breuschPagan,
          state.skewnessAndKurtosis != null ? state.skewnessAndKurtosis.out().getFirst() : null,
          state.skewnessAndKurtosis != null ? state.skewnessAndKurtosis.out().getSecond() : null,
          state.jb);
    }

    /**
     * Estimates for the coefficients
     */
    public List<DRes<SFixed>> getBeta() {
      return beta;
    }

    /**
     * The regression error variance (s<sup>2</sup>) which is equal to the regression standard error
     * squared
     */
    public DRes<SFixed> getErrorVariance() {
      return errorVariance;
    }

    /**
     * The coefficient of determination (R<sup>2</sup>)
     */
    public DRes<SFixed> getRSquared() {
      return rSquared;
    }

    /**
     * The adjusted coefficient of determination (R<sup>2</sup><sub>adj</sub>)
     */
    public DRes<SFixed> getAdjustedRSquared() {
      return adjustedRSquared;
    }

    /**
     * Standard errors for each coefficient estimate
     */
    public List<DRes<SFixed>> getStdErrors() {
      return errors;
    }

    /**
     * The F test statistics for null hypothesis that all coefficients are simultaneously zero. The
     * statistics has <i>F(p–1,n–p)</i> distribution where <i>p</i> is the number of coefficients,
     * including the intercept.
     */
    public DRes<SFixed> getFTestStatistics() {
      return f;
    }

    /**
     * The t-test statistics for the null hypothesis that each coefficient are zero.  The statistics
     * has <i>t(n - p)</i> distribution where <i>p</i> is the number of coefficients, including the
     * intercept.
     */
    public List<DRes<SFixed>> getTTestStatistics() {
      return t;
    }

    /**
     * Get the residuals of this regression.
     */
    List<DRes<SFixed>> getResiduals() {
      return residuals;
    }

    /**
     * The model sum og squares, aka the explained sum of squares
     */
    DRes<SFixed> getModelSumOfSquares() {
      return ssm;
    }

    /**
     * Test for the null hypothesis that the residuals are uncorrelated using the <a
     * href="https://en.wikipedia.org/wiki/Durbin%E2%80%93Watson_statistic">Durbin-Watson</a> test,
     * which is a necessary condition for linear regression. A rule of thumb is that values in the
     * range of 1.5 to 2.5 are relatively normal. Values outside this range could be a cause for
     * concern.
     */
    public DRes<SFixed> getDurbinWatsonTestStatistics() {
      return durbinWatson;
    }

    /**
     * Test for the null hypothesis that the residuals have equal variance, also known as homoskedasticity.
     * The test statistics is distributed as <i>&Chi;<sup>2</sup></i> with <i>p</i> degrees of freedom,
     * where <i>p</i> denotes the number of independent variables.
     */
    public DRes<SFixed> getBreuschPaganTestStatistics() {
      return breuschPagan;
    }

    public DRes<SFixed> getSkew() {
      return skew;
    }

    public DRes<SFixed> getKurtosis() {
      return kurtosis;
    }

    public DRes<SFixed> getJarqueBeraTestStatistics() {
      return jarqueBera;
    }

    @Override
    public LinearRegressionResult out() {
      return this;
    }
  }
}
