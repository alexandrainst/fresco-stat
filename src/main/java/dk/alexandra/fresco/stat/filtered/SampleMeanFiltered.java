package dk.alexandra.fresco.stat.filtered;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.filtered.helpers.DivideBySInt;
import dk.alexandra.fresco.stat.filtered.helpers.SumFiltered;
import java.util.List;

public class SampleMeanFiltered implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final List<DRes<SInt>> filter;

  public SampleMeanFiltered(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    this.data = data;
    this.filter = filter;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<SFixed> s = new SumFiltered(data, filter).buildComputation(par);
      DRes<SInt> n = AdvancedNumeric.using(par).sum(filter);
      return Pair.lazy(s, n);
    }).seq((seq, sn) -> new DivideBySInt(sn.getFirst(), sn.getSecond()).buildComputation(seq));
  }
}
