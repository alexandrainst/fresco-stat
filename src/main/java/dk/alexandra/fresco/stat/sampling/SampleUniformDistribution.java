package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * Sample a number uniformly in the interval <i>[0,1)</i>.
 */
public class SampleUniformDistribution implements Computation<SFixed, ProtocolBuilderNumeric> {

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return AdvancedFixedNumeric.using(builder).random();
  }

}
