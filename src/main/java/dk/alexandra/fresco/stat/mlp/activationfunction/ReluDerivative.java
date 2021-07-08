package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

/**
 * Compute the derivative of the {@link Relu} function, eg. <i>f'(x) = 1</i> if <i>x &gt; 0</i> and <i>f(x) = 0</i>
 * otherwise.
 */
public class ReluDerivative implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;

  public ReluDerivative(DRes<SFixed> x) {
    this.x = x;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(seq -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      return fixedNumeric
          .fromSInt(fixedNumeric.leq(fixedNumeric.known(0.0), x));
    });
  }
}
