package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;

/**
 * This computation samples from an exponential distribution with rate <i>1/lambda</i> and location
 * 0.
 */
public class SampleExponentialDistribution implements Computation<SFixed, ProtocolBuilderNumeric> {

  private DRes<SFixed> lambdaInverse;
  private BigDecimal lambdaInverseKnown;

  public SampleExponentialDistribution(DRes<SFixed> b) {
    this.lambdaInverse = b;
  }

  public SampleExponentialDistribution(BigDecimal b) {
    this.lambdaInverseKnown = b;
  }

  public SampleExponentialDistribution(double b) {
    this(BigDecimal.valueOf(b));
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SFixed> uniform = new SampleUniformDistribution().buildComputation(builder);
      return AdvancedFixedNumeric.using(builder).log(uniform);
    }).seq((builder, logUniform) -> {
      DRes<SInt> negated = builder.numeric().mult(-1, logUniform.out().getSInt());
      logUniform = new SFixed(negated);

      if (lambdaInverseKnown != null) {
        return FixedNumeric.using(builder).mult(lambdaInverseKnown, logUniform);
      } else {
        return FixedNumeric.using(builder).mult(lambdaInverse, logUniform);
      }
    });

  }

}
