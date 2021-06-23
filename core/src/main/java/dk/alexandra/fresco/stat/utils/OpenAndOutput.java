package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.io.OutputStream;
import java.io.PrintStream;

public class OpenAndOutput implements Computation<Void, ProtocolBuilderNumeric> {

  private final DRes<SFixed> value;
  private final String key;
  private final PrintStream stream;

  public OpenAndOutput(DRes<SFixed> value, String key, PrintStream stream) {
    this.value = value;
    this.key = key;
    this.stream = stream;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> FixedNumeric.using(seq).open(value)).seq((seq, open) -> {
      stream.print(key + ": " + open + "\n");
      return null;
    });
  }
}
