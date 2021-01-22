package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.Histogram;
import dk.alexandra.fresco.stat.descriptive.LeakyFrequencyTable;
import dk.alexandra.fresco.stat.descriptive.PearsonsCorrelation;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleStandardDeviation;
import dk.alexandra.fresco.stat.descriptive.SampleVariance;
import dk.alexandra.fresco.stat.descriptive.TwoDimensionalHistogram;
import dk.alexandra.fresco.stat.regression.SimpleLinearRegression;
import dk.alexandra.fresco.stat.regression.SimpleLinearRegression.LinearFunction;
import dk.alexandra.fresco.stat.survival.SurvivalInfoContinuous;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.stat.survival.cox.CoxRegressionContinuous;
import dk.alexandra.fresco.stat.survival.cox.CoxRegressionDiscrete;
import dk.alexandra.fresco.stat.tests.ChiSquareTest;
import dk.alexandra.fresco.stat.tests.FTest;
import dk.alexandra.fresco.stat.tests.KruskallWallisTest;
import dk.alexandra.fresco.stat.tests.OneSampleTTest;
import dk.alexandra.fresco.stat.tests.TwoSampleTTest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class DefaultStatistics implements Statistics {

  private final ProtocolBuilderNumeric builder;

  DefaultStatistics(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SFixed> sampleMean(List<DRes<SFixed>> data) {
    return new SampleMean(data).buildComputation(builder);
  }

  public DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, DRes<SFixed> mean) {
    return new SampleVariance(data, mean).buildComputation(builder);

  }

  @Override
  public DRes<SFixed> sampleVariance(List<DRes<SFixed>> data) {
    return builder.seq(seq -> {
      Statistics stat = new DefaultStatistics(seq);
      DRes<SFixed> mean = stat.sampleMean(data);
      return stat.sampleVariance(data, mean);
    });
  }

  @Override
  public DRes<SFixed> sampleStandardDeviation(List<DRes<SFixed>> data) {
    return builder.seq(seq -> {
      Statistics stat = new DefaultStatistics(seq);
      DRes<SFixed> mean = stat.sampleMean(data);
      return stat.sampleStandardDeviation(data, mean);
    });
  }

  @Override
  public DRes<SFixed> sampleStandardDeviation(List<DRes<SFixed>> data, DRes<SFixed> mean) {
    return new SampleStandardDeviation(data, mean).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> ttest(List<DRes<SFixed>> data, DRes<SFixed> mu) {
    return new OneSampleTTest(data, mu).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> ttest(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2) {
    return new TwoSampleTTest(data1, data2).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> chiSquare(List<DRes<SInt>> observed, List<DRes<SFixed>> expected) {
    return new ChiSquareTest(observed, expected).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> chiSquare(List<DRes<SInt>> observed, double[] expected) {
    return new ChiSquareTest(observed, expected).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> chiSquare(List<DRes<SFixed>> data, List<DRes<SFixed>> buckets,
      List<DRes<SFixed>> expected) {
    if (buckets.size() + 1 != expected.size()) {
      throw new IllegalArgumentException(
          "There should be an expected value for each bucket and an extra for all values exceeding the largest bucket.");
    }
    return builder.seq(seq -> {
      Statistics stat = Statistics.using(seq);
      return stat.histogramReal(buckets, data);
    }).seq((seq, histogram) -> {
      Statistics stat = Statistics.using(seq);
      return stat.chiSquare(histogram, expected);
    });
  }

  @Override
  public DRes<SFixed> chiSquare(List<DRes<SFixed>> data, List<DRes<SFixed>> buckets,
      double[] expected) {
    if (buckets.size() + 1 != expected.length) {
      throw new IllegalArgumentException(
          "There should be an expected value for each bucket and an extra for all values exceeding the largest bucket.");
    }
    return builder.seq(seq -> {
      Statistics stat = Statistics.using(seq);
      return stat.histogramReal(buckets, data);
    }).seq((seq, histogram) -> {
      Statistics stat = Statistics.using(seq);
      return stat.chiSquare(histogram, expected);
    });
  }

  @Override
  public DRes<SFixed> chiSquare(List<DRes<SFixed>> data, double[] buckets, double[] expected) {
    // Using known buckets doesn't give any performance benefit, so we just use them as knowns and use the corresponding method with secret buckets.
    return chiSquare(data,
        Arrays.stream(buckets).mapToObj(FixedNumeric.using(builder)::known).collect(
            Collectors.toList()), expected);
  }

  public DRes<LinearFunction> linearRegression(List<DRes<SFixed>> x, DRes<SFixed> meanX,
      List<DRes<SFixed>> y, DRes<SFixed> meanY) {
    return new SimpleLinearRegression(x, meanX, y, meanY).buildComputation(builder);
  }

  @Override
  public DRes<LinearFunction> linearRegression(List<DRes<SFixed>> x, List<DRes<SFixed>> y) {
    return builder.par(par -> {
      Statistics stat = new DefaultStatistics(par);
      DRes<SFixed> meanX = stat.sampleMean(x);
      DRes<SFixed> meanY = stat.sampleMean(y);
      return () -> new Pair<>(meanX, meanY);
    }).seq((seq, means) -> new SimpleLinearRegression(x, means.getFirst(), y, means.getSecond())
        .buildComputation(seq));
  }

  @Override
  public DRes<SFixed> correlation(List<DRes<SFixed>> data1, DRes<SFixed> mean1,
      List<DRes<SFixed>> data2, DRes<SFixed> mean2) {
    return new PearsonsCorrelation(data1, mean1, data2, mean2).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> correlation(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2) {
    return builder.par(par -> {
      Statistics stat = new DefaultStatistics(par);
      DRes<SFixed> mean1 = stat.sampleMean(data1);
      DRes<SFixed> mean2 = stat.sampleMean(data2);
      return () -> new Pair<>(mean1, mean2);
    }).seq(
        (seq, means) -> new PearsonsCorrelation(data1, means.getFirst(), data2, means.getSecond())
            .buildComputation(seq));
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramInt(List<DRes<SInt>> buckets, List<DRes<SInt>> data) {
    return new Histogram(buckets, data).buildComputation(builder);
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramReal(List<DRes<SFixed>> buckets, List<DRes<SFixed>> data) {
    return builder.seq(seq -> {
      List<DRes<SInt>> intBuckets =
          buckets.stream().map(bi -> bi.out().getSInt()).collect(Collectors.toList());
      List<DRes<SInt>> intData =
          data.stream().map(xi -> xi.out().getSInt()).collect(Collectors.toList());
      return new DefaultStatistics(seq).histogramInt(intBuckets, intData);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramReal(double[] buckets, List<DRes<SFixed>> data) {
    return histogramReal(
        Arrays.stream(buckets).mapToObj(FixedNumeric.using(builder)::known)
            .collect(Collectors.toList()),
        data);
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramInt(
      Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets, List<Pair<DRes<SInt>, DRes<SInt>>> data) {
    return new TwoDimensionalHistogram(buckets, data).buildComputation(builder);
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramReal(
      Pair<List<DRes<SFixed>>, List<DRes<SFixed>>> buckets,
      List<Pair<DRes<SFixed>, DRes<SFixed>>> data) {
    return builder.seq(seq -> {
      Pair<List<DRes<SInt>>, List<DRes<SInt>>> intBuckets = new Pair<>(
          buckets.getFirst().stream().map(bi -> bi.out().getSInt())
              .collect(Collectors.toList()),
          buckets.getSecond().stream().map(bi -> bi.out().getSInt())
              .collect(Collectors.toList()));
      List<Pair<DRes<SInt>, DRes<SInt>>> intData =
          data.stream().map(p -> new Pair<>(p.getFirst().out().getSInt(),
              p.getSecond().out().getSInt())).collect(Collectors.toList());
      return new DefaultStatistics(seq).twoDimensionalHistogramInt(intBuckets, intData);
    });
  }

  @Override
  public DRes<SFixed> ffest(List<List<DRes<SFixed>>> observed) {
    return builder.seq(seq -> new FTest(observed).buildComputation(seq));
  }

  @Override
  public DRes<SFixed> kruskallWallisTest(List<List<DRes<SFixed>>> observed) {
    return builder.seq(seq -> new KruskallWallisTest(KruskallWallisTest.fromSFixed(observed),
        true).buildComputation(seq));
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, Integer>>> leakyFrequencies(List<DRes<SInt>> data) {
    return builder.seq(seq -> new LeakyFrequencyTable(data).buildComputation(seq));
  }

  @Override
  public DRes<List<DRes<SFixed>>> coxRegressionDiscrete(List<SurvivalInfoDiscrete> data) {
    return coxRegressionDiscrete(data, 10, 0.05,
        DoubleStream.generate(() -> 0.0).limit(data.get(0).getCovariates().size()).toArray());
  }

  @Override
  public DRes<List<DRes<SFixed>>> coxRegressionDiscrete(List<SurvivalInfoDiscrete> data,
      int iterations,
      double alpha, double[] beta) {
    return builder
        .seq(seq -> new CoxRegressionDiscrete(data, iterations, alpha, beta).buildComputation(seq));
  }

  @Override
  public DRes<List<DRes<SFixed>>> coxRegressionContinuous(List<SurvivalInfoContinuous> data) {
    return coxRegressionContinuous(data, 10, 0.05,
        DoubleStream.generate(() -> 0.0).limit(data.get(0).getCovariates().size()).toArray());
  }

  @Override
  public DRes<List<DRes<SFixed>>> coxRegressionContinuous(List<SurvivalInfoContinuous> data,
      int iterations, double alpha, double[] beta) {
    return builder.seq(
        seq -> new CoxRegressionContinuous(data, iterations, alpha, beta).buildComputation(seq));
  }

}
