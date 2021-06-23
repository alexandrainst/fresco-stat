package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.security.cert.CollectionCertStoreParameters;
import java.util.List;
import java.util.stream.Collectors;

public class OpenAndOutputList implements Computation<Void, ProtocolBuilderNumeric> {

  private final String key;
  private final PrintStream stream;
  private final List<DRes<SFixed>> values;

  public OpenAndOutputList(List<DRes<SFixed>> values, String key, PrintStream stream) {
    this.values = values;
    this.key = key;
    this.stream = stream;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      List<DRes<BigDecimal>> open = values.stream().map(FixedNumeric.using(seq)::open).collect(
          Collectors.toList());
      return DRes.of(open);
    }).seq((seq, open) -> {
      stream.print(key + ": " + open.stream().map(DRes::out).collect(Collectors.toList()) + "\n");
      return null;
    });
  }
}
