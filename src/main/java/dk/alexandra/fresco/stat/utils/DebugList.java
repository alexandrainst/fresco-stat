package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

public class DebugList implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> list;
  private final String tag;

  public DebugList(String tag, List<DRes<SFixed>> list) {
    this.tag = tag;
    this.list = list;
  }

  public DebugList(List<DRes<SFixed>> list) {
    this("", list);
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
         return DRes.of(VectorUtils.openToAll(list, seq));
    }).seq((seq, open) -> {
      if (seq.getBasicNumericContext().getMyId() == 1) {
        System.out.println(tag);
        System.out.println(open.out());
      }
      return DRes.of(list);

    });
  }
}
