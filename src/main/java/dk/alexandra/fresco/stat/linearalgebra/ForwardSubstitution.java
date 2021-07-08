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
import java.util.Collections;

/**
 * Use forward substitution to compute a vector x such that ax = b, where a is lower triangular
 * square matrix.
 */
public class ForwardSubstitution implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;
  private final ArrayList<DRes<SFixed>> b;

  public ForwardSubstitution(Matrix<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> b) {
    assert (a.getHeight() == a.getWidth());
    assert (a.getWidth() == b.size());
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {

      // The reciprocals of the diagonal entries may be computed in parallel
      AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(par);
      ArrayList<DRes<SFixed>> reciprocals = new ArrayList<>();
      for (int i = 0; i < a.getHeight(); i++) {
        reciprocals.add(advancedFixedNumeric.reciprocal(a.getRow(i).get(i)));
      }

      return DRes.of(reciprocals);
    }).seq(
        (seq, reciprocals) -> {
          DRes<SFixed> x0 = FixedNumeric.using(seq).mult(b.get(0), reciprocals.get(0));
          return Pair.lazy(reciprocals, new ArrayList<>(Collections.singleton(x0)));
        }).whileLoop(pair -> pair.getSecond().size() < a.getHeight(), (seq, pair) -> {

      ArrayList<DRes<SFixed>> x = pair.getSecond();
      ArrayList<DRes<SFixed>> reciprocals = pair.getFirst();

      // We add one element per step, so the iteration count is the size of the vector so far
      int i = x.size();

      DRes<SFixed> sum = AdvancedFixedNumeric.using(seq).innerProduct(
          a.getRow(i).subList(0, i), x);

      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      ArrayList<DRes<SFixed>> newX = new ArrayList<>(x);
      newX.add(fixedNumeric.mult(fixedNumeric.sub(b.get(i), sum), reciprocals.get(i)));

      return Pair.lazy(reciprocals, newX);
    }).seq((seq, pair) -> pair::getSecond);
  }
}