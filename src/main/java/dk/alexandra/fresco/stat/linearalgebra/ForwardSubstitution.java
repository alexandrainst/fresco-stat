package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

/**
 * Use forward substitution to compute a vector x such that ax = b, where a is lower
 * triangular.
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
    return builder.seq(seq -> {
      // TODO: Precompute reciprocals in parallel instead of division
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      DRes<SFixed> x0 = fixedNumeric.div(b.get(0), a.getRow(0).get(0));
      return () -> new ArrayList<>(List.of(x0));
    }).whileLoop(x -> x.size() < a.getHeight(), (seq, x) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      int i = x.size();

      DRes<SFixed> sum = AdvancedFixedNumeric.using(seq).innerProduct(
          a.getRow(i).subList(0, i), x);

      ArrayList<DRes<SFixed>> newX = new ArrayList<>(x);
      newX.add(fixedNumeric.div(fixedNumeric.sub(b.get(i), sum), a.getRow(i).get(i)));

      return () -> newX;
    }).seq((seq, x) -> () -> x);
  }
}