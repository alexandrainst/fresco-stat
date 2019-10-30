package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;

import java.util.List;

/**
 * Compute the sample variance for a list of observations.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SampleVariance implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> observed;
  private DRes<SReal> mean;

  /**
   * Create a new computation with a given computed sample mean.
   * 
   * @param observed
   * @param mean
   */
  public SampleVariance(List<DRes<SReal>> observed, DRes<SReal> mean) {
    this.observed = observed;
    this.mean = mean;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SReal> sum = new SSD(observed, mean).buildComputation(builder);
      return builder.realNumeric().div(sum, observed.size() - 1);
    });
  }

}
