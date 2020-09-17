package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.Statistics;
import java.math.BigDecimal;
import java.util.List;

public class OneSampleTTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> observed;
  private DRes<SReal> mu;

  public OneSampleTTest(List<DRes<SReal>> observed, DRes<SReal> mu) {
    this.observed = observed;
    this.mu = mu;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      Statistics stat = Statistics.using(seq);

      DRes<SReal> mean = stat.sampleMean(observed);
      DRes<SReal> s = stat.sampleStandardDeviation(observed, mean);
      DRes<SReal> t = seq.realNumeric().mult(BigDecimal.valueOf(Math.sqrt(observed.size())),
          seq.realNumeric().div(seq.realNumeric().sub(mean, mu), s));

      return t;
    });
  }

}
