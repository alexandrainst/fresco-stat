package dk.alexandra.fresco.service.applications;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.service.messages.Common;
import dk.alexandra.fresco.service.messages.Common.Matrix.Builder;
import dk.alexandra.fresco.service.messages.MPCInputOuterClass.MPCInput;
import dk.alexandra.fresco.service.messages.MPCInputOuterClass.MPCMatrix;
import dk.alexandra.fresco.service.messages.MPCInputOuterClass.MPCVector;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class Utils {

  static DRes<SFixed> input(MPCInput input, ProtocolBuilderNumeric builder) {
    if (input.hasValue()) {
      return FixedNumeric.using(builder)
          .input(input.getValue(), builder.getBasicNumericContext().getMyId());
    } else {
      int otherId = 3 - builder.getBasicNumericContext().getMyId();
      return FixedNumeric.using(builder).input(input.getValue(), otherId);
    }
  }

  static ArrayList<DRes<SFixed>> inputList(MPCVector input, ProtocolBuilderNumeric builder) {
    return input.getValuesList().stream().map(entry -> input(entry, builder)).collect(Collectors.toCollection(ArrayList::new));
  }

  static Matrix<DRes<SFixed>> inputMatrix(MPCMatrix input, ProtocolBuilderNumeric builder) {
    return MatrixUtils.buildMatrix(input.getRowsCount(), input.getRows(0).getValuesCount(),
        (i,j) -> input(input.getRows(i).getValues(j), builder));
  }

  static <T> Common.Vector convert(ArrayList<T> vector, ToDoubleFunction<T> converter) {
    return Common.Vector.newBuilder().addAllValue(vector.stream().mapToDouble(converter).boxed().collect(Collectors.toList())).build();
  }

  static Common.Vector convert(ArrayList<BigDecimal> vector) {
    return convert(vector, BigDecimal::doubleValue);
  }

  static <T> Common.Matrix convert(Matrix<T> matrix, ToDoubleFunction<T> converter) {
    Builder builder = Common.Matrix.newBuilder();
    matrix.getRows().forEach(row -> builder.addRows(convert(row, converter)));
    return builder.build();
  }

  static Common.Matrix convert(Matrix<BigDecimal> matrix) {
    return convert(matrix, BigDecimal::doubleValue);
  }


}
