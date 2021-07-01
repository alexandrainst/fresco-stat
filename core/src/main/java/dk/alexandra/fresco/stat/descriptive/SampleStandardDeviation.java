package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

/**
 * Compute the standard deviation of a list of observations.
 */
public class SampleStandardDeviation implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> observed;
  private final DRes<SFixed> mean;

  public SampleStandardDeviation(List<DRes<SFixed>> observed, DRes<SFixed> mean) {
    this.observed = observed;
    this.mean = mean;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SFixed> ssd = new SampleVariance(observed, mean).buildComputation(builder);
      return AdvancedFixedNumeric.using(builder).sqrt(ssd);
    });
  }

}
