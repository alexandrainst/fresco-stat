package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.List;

/**
 * Compute the stanard deviation of a list of observations.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SampleStandardDeviation implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> observed;
  private DRes<SReal> mean;

  public SampleStandardDeviation(List<DRes<SReal>> observed) {
    this.observed = observed;
  }

  public SampleStandardDeviation(List<DRes<SReal>> observed, DRes<SReal> mean) {
    this.observed = observed;
    this.mean = mean;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SReal> ssd = new Variance(observed, mean).buildComputation(builder);
      return builder.realAdvanced().sqrt(ssd);
    });
  }

}
