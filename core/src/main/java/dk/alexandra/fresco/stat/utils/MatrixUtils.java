package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.lib.common.collections.Matrix;
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

}
