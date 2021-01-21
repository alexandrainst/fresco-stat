package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

/**
 * Use forward substitution to compute a vector x such that ax = b, where a is lower triangular.
 */
public class ForwardSubstitution implements
    Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;
  private final List<DRes<SFixed>> b;

  public ForwardSubstitution(Matrix<DRes<SFixed>> a,
      List<DRes<SFixed>> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(par);
      List<DRes<SFixed>> reciprocals = new ArrayList<>();
      for (int i = 0; i < a.getHeight(); i++) {
        reciprocals.add(advancedFixedNumeric.reciprocal(a.getRow(i).get(i)));
      }
      return () -> reciprocals;
    }).seq(
        (seq, reciprocals) -> Pair.lazy(reciprocals, new ArrayList<>(List.of(reciprocals.get(0)))))
        .whileLoop(pair -> pair.getSecond().size() < a.getHeight(), (seq, pair) -> {
          FixedNumeric fixedNumeric = FixedNumeric.using(seq);
          List<DRes<SFixed>> x = pair.getSecond();
          List<DRes<SFixed>> reciprocals = pair.getFirst();
          int i = x.size();

          DRes<SFixed> sum = AdvancedFixedNumeric.using(seq).innerProduct(
              a.getRow(i).subList(0, i), pair.getSecond());

          ArrayList<DRes<SFixed>> newX = new ArrayList<>(x);
          newX.add(fixedNumeric.mult(fixedNumeric.sub(b.get(i), sum), reciprocals.get(i)));

          return Pair.lazy(reciprocals, newX);
        }).seq((seq, pair) -> pair::getSecond);
  }
}