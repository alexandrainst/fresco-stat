package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.List;

/**
 * Compute the mean of a list of observations.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SampleMean implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> observed;

  public SampleMean(List<DRes<SReal>> observed) {
    this.observed = observed;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SReal> sum = seq.realAdvanced().sum(observed);
      return seq.realNumeric().div(sum, observed.size());
    });
  }

}
