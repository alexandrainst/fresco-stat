package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.Statistics;
import java.math.BigDecimal;
import java.util.List;

/**
 * Compute a <i>t</i>-test statistics on a sample for the null hypothesis the mean of the sample is equal to <i>mu</i>.
 */
public class OneSampleTTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> observed;
  private final DRes<SFixed> mu;

  public OneSampleTTest(List<DRes<SFixed>> observed, DRes<SFixed> mu) {
    this.observed = observed;
    this.mu = mu;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      Statistics stat = Statistics.using(seq);
      FixedNumeric numeric = FixedNumeric.using(seq);

      DRes<SFixed> mean = stat.sampleMean(observed);
      DRes<SFixed> s = stat.sampleStandardDeviation(observed, mean);

      return numeric.mult(BigDecimal.valueOf(Math.sqrt(observed.size())),
          numeric.div(numeric.sub(mean, mu), s));
    });
  }

}
