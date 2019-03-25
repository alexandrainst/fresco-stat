package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.Mean;
import dk.alexandra.fresco.stat.descriptive.SampleStandardDeviation;
import dk.alexandra.fresco.stat.descriptive.Variance;
import dk.alexandra.fresco.stat.tests.ChiSquareTest;
import dk.alexandra.fresco.stat.tests.LinearRegression;
import dk.alexandra.fresco.stat.tests.LinearRegression.LinearFunction;
import dk.alexandra.fresco.stat.tests.OneSampleTTest;
import dk.alexandra.fresco.stat.tests.TwoSampleTTest;
import java.util.List;

public class DefaultStatistics implements Statistics {

  private ProtocolBuilderNumeric builder;

  public DefaultStatistics(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SReal> mean(List<DRes<SReal>> data) {
    return new Mean(data).buildComputation(builder);
  }

  @Override
  public DRes<SReal> variance(List<DRes<SReal>> data) {
    return new Variance(data).buildComputation(builder);
  }

  @Override
  public DRes<SReal> ssd(List<DRes<SReal>> data) {
    return builder.seq(new SampleStandardDeviation(data));
  }

  @Override
  public DRes<SReal> ttest(List<DRes<SReal>> data, DRes<SReal> mu) {
    return builder.seq(new OneSampleTTest(data, mu));
  }

  @Override
  public DRes<SReal> ttest(List<DRes<SReal>> data1, List<DRes<SReal>> data2) {
    return builder.seq(new TwoSampleTTest(data1, data2));
  }

  @Override
  public DRes<SReal> chiSquare(List<DRes<SInt>> observed, List<DRes<SReal>> expected) {
    return builder.seq(new ChiSquareTest(observed, expected));
  }

  @Override
  public DRes<LinearFunction> linearRegression(List<DRes<SReal>> x, List<DRes<SReal>> y) {
    return builder.seq(new LinearRegression(x, y));
  }

}
