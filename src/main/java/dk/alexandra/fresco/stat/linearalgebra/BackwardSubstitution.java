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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Use forward substitution to compute a vector x such that ax = b, where a is upper triangular
 * square matrix.
 */
public class BackwardSubstitution implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;
  private final List<DRes<SFixed>> b;
  private final Optional<List<DRes<SFixed>>> precomputedReciprocals;

  public BackwardSubstitution(Matrix<DRes<SFixed>> a,
      List<DRes<SFixed>> b, List<DRes<SFixed>> precomputedReciprocals) {
    assert(a.getHeight() == a.getWidth());
    this.a = a;
    this.b = b;
    this.precomputedReciprocals = Optional.ofNullable(precomputedReciprocals);
  }

  public BackwardSubstitution(Matrix<DRes<SFixed>> a,
      List<DRes<SFixed>> b) {
    this(a, b, null);
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    int n = a.getHeight();

    return builder.par(par -> {

      if (precomputedReciprocals.isPresent()) {
        return DRes.of(precomputedReciprocals.get());
      }

      // The reciprocals of the diagonal entries may be computed in parallel
      AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(par);
      ArrayList<DRes<SFixed>> reciprocals = new ArrayList<>();
      for (int i = 0; i < a.getHeight(); i++) {
        reciprocals.add(advancedFixedNumeric.reciprocal(a.getRow(i).get(i)));
      }

      return DRes.of(reciprocals);
    }).seq(
        (seq, reciprocals) -> {
          DRes<SFixed> x0 = FixedNumeric.using(seq).mult(b.get(n-1), reciprocals.get(n-1));
          return Pair.lazy(reciprocals, new ArrayList<>(Collections.singleton(x0)));
        })
        .whileLoop(pair -> pair.getSecond().size() < a.getHeight(), (seq, pair) -> {

          ArrayList<DRes<SFixed>> x = pair.getSecond();
          List<DRes<SFixed>> reciprocals = pair.getFirst();

          // We add one element per step, so the iteration count is the size of the vector so far
          int i = n - x.size() - 1;

          DRes<SFixed> sum = AdvancedFixedNumeric.using(seq).innerProduct(
              a.getRow(i).subList(i + 1, n), x);

          FixedNumeric fixedNumeric = FixedNumeric.using(seq);
          ArrayList<DRes<SFixed>> newX = new ArrayList<>(x);
          newX.add(0, fixedNumeric.mult(fixedNumeric.sub(b.get(i), sum), reciprocals.get(i)));

          return Pair.lazy(reciprocals, newX);
        }).seq((seq, pair) -> pair::getSecond);
  }
}