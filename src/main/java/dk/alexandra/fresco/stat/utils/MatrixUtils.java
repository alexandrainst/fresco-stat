package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dk.alexandra.fresco.lib.collections.Matrix;

public class MatrixUtils {

  public static <E> Matrix<E> buildMatrix(int h, int w, BiFunction<Integer, Integer, E> populator) {
    return new Matrix<E>(h, w, i -> IntStream.range(0, w).mapToObj(j -> populator.apply(i, j))
        .collect(Collectors.toCollection(ArrayList::new)));
  }
  
  public static <E, F> Matrix<E> map(Matrix<F> matrix, Function<F, E> function) {
    return buildMatrix(matrix.getHeight(), matrix.getWidth(),
        (i, j) -> function.apply(matrix.getRow(i).get(j)));
  }

}
