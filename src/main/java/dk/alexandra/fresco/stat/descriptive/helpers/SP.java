package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

/** Calculate the sum of products (aka the dot product) of two samples */
public class SP implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> x, y;

  public SP(List<DRes<SFixed>> x, List<DRes<SFixed>> y) {
    if (x.size() != y.size()) {
      throw new IllegalArgumentException("Lists must have same size");
    }
    this.x = x;
    this.y = y;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      List<DRes<SFixed>> products = new ArrayList<>();
      for (int i = 0; i < x.size(); i++) {
        products.add(fixedNumeric.mult(x.get(i), y.get(i)));
      }
      return DRes.of(products);
    }).seq((seq, products) -> AdvancedFixedNumeric.using(seq).sum(products));
  }
}
