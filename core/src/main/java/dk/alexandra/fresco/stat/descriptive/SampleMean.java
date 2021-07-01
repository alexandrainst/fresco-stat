package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

/**
 * Compute the mean of a list of observations.
 */
public class SampleMean implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> observed;

  public SampleMean(List<DRes<SFixed>> observed) {
    this.observed = observed;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SFixed> sum = AdvancedFixedNumeric.using(seq).sum(observed);
      return FixedNumeric.using(seq).div(sum, observed.size());
    });
  }

}
