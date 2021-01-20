package dk.alexandra.fresco.stat.debug;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.debug.DefaultDebug;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OpenAndPrintSFixed implements Computation<Void, ProtocolBuilderNumeric> {

  private final DRes<SFixed> number;
  private final String label;

  public OpenAndPrintSFixed(String label, DRes<SFixed> number) {
    this.label = label;
    this.number = number;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      FixedNumeric num = FixedNumeric.using(seq);
      List<DRes<BigDecimal>> res = new ArrayList<>();
      res.add(num.open(number));
      return () -> res;
    }).seq((seq, res) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(label);
      sb.append("\n");
      sb.append(res.get(0).out());
      new DefaultDebug(seq).marker(sb.toString(), System.out);
      return null;
    });
  }
}
