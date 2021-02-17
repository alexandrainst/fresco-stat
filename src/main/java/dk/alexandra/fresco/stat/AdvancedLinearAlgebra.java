package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

public interface AdvancedLinearAlgebra {

  static AdvancedLinearAlgebra using(ProtocolBuilderNumeric builder) {
    return new DefaultLinearAlgebra(builder);
  }

  /**
   * Use back substitution to compute a vector <i>x</i> such that <i>ax = b</i> where <i>a</i> is an
   * upper triangular square matrix.
   *
   * @param a An upper triangular matrix.
   * @param b A vector.
   * @return A vector <i>x</i> such that <i>ax = b</i>.
   */
  DRes<ArrayList<DRes<SFixed>>> backSubstitution(Matrix<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> b);

  /**
   * Use forward substitution to compute a vector <i>x</i> such that <i>ax = b</i> where <i>a</i> is
   * a lower triangular square matrix.
   *
   * @param a An lower triangular matrix.
   * @param b A vector.
   * @return A vector <i>x</i> such that <i>ax = b</i>.
   */
  DRes<ArrayList<DRes<SFixed>>> forwardSubstitution(Matrix<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> b);

  /**
   * Return a list of mutually orthogonal vectors spanning the same space as the given vectors.
   *
   * @param vectors A set of linearly independent vectors.
   * @return A set of mutually orthogonal vectors spanning the same space as the input.
   */
  DRes<List<ArrayList<DRes<SFixed>>>> gramSchmidt(List<ArrayList<DRes<SFixed>>> vectors);

  /**
   * Compute the inverse of a lower triangular matrix.
   *
   * @param l A lower triangular matrix.
   * @return The inverse of <i>l</i>
   */
  DRes<Matrix<DRes<SFixed>>> invertLowerTriangularMatrix(Matrix<DRes<SFixed>> l);

  /**
   * Solve a linear inverse problem, eg. find an <i>x</i> such that <i>ax = b</i> where <i>a</i> is
   * an <i>m×n</i>-matrix and <i>b</i> is an <i>n</i>-dimensional vector. If a system is
   * overdetermined (m &ge; n), the computation will find the <i>x</i> minimising &#x7c;&#x7c;<i>ax
   * - b</i>&#x7c;&#x7c;.
   *
   * @param a An <i>m×n</i>-matrix.
   * @param b An <i>n</i>-dimensional vector.
   * @return A solution to the equation <i>ax = b</i> or a matrix minimising <i>ax - b</i>.
   */
  DRes<ArrayList<DRes<SFixed>>> linearInverseProblem(Matrix<DRes<SFixed>> a,
      ArrayList<DRes<SFixed>> b);

  /**
   * Compute the Moore-Penrose pseudo-inverse of an <i>m×n</i>-matrix with full column rank.
   *
   * @param a An <i>m×n</i>-matrix with full column rank.
   * @return The Moore-Penrose pseudo-inverse of <i>a</i>.
   */
  DRes<Matrix<DRes<SFixed>>> moorePenrosePseudoInverse(Matrix<DRes<SFixed>> a);

  /**
   * Normalize a non-zero vector.
   *
   * @param u A non-zero vector.
   * @return A vector with the same direction as <i>u</i> and magnitude 1.
   */
  DRes<ArrayList<DRes<SFixed>>> normalizeVector(ArrayList<DRes<SFixed>> u);

  /**
   * Compute the projection of a vector <i>a</i> onto another vector <i>u</i>.
   *
   * @param a A vector.
   * @param u A vector.
   * @return <i>a</i> projected onto <i>u</i>.
   */
  DRes<ArrayList<DRes<SFixed>>> projection(ArrayList<DRes<SFixed>> a, ArrayList<DRes<SFixed>> u);

  /**
   * Approximate the eigenvalues of a matrix using the QR-algorithm.
   *
   * @param a          A square matrix.
   * @param iterations The number of iterations.
   * @return An approximation of the eigenvalues of <i>a</i>.
   */
  DRes<List<DRes<SFixed>>> iterativeEigenvalues(Matrix<DRes<SFixed>> a, int iterations);

  /**
   * Compute the QR-decomposition of an <i>mxn</i>-matrix a with <i>m &ge; n</i> and full column
   * rank. The QR-decomposition is a pair of matrices <i>(Q,R)</i> with <i>A = QR</i> and where
   * <i>Q</i> is an <i>mxn</i>-matrix with orthonormal columns and <i>R</i> is an upper-triangular
   * <i>nxn</i>-matrix.
   *
   * @param a An <i>mxn</i>-matrix
   * @return A pair of matrices <i>(Q,R)</i> with <i>a = QR</i> and where * <i>Q</i> is an
   * <i>mxn</i>-matrix with orthonormal columns and <i>R</i> is an upper-triangular
   * * <i>nxn</i>-matrix.
   */
  DRes<Pair<Matrix<DRes<SFixed>>, Matrix<DRes<SFixed>>>> qrDecomposition(Matrix<DRes<SFixed>> a);

}
