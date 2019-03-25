package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;

/**
 * Sample a number from an approximately standard normal distribution. The actual distribution is
 * Irwin-Hall with n = 12 subtracted by 6.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SampleNormalDistribution implements Computation<SReal, ProtocolBuilderNumeric> {

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.realNumeric().sub(new SampleIrwinHallDistribution(12).buildComputation(builder),
        BigDecimal.valueOf(6.0));
  }

}

