package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;

/**
 * Normalize a vector.
 */
public class NormalizeVector implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final ArrayList<DRes<SFixed>> u;

  public NormalizeVector(ArrayList<DRes<SFixed>> u) {
    this.u = u;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(seq);
      DRes<SFixed> scale = advancedFixedNumeric
          .sqrt(advancedFixedNumeric.innerProduct(u, u));
      ArrayList<DRes<SFixed>> result = VectorUtils.div(u, scale, seq);
      return DRes.of(result);
    });
  }
}
