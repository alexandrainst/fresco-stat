package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;

/**
 * Compute the sigmoid (logistic) function <i>f(x) = 1 / (1 + e<sup>-x</sup>)</i>.
 */
public class Sigmoid implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;

  public Sigmoid(DRes<SFixed> x) {
    this.x = x;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SFixed> exp = AdvancedFixedNumeric.using(seq).exp(FixedNumeric.using(seq).sub(0.0, x));
      return AdvancedFixedNumeric.using(seq)
          .reciprocal(FixedNumeric.using(seq).add(BigDecimal.ONE, exp));
    });
  }
}
