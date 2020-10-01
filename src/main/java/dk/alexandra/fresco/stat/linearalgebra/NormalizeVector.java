package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

public class NormalizeVector implements
    Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> u;

  public NormalizeVector(List<DRes<SFixed>> u) {
    this.u = u;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SFixed> scale = AdvancedFixedNumeric.using(seq)
          .sqrt(AdvancedFixedNumeric.using(seq).innerProduct(u, u));
      List<DRes<SFixed>> result = VectorUtils.div(u, scale, seq);
      return () -> result;
    });
  }
}
