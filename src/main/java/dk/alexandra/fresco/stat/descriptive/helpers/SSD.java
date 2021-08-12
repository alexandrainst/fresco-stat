package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute the sum of squared deviations
 */
public class SSD implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final DRes<SFixed> mean;

  public SSD(List<DRes<SFixed>> data, DRes<SFixed> mean) {
    this.data = data;
    this.mean = mean;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(par -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      List<DRes<SFixed>> terms = data.stream().map(x -> fixedNumeric.sub(x, mean))
          .collect(Collectors.toList());
      return DRes.of(terms);
    }).seq((seq, terms) -> seq.seq(new USS(terms)));
  }

}
