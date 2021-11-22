package dk.alexandra.fresco.stat.filtered.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;
import java.util.stream.Collectors;

public class USSFiltered  implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final List<DRes<SInt>> filter;

  public USSFiltered(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    this.data = data;
    this.filter = filter;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SFixed>> squaredTerms =
          data.stream().map(x -> FixedNumeric.using(par).mult(x, x)).collect(Collectors.toList());
      return DRes.of(squaredTerms);
    }).seq((seq, terms) -> new SumFiltered(terms, filter).buildComputation(seq));
  }

}