package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

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
