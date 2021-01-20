package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.lib.common.collections.Matrix;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixUtils {

  public static <E> Matrix<E> buildMatrix(int h, int w, MatrixPopulator<E> populator) {
    return new Matrix<>(h, w, i -> IntStream.range(0, w).mapToObj(j -> populator.apply(i, j))
        .collect(Collectors.toCollection(ArrayList::new)));
  }

  public static <E, F> Matrix<E> map(Matrix<F> matrix, Function<F, E> function) {
    return buildMatrix(matrix.getHeight(), matrix.getWidth(),
        (i, j) -> function.apply(matrix.getRow(i).get(j)));
  }

  @FunctionalInterface
  public interface MatrixPopulator<E> {

    E apply(int i, int j);
  }

}
