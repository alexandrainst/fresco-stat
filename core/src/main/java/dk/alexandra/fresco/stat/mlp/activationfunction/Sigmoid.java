package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;

public class Sigmoid implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;

  public Sigmoid(DRes<SFixed> x) {
    this.x = x;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
//      return AdvancedFixedNumeric.using(seq).polynomialEvalutation(x,
//          0.5, 1.0 / 4.0, 0.0, -1.0 / 48.0, 0.0, 1.0 / 480, 0.0, -17.0 / 80640.0);

      DRes<SFixed> exp = AdvancedFixedNumeric.using(seq).exp(FixedNumeric.using(seq).sub(0.0, x));
      return AdvancedFixedNumeric.using(seq)
          .reciprocal(FixedNumeric.using(seq).add(BigDecimal.ONE, exp));
    });
  }
}
