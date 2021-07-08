package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * Sample a number from an approximately standard normal distribution. The actual distribution is
 * Irwin-Hall with n = 12 subtracted by 6.
 */
public class SampleNormalDistribution implements Computation<SFixed, ProtocolBuilderNumeric> {

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> FixedNumeric.using(builder).sub(
        new SampleIrwinHallDistribution(12).buildComputation(builder), 6));
  }

}

