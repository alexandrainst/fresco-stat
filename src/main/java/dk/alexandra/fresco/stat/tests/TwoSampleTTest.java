package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.Mean;
//import dk.alexandra.fresco.stat.descriptive.Mean;
import dk.alexandra.fresco.stat.descriptive.Variance;
import java.math.BigDecimal;
import java.util.List;

public class TwoSampleTTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> data1;
  private List<DRes<SReal>> data2;
  private DRes<SReal> mean1;
  private DRes<SReal> var1;
  private int n1;
  private DRes<SReal> mean2;
  private DRes<SReal> var2;
  private int n2;
  private boolean discriptivesGiven;

  /**
   * This implements the calculation of a t-test statistics for two samples where it can be assumed
   * that the variances are equal.
   * 
   * TODO: Also implement case where variances are unequal.
   * 
   * @param data1
   * @param data2
   */
  public TwoSampleTTest(List<DRes<SReal>> data1, List<DRes<SReal>> data2) {
    this.data1 = data1;
    this.data2 = data2;
    this.discriptivesGiven = false;
  }

  public TwoSampleTTest(DRes<SReal> mean1, DRes<SReal> var1, int n1, DRes<SReal> mean2,
      DRes<SReal> var2, int n2) {
    this.discriptivesGiven = true;
    this.mean1 = mean1;
    this.var1 = var1;
    this.n1 = n1;
    this.mean2 = mean2;
    this.var2 = var2;
    this.n2 = n2;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      // TODO: Calculations on the two datasets can be done in parallel.
      if (!discriptivesGiven) {
        mean1 = seq.seq(new Mean(data1));
        mean2 = seq.seq(new Mean(data2));
        var1 = seq.seq(new Variance(data1, mean1));
        var2 = seq.seq(new Variance(data2, mean2));
        n1 = data1.size();
        n2 = data2.size();
      }
      
      DRes<SReal> n = seq.realNumeric().sub(mean1, mean2);
      
      DRes<SReal> d = null;
      if (n1 == n2) {
        d = seq.realNumeric().add(var1, var2);
        d = seq.realAdvanced().sqrt(d);
        d = seq.realNumeric().mult(BigDecimal.valueOf(1.0 / Math.sqrt(n1)), d);
      } else {
        d = seq.realNumeric().mult(BigDecimal.valueOf(data1.size() - 1), var1);
        d = seq.realNumeric().add(d,
            seq.realNumeric().mult(BigDecimal.valueOf(data2.size() - 1), var2));
        d = seq.realNumeric().div(d, BigDecimal.valueOf(data1.size() + data2.size() - 2));
        d = seq.realNumeric().mult(BigDecimal.valueOf(Math.sqrt(1.0 / n1 + 1.0 / n2)), d);
      }
      return seq.realNumeric().div(n, d);
    });
  }

}
