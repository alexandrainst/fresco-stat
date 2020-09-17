package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import java.math.BigDecimal;

/**
 * This computation samples from an exponential distribution with rate <i>1/lambda</i> and location
 * 0.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SampleExponentialDistribution implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> lambdaInverse;
  private BigDecimal lambdaInverseKnown;

  public SampleExponentialDistribution(DRes<SReal> b) {
    this.lambdaInverse = b;
  }

  public SampleExponentialDistribution(BigDecimal b) {
    this.lambdaInverseKnown = b;
  }

  public SampleExponentialDistribution(double b) {
    this(BigDecimal.valueOf(b));
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SReal> uniform = new SampleUniformDistribution().buildComputation(builder);
      DRes<SReal> logUniform = builder.realAdvanced().log(uniform);
      return logUniform;
    }).seq((builder, logUniform) -> {
      DRes<SInt> negated = builder.numeric().mult(-1, ((SFixed) logUniform.out()).getSInt());
      logUniform = new SFixed(negated);

      if (lambdaInverseKnown != null) {
        return builder.realNumeric().mult(lambdaInverseKnown, logUniform);
      } else {
        return builder.realNumeric().mult(lambdaInverse, logUniform);
      }
    });

  }

}
