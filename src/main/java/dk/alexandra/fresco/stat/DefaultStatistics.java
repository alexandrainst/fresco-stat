package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.Histogram;
import dk.alexandra.fresco.stat.descriptive.LeakyFrequencyTable;
import dk.alexandra.fresco.stat.descriptive.PearsonsCorrelation;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleStandardDeviation;
import dk.alexandra.fresco.stat.descriptive.SampleVariance;
import dk.alexandra.fresco.stat.descriptive.TwoDimensionalHistogram;
import dk.alexandra.fresco.stat.tests.ChiSquareTest;
import dk.alexandra.fresco.stat.tests.FTest;
import dk.alexandra.fresco.stat.tests.KruskallWallisTest;
import dk.alexandra.fresco.stat.regression.LinearRegression;
import dk.alexandra.fresco.stat.regression.LinearRegression.LinearFunction;
import dk.alexandra.fresco.stat.tests.OneSampleTTest;
import dk.alexandra.fresco.stat.tests.TwoSampleTTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultStatistics implements Statistics {

  private ProtocolBuilderNumeric builder;

  DefaultStatistics(ProtocolBuilderNumeric builder) {
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

  @Override
  public DRes<SReal> chiSquare(List<DRes<SInt>> observed, double[] expected) {
    return new ChiSquareTest(observed, expected).buildComputation(builder);
  }

  @Override
  public DRes<SReal> chiSquare(List<DRes<SReal>> data, List<DRes<SReal>> buckets,
      List<DRes<SReal>> expected) {
    if (buckets.size() + 1 != expected.size()) {
      throw new IllegalArgumentException("There should be an expected value for each bucket and an extra for all values exceeding the largest bucket.");
    }
    return builder.seq(seq -> {
      Statistics stat = Statistics.using(seq);
      DRes<List<DRes<SInt>>> histogram = stat.histogramReal(buckets, data);
      return histogram;
    }).seq((seq, histogram) -> {
      Statistics stat = Statistics.using(seq);
      return stat.chiSquare(histogram, expected);
    });
  }

  @Override
  public DRes<SReal> chiSquare(List<DRes<SReal>> data, List<DRes<SReal>> buckets,
      double[] expected) {
    if (buckets.size() + 1 != expected.length) {
      throw new IllegalArgumentException("There should be an expected value for each bucket and an extra for all values exceeding the largest bucket.");
    }
    return builder.seq(seq -> {
      Statistics stat = Statistics.using(seq);
      DRes<List<DRes<SInt>>> histogram = stat.histogramReal(buckets, data);
      return histogram;
    }).seq((seq, histogram) -> {
      Statistics stat = Statistics.using(seq);
      return stat.chiSquare(histogram, expected);
    });
  }

  @Override
  public DRes<SReal> chiSquare(List<DRes<SReal>> data, double[] buckets, double[] expected) {
    // Using known buckets doesn't give any performance benefit, so we just use them as knowns and use the corresponding method with secret buckets.
    return chiSquare(data, Arrays.stream(buckets).mapToObj(builder.realNumeric()::known).collect(
        Collectors.toList()), expected);
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

  @Override
  public DRes<List<DRes<SInt>>> histogramInt(List<DRes<SInt>> buckets, List<DRes<SInt>> data) {
    return new Histogram(buckets, data).buildComputation(builder);
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramReal(List<DRes<SReal>> buckets, List<DRes<SReal>> data) {
    return builder.seq(seq -> {
      List<DRes<SInt>> intBuckets =
          buckets.stream().map(bi -> ((SFixed) bi.out()).getSInt()).collect(Collectors.toList());
      List<DRes<SInt>> intData =
          data.stream().map(xi -> ((SFixed) xi.out()).getSInt()).collect(Collectors.toList());
      return new DefaultStatistics(seq).histogramInt(intBuckets, intData);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramReal(double[] buckets, List<DRes<SReal>> data) {
    return histogramReal(Arrays.stream(buckets).mapToObj(builder.realNumeric()::known).collect(Collectors.toList()), data);
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramInt(
      Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets, List<Pair<DRes<SInt>, DRes<SInt>>> data) {
    return new TwoDimensionalHistogram(buckets, data).buildComputation(builder);
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramReal(
      Pair<List<DRes<SReal>>, List<DRes<SReal>>> buckets,
      List<Pair<DRes<SReal>, DRes<SReal>>> data) {
    return builder.seq(seq -> {
      Pair<List<DRes<SInt>>, List<DRes<SInt>>> intBuckets = new Pair<>(
          buckets.getFirst().stream().map(bi -> ((SFixed) bi.out()).getSInt())
              .collect(Collectors.toList()),
          buckets.getSecond().stream().map(bi -> ((SFixed) bi.out()).getSInt())
              .collect(Collectors.toList()));
      List<Pair<DRes<SInt>, DRes<SInt>>> intData =
          data.stream().map(p -> new Pair<>(((SFixed) p.getFirst().out()).getSInt(),
              ((SFixed) p.getSecond().out()).getSInt())).collect(Collectors.toList());
      return new DefaultStatistics(seq).twoDimensionalHistogramInt(intBuckets, intData);
    });
  }

  @Override
  public DRes<SReal> ffest(List<List<DRes<SReal>>> observed) {
    return builder.seq(seq -> new FTest(observed).buildComputation(seq));
  }

  @Override
  public DRes<SReal> kruskallWallisTest(List<List<DRes<SReal>>> observed) {
    return builder.seq(seq -> new KruskallWallisTest(KruskallWallisTest.fromSReal(observed),
        true).buildComputation(seq));
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, Integer>>> leakyFrequencies(List<DRes<SInt>> data) {
    return builder.seq(seq -> new LeakyFrequencyTable(data).buildComputation(seq));
  }

}
