package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleVariance;
import java.math.BigDecimal;
import java.util.List;

public class TwoSampleTTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data1;
  private final List<DRes<SFixed>> data2;

  /**
   * This implements the calculation of a t-test statistics for two samples where it can be assumed
   * that the variances are equal.
   * <p>
   * TODO: Also implement case where variances are unequal.
   *
   * @param data1
   * @param data2
   */
  public TwoSampleTTest(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2) {
    this.data1 = data1;
    this.data2 = data2;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par1 -> {
      DRes<SFixed> mean1 = new SampleMean(data1).buildComputation(par1);
      DRes<SFixed> mean2 = new SampleMean(data2).buildComputation(par1);
      return () -> new Pair<>(mean1, mean2);
    }).par((par2, means) -> {
      DRes<SFixed> var1 = new SampleVariance(data1, means.getFirst()).buildComputation(par2);
      DRes<SFixed> var2 = new SampleVariance(data2, means.getSecond()).buildComputation(par2);
      return () -> new Pair<>(means, new Pair<>(var1, var2));
    }).seq((seq, des) -> {
      DRes<SFixed> mean1 = des.getFirst().getFirst();
      DRes<SFixed> mean2 = des.getFirst().getSecond();
      DRes<SFixed> var1 = des.getSecond().getFirst();
      DRes<SFixed> var2 = des.getSecond().getSecond();

      FixedNumeric numeric = FixedNumeric.using(seq);
      DRes<SFixed> n = numeric.sub(mean1, mean2);
      DRes<SFixed> d = null;
      if (data1.size() == data2.size()) {
        d = numeric.add(var1, var2);
        d = AdvancedFixedNumeric.using(seq).sqrt(d);
        d = numeric.mult(BigDecimal.valueOf(1.0 / Math.sqrt(data1.size())), d);
      } else {
        d = numeric.mult(BigDecimal.valueOf(data1.size() - 1), var1);
        d = numeric.add(d,
            numeric.mult(BigDecimal.valueOf(data2.size() - 1), var2));
        d = numeric.div(d, BigDecimal.valueOf(data1.size() + data2.size() - 2));
        d = numeric
            .mult(BigDecimal.valueOf(Math.sqrt(1.0 / data1.size() + 1.0 / data2.size())), d);
      }
      return numeric.div(n, d);
    });
  }

}
