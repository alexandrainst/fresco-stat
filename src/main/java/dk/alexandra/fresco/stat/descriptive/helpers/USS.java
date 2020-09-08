package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute the uncorrected sum of squares
 */
public class USS implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> data;

  public USS(List<DRes<SReal>> data) {
    this.data = data;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(par -> {
      List<DRes<SReal>> squaredTerms =
          data.stream().map(x -> par.realNumeric().mult(x, x)).collect(Collectors.toList());
      return () -> squaredTerms;
    }).seq((seq, terms) -> {
      DRes<SReal> sum = seq.realAdvanced().sum(terms);
      return sum;
    });
  }

}
