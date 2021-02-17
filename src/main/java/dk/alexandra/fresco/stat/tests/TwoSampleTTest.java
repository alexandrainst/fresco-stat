package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.Statistics;
import java.util.List;

public class TwoSampleTTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data1;
  private final List<DRes<SFixed>> data2;

  /**
   * This implements the calculation of a t-test statistics for two samples where it can be assumed
   * that the variances are equal.
   */
  public TwoSampleTTest(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2) {
    this.data1 = data1;
    this.data2 = data2;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par1 -> {
      Statistics statistics = Statistics.using(par1);
      DRes<SFixed> mean1 = statistics.sampleMean(data1);
      DRes<SFixed> mean2 = statistics.sampleMean(data2);
      return Pair.lazy(mean1, mean2);
    }).par((par2, means) -> {
      Statistics statistics = Statistics.using(par2);
      DRes<SFixed> var1 = statistics.sampleVariance(data1, means.getFirst());
      DRes<SFixed> var2 = statistics.sampleVariance(data2, means.getSecond());
      return Pair.lazy(means, new Pair<>(var1, var2));
    }).seq((seq, des) -> {
      DRes<SFixed> mean1 = des.getFirst().getFirst();
      DRes<SFixed> mean2 = des.getFirst().getSecond();
      DRes<SFixed> var1 = des.getSecond().getFirst();
      DRes<SFixed> var2 = des.getSecond().getSecond();

      FixedNumeric numeric = FixedNumeric.using(seq);
      DRes<SFixed> n = numeric.sub(mean1, mean2);
      DRes<SFixed> d;
      if (data1.size() == data2.size()) {
        d = numeric.add(var1, var2);
        d = AdvancedFixedNumeric.using(seq).sqrt(d);
        d = numeric.mult(1.0 / Math.sqrt(data1.size()), d);
      } else {
        d = numeric.mult(data1.size() - 1, var1);
        d = numeric.add(d,
            numeric.mult(data2.size() - 1, var2));
        d = numeric.div(d, data1.size() + data2.size() - 2);
        d = AdvancedFixedNumeric.using(seq).sqrt(d);
        d = numeric
            .mult(Math.sqrt(1.0 / data1.size() + 1.0 / data2.size()), d);
      }
      return numeric.div(n, d);
    });
  }

}
