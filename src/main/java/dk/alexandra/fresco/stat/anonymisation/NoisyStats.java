package dk.alexandra.fresco.stat.anonymisation;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.Sampler;
import dk.alexandra.fresco.stat.descriptive.helpers.SP;
import dk.alexandra.fresco.stat.descriptive.helpers.USS;
import java.math.BigInteger;
import java.util.List;

/**
 * Compute differentially private estimates for the coefficients of a linear model fitted on a
 * dataset. The method is based on the paper <a href="https://arxiv.org/pdf/2007.05157.pdf">Differentially
 * Private Simple Linear Regression</a> by Alabi et. al.
 */
public class NoisyStats implements
    Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> x, y;
  private final double epsilon;

  //
  public NoisyStats(List<DRes<SFixed>> x, List<DRes<SFixed>> y, double epsilon) {
    this.x = x;
    this.y = y;
    this.epsilon = epsilon;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric root) {
    int n = x.size();
    return root.par(builder -> {
      DRes<SFixed> Sx = AdvancedFixedNumeric.using(builder).sum(x);
      DRes<SFixed> Sxx = builder.seq(new USS(x));
      DRes<SFixed> Sy = AdvancedFixedNumeric.using(builder).sum(y);
      DRes<SFixed> Sxy = builder.seq(new SP(x, y));
      return new State(Sx, Sxx, Sy, Sxy);
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
      double delta = (1.0 - 1.0 / n);
      state.L1 = Sampler.using(par).sampleLaplaceDistribution(3 * delta / epsilon);
      state.L2 = Sampler.using(par).sampleLaplaceDistribution(delta);
      return state;
    }).seq((seq, state) -> {
      FixedNumeric numeric = FixedNumeric.using(seq);
      state.denominator = numeric.add(state.varx, state.L2);
      DRes<SInt> indicator = seq.numeric().sub(1, numeric.leq(state.denominator, numeric.known(0)));
      state.indicator = seq.numeric().open(indicator);
      return state;
    }).seq((seq, state) -> {
      FixedNumeric numeric = FixedNumeric.using(seq);
      if (state.indicator.out().equals(BigInteger.ZERO)) {
        return null;
      }

      state.beta = numeric.div(numeric.add(state.covxy, state.L1), state.denominator);

      DRes<SFixed> delta3 = numeric.mult(1.0 / n, numeric
          .mult(numeric.fromSInt(AdvancedFixedNumeric.using(seq).sign(state.beta)), state.beta));
      DRes<SFixed> L3 = Sampler.using(seq)
          .sampleLaplaceDistribution(numeric.mult(3.0 / epsilon, delta3));
      state.alpha = numeric.add(numeric
          .mult(1.0 / n, numeric.sub(state.sy, numeric.mult(state.beta, state.sx))), L3);

      return DRes.of(List.of(state.alpha, state.beta));
    });

  }

  private static class State implements DRes<State> {

    private final DRes<SFixed> sx, sxx, sy, sxy;
    private DRes<SFixed> denominator;
    private DRes<BigInteger> indicator;
    private DRes<SFixed> L1, L2;
    private DRes<SFixed> beta, alpha, varx, covxy;

    State(DRes<SFixed> sx, DRes<SFixed> sxx, DRes<SFixed> sy, DRes<SFixed> sxy) {
      this.sx = sx;
      this.sxx = sxx;
      this.sy = sy;
      this.sxy = sxy;
    }

    @Override
    public State out() {
      return this;
    }
  }

}
