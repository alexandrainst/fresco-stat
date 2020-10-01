package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

public class Projection implements
    Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> a, u;

  public Projection(List<DRes<SFixed>> a, List<DRes<SFixed>> u) {
    this.a = a;
    this.u = u;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<SFixed> nominator = AdvancedFixedNumeric.using(par).innerProduct(a, u);
      DRes<SFixed> denominator = AdvancedFixedNumeric.using(par).innerProduct(u, u);
      return Pair.lazy(nominator, denominator);
    }).seq((seq, values) -> {
      DRes<SFixed> scale = FixedNumeric.using(seq).div(values.getFirst(), values.getSecond());
      List<DRes<SFixed>> result = VectorUtils.scale(u, scale, seq);
      return () -> result;
    });
  }
}
