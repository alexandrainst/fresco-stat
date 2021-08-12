package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute the QR-decomposition of an <i>mxn</i>-matrix A with <i>m &ge; n</i> and full column rank.
 * The QR-decomposition is a pair of matrices <i>(Q,R)</i> with <i>A = QR</i> and where <i>Q</i> is
 * an <i>mxn</i>-matrix with orthonormal columns and <i>R</i> is an upper-triangular
 * <i>nxn</i>-matrix.
 */
public class QRDecomposition implements
    Computation<Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;

  public QRDecomposition(Matrix<DRes<SFixed>> a) {
    assert (a.getHeight() >= a.getWidth());
    this.a = a;
  }

  @Override
  public DRes<Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      List<ArrayList<DRes<SFixed>>> columns = VectorUtils.listBuilder(a.getWidth(), a::getColumn);
      return new GramSchmidt(columns).buildComputation(seq);
    }).par((par, gs) -> {
      AdvancedLinearAlgebra linearAlgebra = AdvancedLinearAlgebra.using(par);
      List<DRes<ArrayList<DRes<SFixed>>>> normalized = gs.stream()
          .map(linearAlgebra::normalizeVector).collect(
              Collectors.toList());
      return DRes.of(normalized);
    }).par((par, gs) -> {
      FixedNumeric numeric = FixedNumeric.using(par);
      AdvancedFixedNumeric advanced = AdvancedFixedNumeric.using(par);
      Matrix<DRes<SFixed>> r = MatrixUtils.buildMatrix(a.getWidth(), a.getWidth(), (i, j) -> {
        if (i > j) {
          return numeric.known(0);
        } else {
          return advanced.innerProduct(gs.get(i).out(), a.getColumn(j));
        }
      });

      Matrix<DRes<SFixed>> q = MatrixUtils
          .buildMatrix(a.getHeight(), a.getWidth(), (i, j) -> gs.get(j).out().get(i));
      return Pair.lazy(q, r);
    });
  }
}
