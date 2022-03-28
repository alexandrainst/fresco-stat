package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/** Compute the outer product of two vectors */
public class OuterProduct implements Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> a, b;

  public OuterProduct(List<DRes<SFixed>> a, List<DRes<SFixed>> b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(par -> DRes.of(MatrixUtils.buildMatrix(a.size(), b.size(), (i,j) -> FixedNumeric.using(par).mult(a.get(i), b.get(j)))));
  }
}
