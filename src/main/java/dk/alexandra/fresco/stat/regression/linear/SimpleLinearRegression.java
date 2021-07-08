package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.helpers.SP;
import dk.alexandra.fresco.stat.descriptive.helpers.USS;
import dk.alexandra.fresco.stat.regression.linear.SimpleLinearRegression.SimpleLinearRegressionResult;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * This computation returns coefficients a and b based on a simple linear regression of the observed
 * x and y values.
 */
public class SimpleLinearRegression implements
    Computation<SimpleLinearRegressionResult, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> x;
  private final List<DRes<SFixed>> y;
  private final boolean computeErrors, computeCorrelation;

  public SimpleLinearRegression(List<DRes<SFixed>> x, List<DRes<SFixed>> y) {
    this(x, y, true, true);
  }

  public SimpleLinearRegression(List<DRes<SFixed>> x, List<DRes<SFixed>> y,
      boolean computeErrors, boolean computeCorrelation) {
    if (x.size() != y.size()) {
      throw new IllegalArgumentException("Lists must have same size");
    }
    this.x = x;
    this.y = y;
    this.computeErrors = computeErrors;
    this.computeCorrelation = computeCorrelation;
  }

  @Override
  public DRes<SimpleLinearRegressionResult> buildComputation(ProtocolBuilderNumeric root) {
    int n = x.size();
    return root.par(builder -> {
      DRes<SFixed> Sx = AdvancedFixedNumeric.using(builder).sum(x);
      DRes<SFixed> Sxx = builder.seq(new USS(x));
      DRes<SFixed> Sy = AdvancedFixedNumeric.using(builder).sum(y);
      DRes<SFixed> Syy = computeErrors || computeErrors ? builder.seq(new USS(y)) : null;
      DRes<SFixed> Sxy = builder.seq(new SP(x, y));
      return new State(Sx, Sxx, Sy, Syy, Sxy);
    }).par((par, state) -> {
      state.covxy = par.seq(seq -> {
        FixedNumeric numeric = FixedNumeric.using(seq);
        return numeric.sub(numeric.mult(n, state.sxy), numeric
            .mult(state.sx, state.sy));
        });
      state.varx = par.seq(seq -> {
        FixedNumeric numeric = FixedNumeric.using(seq);
        return numeric.sub(numeric.mult(n, state.sxx), numeric
            .mult(state.sx, state.sx));
      });
      if (computeErrors || computeCorrelation) {
        state.vary = par.seq(seq -> {
          FixedNumeric numeric = FixedNumeric.using(seq);
          return numeric.sub(numeric.mult(n, state.syy), numeric
              .mult(state.sy, state.sy));
        });
      }
      return state;
    }).seq((seq, state) -> {
      FixedNumeric numeric = FixedNumeric.using(seq);
      state.beta = numeric.div(state.covxy, state.varx);
      state.alpha = numeric
          .mult(1.0 / n, numeric.sub(state.sy, numeric.mult(state.beta, state.sx)));
      return state;
    }).seq((seq, state) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      if (computeErrors) {
        DRes<SFixed> se = fixedNumeric.mult(1.0 / (n * (n - 2)), fixedNumeric.sub(state.vary,
            fixedNumeric.mult(fixedNumeric.mult(state.beta, state.beta), state.varx)));
        state.errorBeta = fixedNumeric.div(fixedNumeric.mult(n, se), state.varx);
        state.errorAlpha = fixedNumeric.mult(1.0 / n, fixedNumeric.mult(state.errorBeta, state.sxx));
      }

      if (computeCorrelation) {
        state.correlation = fixedNumeric.div(fixedNumeric.mult(state.covxy, state.covxy), fixedNumeric.mult(state.varx, state.vary));
      }
      return DRes.of(new SimpleLinearRegressionResult(state));
    });

  }

  private static class State implements DRes<State> {

    private final DRes<SFixed> sx, sxx, sy, syy, sxy;
    private DRes<SFixed> beta, alpha, errorBeta, errorAlpha, correlation, varx, vary, covxy;

    State(DRes<SFixed> sx, DRes<SFixed> sxx, DRes<SFixed> sy, DRes<SFixed> syy, DRes<SFixed> sxy) {
      this.sx = sx;
      this.sxx = sxx;
      this.sy = sy;
      this.syy = syy;
      this.sxy = sxy;
    }

    @Override
    public State out() {
      return this;
    }
  }

  public static class SimpleLinearRegressionResult {

    private final DRes<SFixed> alpha, beta, errorAlpha, errorBeta, correlation;

    private SimpleLinearRegressionResult(State state) {
      this.alpha = state.alpha;
      this.beta = state.beta;
      this.errorAlpha = state.errorAlpha;
      this.errorBeta = state.errorBeta;
      this.correlation = state.correlation;
    }

    public DRes<SFixed> getAlpha() {
      return alpha;
    }

    public DRes<SFixed> getBeta() {
      return beta;
    }

    public DRes<SFixed> getErrorAlphaSquared() {
      return errorAlpha;
    }

    public DRes<SFixed> getErrorBetaSquared() {
      return errorBeta;
    }

    public DRes<SFixed> getRSquared() {
      return correlation;
    }
  }

}
