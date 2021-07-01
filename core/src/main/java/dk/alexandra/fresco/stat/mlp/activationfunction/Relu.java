package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.utils.MultiplyWithSInt;

/**
 * Compute the rectified linear function <i>f(x) = x</i> if <i>x &gt; 0</i> and <i>f(x) = 0</i>
 * otherwise.
 */
public class Relu implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> x;

  public Relu(DRes<SFixed> x) {
    this.x = x;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(seq -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      return fixedNumeric.leq(x, fixedNumeric.known(0.0));
    }).seq(
        (seq, indicator) -> new MultiplyWithSInt(x, indicator).buildComputation(seq));
  }
}
