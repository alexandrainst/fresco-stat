package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;

/**
 * This computation samples from an exponential distribution with rate <i>lambda</i> and location 0.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SampleExponentialDistribution implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> lambda;
  private BigDecimal lambdaKnown;

  public SampleExponentialDistribution(DRes<SReal> b) {
    this.lambda = b;
  }

  public SampleExponentialDistribution(BigDecimal b) {
    this.lambdaKnown = b;
  }

  public SampleExponentialDistribution(double b) {
    this(BigDecimal.valueOf(b));
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SReal> uniform = new SampleUniformDistribution().buildComputation(builder);
      DRes<SReal> logUniform = builder.realAdvanced().log(uniform);

      if (lambdaKnown != null) {
        return builder.realNumeric().mult(lambdaKnown, logUniform);
      } else {
        return builder.realNumeric().mult(lambda, logUniform);
      }
    });

  }

}
