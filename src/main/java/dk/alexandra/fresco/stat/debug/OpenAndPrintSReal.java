package dk.alexandra.fresco.stat.debug;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;

public class OpenAndPrintSReal implements Computation<Void, ProtocolBuilderNumeric>{
  
  private DRes<SReal> number;
  private String label;

  public OpenAndPrintSReal(String label, DRes<SReal> number) {
    this.label = label;
    this.number = number;
  }
  
  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      RealNumeric num = seq.realNumeric();
      List<DRes<BigDecimal>> res = new ArrayList<>();
      res.add(num.open(number));
      return () -> res;
    }).seq((seq, res) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(label);
      sb.append("\n");
      sb.append(res.get(0).out());
      seq.debug().marker(sb.toString(), System.out);
      return null;
    });
  }
}
