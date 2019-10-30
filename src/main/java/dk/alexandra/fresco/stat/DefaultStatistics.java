package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.PearsonsCorrelation;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleStandardDeviation;
import dk.alexandra.fresco.stat.descriptive.SampleVariance;
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
  public DRes<SReal> sampleMean(List<DRes<SReal>> data) {
    return new SampleMean(data).buildComputation(builder);
  }

  public DRes<SReal> sampleVariance(List<DRes<SReal>> data, DRes<SReal> mean) {
    return new SampleVariance(data, mean).buildComputation(builder);

  }

  @Override
  public DRes<SReal> sampleVariance(List<DRes<SReal>> data) {
    return builder.seq(seq -> {
      Statistics stat = new DefaultStatistics(seq);
      DRes<SReal> mean = stat.sampleMean(data);
      return stat.sampleVariance(data, mean);
    });
  }

  @Override
  public DRes<SReal> sampleStandardDeviation(List<DRes<SReal>> data) {
    return builder.seq(seq -> {
      Statistics stat = new DefaultStatistics(seq);
      DRes<SReal> mean = stat.sampleMean(data);
      return stat.sampleStandardDeviation(data, mean);
    });
  }

  @Override
  public DRes<SReal> sampleStandardDeviation(List<DRes<SReal>> data, DRes<SReal> mean) {
    return new SampleStandardDeviation(data, mean).buildComputation(builder);
  }

  @Override
  public DRes<SReal> ttest(List<DRes<SReal>> data, DRes<SReal> mu) {
    return new OneSampleTTest(data, mu).buildComputation(builder);
  }

  @Override
  public DRes<SReal> ttest(List<DRes<SReal>> data1, List<DRes<SReal>> data2) {
    return new TwoSampleTTest(data1, data2).buildComputation(builder);
  }

  @Override
  public DRes<SReal> chiSquare(List<DRes<SInt>> observed, List<DRes<SReal>> expected) {
    return new ChiSquareTest(observed, expected).buildComputation(builder);
  }

  public DRes<LinearFunction> linearRegression(List<DRes<SReal>> x, DRes<SReal> meanX,
      List<DRes<SReal>> y, DRes<SReal> meanY) {
    return new LinearRegression(x, meanX, y, meanY).buildComputation(builder);
  }

  @Override
  public DRes<LinearFunction> linearRegression(List<DRes<SReal>> x, List<DRes<SReal>> y) {
    return builder.par(par -> {
      Statistics stat = new DefaultStatistics(par);
      DRes<SReal> meanX = stat.sampleMean(x);
      DRes<SReal> meanY = stat.sampleMean(y);
      return () -> new Pair<>(meanX, meanY);
    }).seq((seq, means) -> {
      return new LinearRegression(x, means.getFirst(), y, means.getSecond()).buildComputation(seq);
    });
  }

  @Override
  public DRes<SReal> correlation(List<DRes<SReal>> data1, DRes<SReal> mean1,
      List<DRes<SReal>> data2, DRes<SReal> mean2) {
    return new PearsonsCorrelation(data1, mean1, data2, mean2).buildComputation(builder);
  }

  @Override
  public DRes<SReal> correlation(List<DRes<SReal>> data1, List<DRes<SReal>> data2) {
    return builder.par(par -> {
      Statistics stat = new DefaultStatistics(par);
      DRes<SReal> mean1 = stat.sampleMean(data1);
      DRes<SReal> mean2 = stat.sampleMean(data2);
      return () -> new Pair<>(mean1, mean2);
    }).seq((seq, means) -> {
      return new PearsonsCorrelation(data1, means.getFirst(), data2, means.getSecond())
          .buildComputation(seq);
    });
  }

}
