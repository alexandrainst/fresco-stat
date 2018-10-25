package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.Mean;
import dk.alexandra.fresco.stat.descriptive.SampleStandardDeviation;
import dk.alexandra.fresco.stat.descriptive.Variance;
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
    return builder.seq(new Mean(data));
  }

  @Override
  public DRes<SReal> variance(List<DRes<SReal>> data) {
    return builder.seq(new Variance(data));
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
  
}
