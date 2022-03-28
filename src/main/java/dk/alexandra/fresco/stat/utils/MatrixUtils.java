package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixUtils {

  /**
   * Create a new matrix with the given height and width and populate it use the populator.
   */
  public static <E> Matrix<E> buildMatrix(int h, int w, MatrixPopulator<E> populator) {
    return new Matrix<>(h, w, i -> IntStream.range(0, w).mapToObj(j -> populator.apply(i, j))
        .collect(Collectors.toCollection(ArrayList::new)));
  }

  /**
   * Map a matrix to another matrix of the same size using the given function
   */
  public static <E, F> Matrix<E> map(Matrix<F> matrix, Function<F, E> function) {
    return buildMatrix(matrix.getHeight(), matrix.getWidth(),
        (i, j) -> function.apply(matrix.getRow(i).get(j)));
  }

  /**
   * Return a new matrix equal to the transpose of the given matrix
   */
  public static <E> Matrix<E> transpose(Matrix<E> a) {
    return buildMatrix(a.getWidth(), a.getHeight(), (i, j) -> a.getRow(j).get(i));
  }

  @FunctionalInterface
  public interface MatrixPopulator<E> {
    E apply(int i, int j);
  }

  /**
   * Create a new matrix from the given one with rows and columns taken from certain intervals.
   *
   * @param matrix The base matrix.
   * @param i0 The lower bound (inclusive) for the rows to include.
   * @param i1 The upper bound (exclusive) for the rows to include.
   * @param j0 The lower bound (inclusive) for the columns to include.
   * @param j1 The upper bound (exclusive) for the columns to include.
   * @param <E>
   * @return A new matrix from the given one with rows and columns taken from certain intervals
   */
  public static <E> Matrix<E> subMatrix(Matrix<E> matrix, int i0, int i1, int j0, int j1) {
    ArrayList<ArrayList<E>> rows = new ArrayList<>();
    for (int i = i0; i < i1; i++) {
      rows.add(new ArrayList<>(matrix.getRow(i).subList(j0, j1)));
    }
    return new Matrix<>(i1-i0, j1-j0, rows);
  }

}
