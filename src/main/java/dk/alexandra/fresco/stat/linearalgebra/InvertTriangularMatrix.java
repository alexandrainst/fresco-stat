package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Invert lower triangular matrix.
 */
public class InvertTriangularMatrix implements
    Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> l;

  public InvertTriangularMatrix(Matrix<DRes<SFixed>> l) {
    assert (l.getHeight() == l.getWidth());
    assert (l.getHeight() >= 1);
    this.l = l;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(par);
      List<DRes<SFixed>> diagonalInverses = VectorUtils
          .listBuilder(l.getHeight(), i -> advancedFixedNumeric.reciprocal(l.getRow(i).get(i)));
      return DRes.of(diagonalInverses);
    }).par((par, diagonalInverses) -> {
      List<DRes<List<DRes<SFixed>>>> inverse = new ArrayList<>();
      for (int i = 0; i < l.getHeight(); i++) {
        DRes<List<DRes<SFixed>>> column = new ForwardSubstitution(l, diagonalInverses, i)
            .buildComputation(par);
        inverse.add(column);
      }
      return DRes.of(inverse);
    }).seq((seq, inverse) -> {
      ArrayList<ArrayList<DRes<SFixed>>> unfolded = new ArrayList<>();
      for (int i = 0; i < l.getHeight(); i++) {
        ArrayList<DRes<SFixed>> row = new ArrayList<>();
        if (i > 0) {
          row.addAll(Collections.nCopies(i, FixedNumeric.using(seq).known(0)));
        }
        row.addAll(inverse.get(i).out());
        unfolded.add(row);
      }
      Matrix<DRes<SFixed>> out = new Matrix<>(l.getHeight(), l.getWidth(), unfolded);

      // We have constructed the matrix from columns but the representation here is in rows, so we need
      // to transpose the result.
      return FixedLinearAlgebra.using(seq).transpose(DRes.of(out));
    });
  }

  /**
   * Use forward substitution to compute a vector x such that Lx = e_k, where L is lower triangular,
   * e_k is a vector with all zeros and 1 on the k'th coordinate. The list reciprocals contains the
   * reciprocals of all the diagonal entries of L.
   */
  private static class ForwardSubstitution implements
      Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

    private final Matrix<DRes<SFixed>> l;
    private final List<DRes<SFixed>> reciprocals;
    private final int k;

    private ForwardSubstitution(Matrix<DRes<SFixed>> l,
        List<DRes<SFixed>> reciprocals, int k) {
      this.l = l;
      this.reciprocals = reciprocals;
      this.k = k;
    }

    @Override
    public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.seq(seq -> DRes.of(new ArrayList<>(List.of(reciprocals.get(k)))))
          .whileLoop(x -> x.size() < l.getHeight() - k, (b, x) -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(b);

            int i = x.size();
            DRes<SFixed> sum = AdvancedFixedNumeric.using(b).innerProduct(
                l.getRow(k + i).subList(k, k + i), x);

            ArrayList<DRes<SFixed>> newX = new ArrayList<>(x);
            newX.add(fixedNumeric.mult(fixedNumeric.sub(0, sum), reciprocals.get(k + i)));

            return DRes.of(newX);
          }).seq((seq, x) -> DRes.of(x));
    }
  }
}
