package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

public class DivideBySInt implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final DRes<SFixed> n;
  private final DRes<SInt> d;

  public DivideBySInt(DRes<SFixed> n, DRes<SInt> d) {
    this.n = n;
    this.d = d;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SInt> nAsSInt = n.out().getSInt();
      DRes<SInt> q = AdvancedNumeric.using(seq).div(nAsSInt, d);
      return new SFixed(q);
    });
  }
}
