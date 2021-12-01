package dk.alexandra.fresco.stat.filtered.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.utils.MultiplyWithSInt;
import java.util.ArrayList;
import java.util.List;

public class SumFiltered implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final List<DRes<SInt>> filter;

  public SumFiltered(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    this.data = data;
    this.filter = filter;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    if (data.size() != filter.size()) {
      throw new IllegalArgumentException("Data and filter must have same size");
    }
    return builder.par(par -> {
      List<DRes<SFixed>> filteredData = new ArrayList<>();
      for (int i = 0; i < data.size(); i++) {
        filteredData.add(new MultiplyWithSInt(data.get(i), filter.get(i)).buildComputation(par));
      }
      return DRes.of(filteredData);
    }).seq((seq, filteredData) -> AdvancedFixedNumeric.using(seq).sum(filteredData));
  }
}
