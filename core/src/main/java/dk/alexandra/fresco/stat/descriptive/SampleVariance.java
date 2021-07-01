package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import java.util.List;

/**
 * Compute the sample variance for a list of observations.
 */
public class SampleVariance implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> observed;
  private final DRes<SFixed> mean;

  /**
   * Create a new computation with a given computed sample mean. Use {@link SampleMean} to compute
   * the mean.
   */
  public SampleVariance(List<DRes<SFixed>> observed, DRes<SFixed> mean) {
    this.observed = observed;
    this.mean = mean;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SFixed> sum = new SSD(observed, mean).buildComputation(builder);
      return FixedNumeric.using(builder).div(sum, observed.size() - 1);
    });
  }

}
