package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;

/**
 * Compute the projection of a vector onto another.
 */
public class Projection implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final ArrayList<DRes<SFixed>> a, u;

  public Projection(ArrayList<DRes<SFixed>> a, ArrayList<DRes<SFixed>> u) {
    this.a = a;
    this.u = u;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<SFixed> nominator = AdvancedFixedNumeric.using(par).innerProduct(a, u);
      DRes<SFixed> denominator = AdvancedFixedNumeric.using(par).innerProduct(u, u);
      return Pair.lazy(nominator, denominator);
    }).seq((seq, values) -> {
      DRes<SFixed> scale = FixedNumeric.using(seq).div(values.getFirst(), values.getSecond());
      ArrayList<DRes<SFixed>> result = VectorUtils.scale(u, scale, seq);
      return DRes.of(result);
    });
  }
}
