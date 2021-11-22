package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.filtered.OneSampleTTestFiltered;
import dk.alexandra.fresco.stat.filtered.SampleMeanFiltered;
import dk.alexandra.fresco.stat.filtered.SampleVarianceFiltered;
import java.util.List;

public class DefaultFilteredStatistics implements FilteredStatistics {

  private final ProtocolBuilderNumeric builder;

  DefaultFilteredStatistics(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SFixed> sampleMean(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    return new SampleMeanFiltered(data, filter).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, DRes<SFixed> mean,
      List<DRes<SInt>> filter) {
    return new SampleVarianceFiltered(data, mean, filter).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    return new SampleVarianceFiltered(data, filter).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> ttest(List<DRes<SFixed>> data, DRes<SFixed> mu, List<DRes<SInt>> filter) {
    return new OneSampleTTestFiltered(data, mu, filter).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> chiSquare(List<DRes<SInt>> observed, List<DRes<SFixed>> expected,
      List<DRes<SInt>> filter) {
    return null;
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramContinuous(double[] buckets, List<DRes<SFixed>> data,
      List<DRes<SInt>> filter) {
    return null;
  }
}