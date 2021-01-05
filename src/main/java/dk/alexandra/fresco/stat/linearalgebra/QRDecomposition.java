package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;
import java.util.stream.Collectors;

public class QRDecomposition implements
    Computation<Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> a;

  public QRDecomposition(Matrix<DRes<SFixed>> a) {
    this.a = a;
  }

  @Override
  public DRes<Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      List<List<DRes<SFixed>>> columns = VectorUtils.listBuilder(a.getHeight(), a::getColumn);
      return new GramSchmidt(columns).buildComputation(seq);
    }).par((par, gs) -> {
      List<DRes<List<DRes<SFixed>>>> normalized = gs.stream().map(v -> new NormalizeVector(v).buildComputation(par)).collect(
          Collectors.toList());
      return () -> normalized.stream().map(DRes::out).collect(Collectors.toList());
    }).par((par, gs) -> {
      FixedNumeric numeric = FixedNumeric.using(par);
      AdvancedFixedNumeric advanced = AdvancedFixedNumeric.using(par);
      Matrix<DRes<SFixed>> r = MatrixUtils.buildMatrix(a.getHeight(), a.getWidth(), (i,j) -> {
        if (i > j) {
          return numeric.known(0);
        } else {
          return advanced.innerProduct(gs.get(i), a.getColumn(j));
        }
      });

      Matrix<DRes<SFixed>> q = MatrixUtils.buildMatrix(a.getWidth(), a.getHeight(), (i,j) -> gs.get(j).get(i));
      return Pair.lazy(q, r);
    });
  }
}
