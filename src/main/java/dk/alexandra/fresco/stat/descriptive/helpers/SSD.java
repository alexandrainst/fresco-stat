package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute the sum of squared deviations
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SSD implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> data;
  private DRes<SReal> mean;

  public SSD(List<DRes<SReal>> data, DRes<SReal> mean) {
    this.data = data;
    this.mean = mean;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(par -> {
      List<DRes<SReal>> terms = data.stream().map(x -> par.realNumeric().sub(x, mean))
          .collect(Collectors.toList());
      return () -> terms;
    }).par((par, terms) -> {
      List<DRes<SReal>> squaredTerms =
          terms.stream().map(x -> par.realNumeric().mult(x, x)).collect(Collectors.toList());
      return () -> squaredTerms;
    }).seq((seq, terms) -> {
      DRes<SReal> sum = seq.realAdvanced().sum(terms);
      return sum;
    });
  }

}
