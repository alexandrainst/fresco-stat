package dk.alexandra.fresco.stat.filtered;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.FilteredStatistics;
import dk.alexandra.fresco.stat.utils.DivideBySInt;
import java.util.List;

/**
 * Compute a <i>t</i>-test statistics on a sample for the null hypothesis the mean of the sample is equal to <i>mu</i>.
 */
public class OneSampleTTestFiltered implements Computation<FilteredResult, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> observed;
  private final DRes<SFixed> mu;
  private final List<DRes<SInt>> filter;

  public OneSampleTTestFiltered(List<DRes<SFixed>> observed, DRes<SFixed> mu, List<DRes<SInt>> filter) {
    this.observed = observed;
    this.mu = mu;
    this.filter = filter;
  }

  @Override
  public DRes<FilteredResult> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      FilteredStatistics stat = FilteredStatistics.using(seq);
      FixedNumeric numeric = FixedNumeric.using(seq);
      DRes<SFixed> mean = stat.sampleMean(observed, filter);
      DRes<SFixed> sSquared = stat.sampleVariance(observed, mean, filter);
      DRes<SInt> n = AdvancedNumeric.using(seq).sum(filter);
      DRes<SFixed> divisor = AdvancedFixedNumeric.using(seq).sqrt(new DivideBySInt(sSquared, n).buildComputation(seq));
      return new FilteredResult(numeric.div(numeric.sub(mean, mu), divisor), n);
    });
  }

}
