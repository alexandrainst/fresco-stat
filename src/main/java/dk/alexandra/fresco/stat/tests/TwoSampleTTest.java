package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleVariance;
import java.math.BigDecimal;
import java.util.List;

public class TwoSampleTTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> data1;
  private List<DRes<SReal>> data2;

  /**
   * This implements the calculation of a t-test statistics for two samples where it can be assumed
   * that the variances are equal.
   * <p>
   * TODO: Also implement case where variances are unequal.
   *
   * @param data1
   * @param data2
   */
  public TwoSampleTTest(List<DRes<SReal>> data1, List<DRes<SReal>> data2) {
    this.data1 = data1;
    this.data2 = data2;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par1 -> {
      DRes<SReal> mean1 = new SampleMean(data1).buildComputation(par1);
      DRes<SReal> mean2 = new SampleMean(data2).buildComputation(par1);
      return () -> new Pair<>(mean1, mean2);
    }).par((par2, means) -> {
      DRes<SReal> var1 = new SampleVariance(data1, means.getFirst()).buildComputation(par2);
      DRes<SReal> var2 = new SampleVariance(data2, means.getSecond()).buildComputation(par2);
      return () -> new Pair<>(means, new Pair<>(var1, var2));
    }).seq((seq, des) -> {
      DRes<SReal> mean1 = des.getFirst().getFirst();
      DRes<SReal> mean2 = des.getFirst().getSecond();
      DRes<SReal> var1 = des.getSecond().getFirst();
      DRes<SReal> var2 = des.getSecond().getSecond();

      DRes<SReal> n = seq.realNumeric().sub(mean1, mean2);
      DRes<SReal> d = null;
      if (data1.size() == data2.size()) {
        d = seq.realNumeric().add(var1, var2);
        d = seq.realAdvanced().sqrt(d);
        d = seq.realNumeric().mult(BigDecimal.valueOf(1.0 / Math.sqrt(data1.size())), d);
      } else {
        d = seq.realNumeric().mult(BigDecimal.valueOf(data1.size() - 1), var1);
        d = seq.realNumeric().add(d,
            seq.realNumeric().mult(BigDecimal.valueOf(data2.size() - 1), var2));
        d = seq.realNumeric().div(d, BigDecimal.valueOf(data1.size() + data2.size() - 2));
        d = seq.realNumeric()
            .mult(BigDecimal.valueOf(Math.sqrt(1.0 / data1.size() + 1.0 / data2.size())), d);
      }
      return seq.realNumeric().div(n, d);
    });
  }

}
