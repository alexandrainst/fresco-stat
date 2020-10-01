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
 * Compute the uncorrected sum of squares
 */
public class USS implements Computation<SFixed, ProtocolBuilderNumeric> {

  private List<DRes<SFixed>> data;

  public USS(List<DRes<SFixed>> data) {
    this.data = data;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(par -> {
      List<DRes<SFixed>> squaredTerms =
          data.stream().map(x -> FixedNumeric.using(par).mult(x, x)).collect(Collectors.toList());
      return () -> squaredTerms;
    }).seq((seq, terms) -> {
      return AdvancedFixedNumeric.using(seq).sum(terms);
    });
  }

}
