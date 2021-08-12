package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * Compute the derivative of the {@link Sigmoid} function <i>f'(x)</i>given the function value
 * <i>y = f(x)</i>in the point.
 */
public class SigmoidDerivative implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> y;

  public SigmoidDerivative(DRes<SFixed> y) {
    this.y = y;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      // f'(x) = f(x)(1 - f(x))
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      return fixedNumeric.mult(y, fixedNumeric.sub(1, y));
    });
  }
}
