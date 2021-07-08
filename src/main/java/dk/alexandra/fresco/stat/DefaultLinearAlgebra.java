package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.BackSubstitution;
import dk.alexandra.fresco.stat.linearalgebra.ForwardSubstitution;
import dk.alexandra.fresco.stat.linearalgebra.GramSchmidt;
import dk.alexandra.fresco.stat.linearalgebra.InvertTriangularMatrix;
import dk.alexandra.fresco.stat.linearalgebra.LinearInverseProblem;
import dk.alexandra.fresco.stat.linearalgebra.MoorePenrosePseudoInverse;
import dk.alexandra.fresco.stat.linearalgebra.NormalizeVector;
import dk.alexandra.fresco.stat.linearalgebra.Projection;
import dk.alexandra.fresco.stat.linearalgebra.QRAlgorithm;
import dk.alexandra.fresco.stat.linearalgebra.QRDecomposition;
import java.util.ArrayList;
import java.util.List;

public class DefaultLinearAlgebra implements AdvancedLinearAlgebra {

  private final ProtocolBuilderNumeric builder;

  DefaultLinearAlgebra(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> backSubstitution(Matrix<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> b) {
    return new BackSubstitution(a, b).buildComputation(builder);
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> forwardSubstitution(Matrix<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> b) {
    return new ForwardSubstitution(a, b).buildComputation(builder);
  }

  @Override
  public DRes<List<ArrayList<DRes<SFixed>>>> gramSchmidt(List<ArrayList<DRes<SFixed>>> vectors) {
    return new GramSchmidt(vectors).buildComputation(builder);
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> invertLowerTriangularMatrix(Matrix<DRes<SFixed>> l) {
    return new InvertTriangularMatrix(l).buildComputation(builder);
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> linearInverseProblem(Matrix<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> b) {
    return new LinearInverseProblem(a, b).buildComputation(builder);
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> moorePenrosePseudoInverse(Matrix<DRes<SFixed>> a) {
    return new MoorePenrosePseudoInverse(a).buildComputation(builder);
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> normalizeVector(ArrayList<DRes<SFixed>> u) {
    return new NormalizeVector(u).buildComputation(builder);
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> projection(ArrayList<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> u) {
    return new Projection(a, u).buildComputation(builder);
  }

  @Override
  public DRes<List<DRes<SFixed>>> iterativeEigenvalues(Matrix<DRes<SFixed>> a, int iterations) {
    return new QRAlgorithm(a, iterations).buildComputation(builder);
  }

  @Override
  public DRes<Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>>> qrDecomposition(
      Matrix<DRes<SFixed>> a) {
    return new QRDecomposition(a).buildComputation(builder);
  }
}
