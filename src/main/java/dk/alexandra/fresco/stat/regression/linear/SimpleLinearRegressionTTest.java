package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * Test for the null hypothesis <i>H<sub>0</sub>: &beta; = &beta;<sub>0</sub></i> where <i>&beta;</i>
 * is a coefficient in a linear model. The result is <i>t</i>-distributed with <i>v = n-2</i> degrees
 * of freedom, where <i>n</i> is the number of observations.
 */
public class SimpleLinearRegressionTTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> estimate;
  private final DRes<SFixed> seSquared;
  private final DRes<SFixed> b0;

  public SimpleLinearRegressionTTest(DRes<SFixed> estimate, DRes<SFixed> b0,
      DRes<SFixed> seSquared) {
    this.estimate = estimate;
    this.seSquared = seSquared;
    this.b0 = b0;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SFixed> se = AdvancedFixedNumeric.using(seq).sqrt(seSquared);
      return FixedNumeric.using(seq).div(FixedNumeric.using(seq).sub(estimate, b0), se);
    });
  }
}
