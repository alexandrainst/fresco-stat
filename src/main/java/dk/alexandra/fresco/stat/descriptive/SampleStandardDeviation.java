package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

/**
 * Compute the stanard deviation of a list of observations.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SampleStandardDeviation implements Computation<SFixed, ProtocolBuilderNumeric> {

  private List<DRes<SFixed>> observed;
  private DRes<SFixed> mean;

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
