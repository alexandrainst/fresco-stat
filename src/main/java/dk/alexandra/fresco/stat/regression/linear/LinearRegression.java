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
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import dk.alexandra.fresco.stat.linearalgebra.InvertUpperTriangularMatrix;
import dk.alexandra.fresco.stat.linearalgebra.LinearInverseProblem;
import dk.alexandra.fresco.stat.linearalgebra.QRDecomposition;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Fit a linear model to the given dataset and output estimates for the coefficients and the
 * regression error variance (s<sup>2</sup>) which is equal to the regression standard error
 * squared, the coefficient of determination (R<sup>2</sup>) and the standard errors (squared) for
 * each coefficient estimate. (see {@link LinearRegressionResult}.
 */
public class LinearRegression implements
    Computation<LinearRegressionResult, ProtocolBuilderNumeric> {

  private final List<ArrayList<DRes<SFixed>>> observations;
  private final int n;
  private final int p;
  private final ArrayList<DRes<SFixed>> y;

  public LinearRegression(List<ArrayList<DRes<SFixed>>> observations, ArrayList<DRes<SFixed>> y) {
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
    return builder.seq(new QRDecomposition(x))
        .seq((seq, qr) -> {
          state.qr = qr;
          return new InvertUpperTriangularMatrix(state.qr.getSecond())
              .buildComputation(seq);
        }).seq((seq, rInverse) -> {
            FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(seq);
            state.qInverse = fixedLinearAlgebra
                .mult(DRes.of(rInverse), DRes.of(MatrixUtils.transpose(rInverse)));
            FixedLinearAlgebra la = FixedLinearAlgebra.using(seq);
          state.estimates = la.vectorMult(la.mult(state.qInverse, DRes.of(MatrixUtils.transpose(x))), DRes.of(y));

          return state;
        }).par((par, s) -> {
          state.yHat = FixedLinearAlgebra.using(par).vectorMult(DRes.of(x), state.estimates);
          state.mean = par.seq(new SampleMean(y));
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
            (seq, s) -> FixedNumeric.using(seq).div(state.sse, n - p),
            (seq, s) -> FixedNumeric.using(seq).div(state.ssm, state.sst)
        ).par((par, errorVarianceAndSampleCorrelation) -> {
          state.errorVariance = errorVarianceAndSampleCorrelation.getFirst();
          state.rSquared = errorVarianceAndSampleCorrelation.getSecond();
          state.adjustedRSquared = par.seq(seq ->
              FixedNumeric.using(seq).sub(1, FixedNumeric.using(seq)
                  .mult((double) (n - 1) / (n - p), FixedNumeric.using(seq).sub(1, state.rSquared)))
          );

          // Compute std errors (squared) for all estimates and t-test statistics
          par.seq(sub -> {

            state.errors = sub.seq(seq -> new InvertUpperTriangularMatrix(state.qr.getSecond())
                .buildComputation(seq)).seq((seq, rInverse) -> {
              FixedLinearAlgebra fixedLinearAlgebra = FixedLinearAlgebra.using(seq);
              return fixedLinearAlgebra
                  .mult(DRes.of(rInverse), DRes.of(MatrixUtils.transpose(rInverse)));
            }).par((s, qInverse) -> DRes.of(VectorUtils.listBuilder(qInverse.getHeight(),
                i -> s.seq(sub2 ->
                    AdvancedFixedNumeric.using(sub2).sqrt(
                        FixedNumeric.using(sub2).mult(state.errorVariance, qInverse.getRow(i).get(i)))))));

            state.t = sub.par(sub2 -> DRes.of(VectorUtils.listBuilder(p, i ->
                FixedNumeric.using(sub2)
                    .div(state.estimates.out().get(i), state.errors.out().get(i)))));

            return null;

          });

          state.F = par.seq(sub ->
              FixedNumeric.using(sub).div(state.ssm, state.sse)
          ).seq((sub, q) -> FixedNumeric.using(sub).mult((double) (n - p) / (p - 1), q));

          return state;

        }).seq((seq, s) -> new LinearRegressionResult(s));
  }

  private static class State implements DRes<State> {

    public DRes<SFixed> adjustedRSquared;
    public DRes<ArrayList<DRes<SFixed>>> yHat;
    public Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>> qr;
    public DRes<ArrayList<DRes<SFixed>>> t;
    public DRes<Matrix<DRes<SFixed>>> qInverse;
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

  public static class LinearRegressionResult implements DRes<LinearRegressionResult> {

    private final List<DRes<SFixed>> beta;
    private final DRes<SFixed> errorVariance;
    private final DRes<SFixed> rSquared;
    private final List<DRes<SFixed>> errors;
    private final DRes<SFixed> f;
    private final DRes<SFixed> adjustedRSquared;
    private final List<DRes<SFixed>> t;

    private LinearRegressionResult(List<DRes<SFixed>> beta, DRes<SFixed> errorVariance,
        List<DRes<SFixed>> errors, DRes<SFixed> rSquared, DRes<SFixed> correctedRSquared,
        DRes<SFixed> f, List<DRes<SFixed>> t) {
      this.beta = beta;
      this.errorVariance = errorVariance;
      this.errors = errors;
      this.rSquared = rSquared;
      this.adjustedRSquared = correctedRSquared;
      this.f = f;
      this.t = t;
    }

    public LinearRegressionResult(State state) {
      this(state.estimates.out(), state.errorVariance, state.errors.out(), state.rSquared,
          state.adjustedRSquared, state.F, state.t.out());
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

    @Override
    public LinearRegressionResult out() {
      return this;
    }
  }
}
