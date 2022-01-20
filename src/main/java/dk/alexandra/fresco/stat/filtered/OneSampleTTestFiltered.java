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
import dk.alexandra.fresco.stat.filtered.OneSampleTTestFiltered.FilteredTTestResult;
import dk.alexandra.fresco.stat.utils.DivideBySInt;
import java.util.List;

/**
 * Compute a <i>t</i>-test statistics on a sample for the null hypothesis the mean of the sample is
 * equal to <i>mu</i>. The input is <i>filtered</i>, meaning that besides the data set, the input
 * also consists of a secret 0-1-vector (a <code>filter</code>) which indicates what entries of the data
 * set should be included in the analysis
 */
public class OneSampleTTestFiltered implements
    Computation<FilteredTTestResult, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> observed;
  private final DRes<SFixed> mu;
  private final List<DRes<SInt>> filter;

  public OneSampleTTestFiltered(List<DRes<SFixed>> observed, DRes<SFixed> mu,
      List<DRes<SInt>> filter) {
    this.observed = observed;
    this.mu = mu;
    this.filter = filter;
  }

  @Override
  public DRes<FilteredTTestResult> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      FilteredStatistics stat = FilteredStatistics.using(seq);
      FixedNumeric numeric = FixedNumeric.using(seq);
      DRes<SFixed> mean = stat.sampleMean(observed, filter);
      DRes<SFixed> sSquared = stat.sampleVariance(observed, mean, filter);
      DRes<SInt> n = AdvancedNumeric.using(seq).sum(filter);
      DRes<SFixed> divisor = AdvancedFixedNumeric.using(seq)
          .sqrt(new DivideBySInt(sSquared, n).buildComputation(seq));
      return new FilteredTTestResult(numeric.div(numeric.sub(mean, mu), divisor), n);
    });
  }

  public static class FilteredTTestResult implements DRes<FilteredTTestResult> {

    private final DRes<SFixed> result;
    private final DRes<SInt> n;

    public FilteredTTestResult(DRes<SFixed> result, DRes<SInt> n) {
      this.result = result;
      this.n = n;
    }

    /**
     * The test statistics
     */
    public DRes<SFixed> getResult() {
      return result;
    }

    /**
     * The sample size
     */
    public DRes<SInt> getN() {
      return n;
    }

    @Override
    public FilteredTTestResult out() {
      return this;
    }
  }
}
