package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Convolution implements Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final ArrayList<DRes<SFixed>> a, b;

  public Convolution(ArrayList<DRes<SFixed>> a, ArrayList<DRes<SFixed>> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric root) {

    int m = a.size();
    int n = b.size();

    return root.par(par -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      List<List<DRes<SFixed>>> products = new ArrayList<>();
      for (int k = 1; k < m + n; k++) {
        List<DRes<SFixed>> pk = new ArrayList<>();
        for (int u = Math.max(0, k - n); u < Math.min(k, m); u++) {
          pk.add(fixedNumeric.mult(a.get(u), b.get(k - u - 1)));
        }
        products.add(pk);
      }
      return DRes.of(products);
    }).par((par, products) -> {
      AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(par);
      return DRes.of(products.stream().map(advancedFixedNumeric::sum).collect(Collectors.toCollection(ArrayList::new)));
    });
  }
}
