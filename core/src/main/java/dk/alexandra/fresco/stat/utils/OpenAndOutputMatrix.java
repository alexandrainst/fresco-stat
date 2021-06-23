package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.io.PrintStream;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OpenAndOutputMatrix implements Computation<Void, ProtocolBuilderNumeric> {

  private final String key;
  private final PrintStream stream;
  private final Matrix<DRes<SFixed>> values;

  public OpenAndOutputMatrix(Matrix<DRes<SFixed>> values, String key, PrintStream stream) {
    this.values = values;
    this.key = key;
    this.stream = stream;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> DRes.of(IntStream.range(0, values.getHeight()).mapToObj(i -> new OpenAndOutputList(values.getRow(i), key + "_" + i, stream).buildComputation(seq)).collect(
        Collectors.toList()))).seq((seq, rows) -> null);
  }
}
