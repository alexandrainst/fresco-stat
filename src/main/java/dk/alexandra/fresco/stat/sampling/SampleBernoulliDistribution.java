package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;

/**
 * Sample a number from a Bernoulli distribution which is 0 with probability <i>p</i> and 1 with
 * probability <i>1-p</i>.
 */
public class SampleBernoulliDistribution implements Computation<SInt, ProtocolBuilderNumeric> {

  private DRes<SFixed> p;
  private Double knownP;

  public SampleBernoulliDistribution(double p) {
    this.knownP = p;
  }

  public SampleBernoulliDistribution(DRes<SFixed> p) {
    this.p = p;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      FixedNumeric numeric = FixedNumeric.using(builder);
      if (knownP != null) {
        p = numeric.known(BigDecimal.valueOf(knownP));
      }

      DRes<SFixed> draw = new SampleUniformDistribution().buildComputation(builder);
      return numeric.leq(p, draw);
    });
  }

}
