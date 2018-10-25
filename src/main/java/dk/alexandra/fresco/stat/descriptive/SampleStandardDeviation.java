package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.List;

public class SampleStandardDeviation implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> observed;

  public SampleStandardDeviation(List<DRes<SReal>> observed) {
    this.observed = observed;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      DRes<SReal> ssd = seq.seq(new Variance(observed));
      return seq.realAdvanced().sqrt(ssd);
    });
  }

}
