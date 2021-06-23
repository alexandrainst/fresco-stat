package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;

/**
 * This computation multiplies the transpose of the given matrix to a vector without explicitly
 * representing the matrix in its transposed form.
 */
public class TransposedMatrixAction implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> matrix;
  private final ArrayList<DRes<SFixed>> vector;

  public TransposedMatrixAction(Matrix<DRes<SFixed>> matrix, ArrayList<DRes<SFixed>> vector) {
    this.matrix = matrix;
    this.vector = vector;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(
      ProtocolBuilderNumeric root) {
    return root.par(par -> {
      ArrayList<DRes<SFixed>> result = new ArrayList<>();
      for (int i = 0; i < matrix.getWidth(); i++) {
        result.add(AdvancedFixedNumeric.using(par).innerProduct(matrix.getColumn(i), vector));
      }
      return DRes.of(result);
    });
  }
}
