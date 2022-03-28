package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.util.ArrayList;
import java.util.List;

/** Compute the outer product of a vector with it self */
public class OuterProductWithItself implements Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> a;

  public OuterProductWithItself(List<DRes<SFixed>> a) {
    this.a = a;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.par(par -> {
      List<List<DRes<SFixed>>> upperTriangular = new ArrayList<>();

      for (int i = 0; i < a.size(); i++) {
        List<DRes<SFixed>> row = new ArrayList<>();
        for (int j = 0; j <= i; j++) {
          row.add(FixedNumeric.using(par).mult(a.get(i), a.get(j)));
        }
        upperTriangular.add(row);
      }
      return DRes.of(upperTriangular);
    }).seq((seq, upper) -> DRes.of(MatrixUtils.buildMatrix(a.size(), a.size(), (i,j) -> {
      if (j <= i) {
        return upper.get(i).get(j);
      } else {
        return upper.get(j).get(i);
      }
    })));

  }
}
