package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.anonymisation.KAnonymity;
import dk.alexandra.fresco.stat.descriptive.Histogram;
import dk.alexandra.fresco.stat.descriptive.LeakyFrequencyTable;
import dk.alexandra.fresco.stat.descriptive.MultiDimensionalHistogram;
import dk.alexandra.fresco.stat.descriptive.PearsonCorrelation;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleStandardDeviation;
import dk.alexandra.fresco.stat.descriptive.SampleVariance;
import dk.alexandra.fresco.stat.descriptive.TwoDimensionalHistogram;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import dk.alexandra.fresco.stat.regression.linear.SimpleLinearRegression;
import dk.alexandra.fresco.stat.regression.linear.SimpleLinearRegression.SimpleLinearRegressionResult;
import dk.alexandra.fresco.stat.regression.logistic.LogisticRegression;
import dk.alexandra.fresco.stat.survival.SurvivalInfoContinuous;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.stat.survival.cox.CoxRegressionContinuous;
import dk.alexandra.fresco.stat.survival.cox.CoxRegressionDiscrete;
import dk.alexandra.fresco.stat.tests.ChiSquareTest;
import dk.alexandra.fresco.stat.tests.FTest;
import dk.alexandra.fresco.stat.tests.KruskallWallisTest;
import dk.alexandra.fresco.stat.tests.OneSampleTTest;
import dk.alexandra.fresco.stat.tests.TwoSampleTTest;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;

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
  public DRes<LinearRegressionResult> linearRegression(List<ArrayList<DRes<SFixed>>> x,
      ArrayList<DRes<SFixed>> y) {
    return new LinearRegression(x, y).buildComputation(builder);
  }

  @Override
  public DRes<SimpleLinearRegressionResult> simpleLinearRegression(List<DRes<SFixed>> x,
      List<DRes<SFixed>> y) {
    return builder.seq(new SimpleLinearRegression(x, y));
  }

  @Override
  public DRes<SFixed> correlation(List<DRes<SFixed>> data1, DRes<SFixed> mean1,
      List<DRes<SFixed>> data2, DRes<SFixed> mean2) {
    return new PearsonCorrelation(data1, mean1, data2, mean2).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> correlation(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2) {
    return builder.par(par -> {
      Statistics stat = new DefaultStatistics(par);
      DRes<SFixed> mean1 = stat.sampleMean(data1);
      DRes<SFixed> mean2 = stat.sampleMean(data2);
      return Pair.lazy(mean1, mean2);
    }).seq(
        (seq, means) -> Statistics.using(seq)
            .correlation(data1, means.getFirst(), data2, means.getSecond()));
  }

  @Override
  public DRes<SFixed> ffest(List<List<DRes<SFixed>>> observed) {
    return builder.seq(seq -> new FTest(observed).buildComputation(seq));
  }

  @Override
  public DRes<SFixed> kruskallWallisTest(List<List<DRes<SFixed>>> observed) {
    return builder.seq(seq -> new KruskallWallisTest(KruskallWallisTest.fromSFixed(observed))
        .buildComputation(seq));
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, Integer>>> leakyFrequencyTable(List<DRes<SInt>> data) {
    return builder.seq(seq -> new LeakyFrequencyTable(data).buildComputation(seq));
  }

  @Override
  public DRes<List<Pair<BigInteger, Integer>>> frequencyTable(List<DRes<SInt>> data) {
    // The shuffle method expects a matrix whose height is a two power. If this is not the case, we
    // pad with zeros and remove them afterwards.
    BigInteger paddingValue = BigInteger.ZERO;
    int padding = Integer.bitCount(data.size()) == 1 ? 0 : (Integer.highestOneBit(data.size()) << 1) - data.size();
    return builder.par(par -> {
      List<DRes<SInt>> input = data;

      if (padding > 0) {
        input = new ArrayList<>(data);
        for (int i = 0; i < padding; i++) {
          input.add(par.numeric().known(paddingValue));
        }
      }
      ArrayList<ArrayList<DRes<SInt>>> rows =
          input.stream().map(List::of).map(ArrayList::new)
              .collect(
                  Collectors.toCollection(ArrayList::new));
      return DRes.of(new Matrix<>(input.size(), 1, rows));
    }).seq((seq, columnVector) -> Collections.using(seq).shuffle(DRes.of(columnVector))).seq((seq, shuffledMatrix) -> {
      List<DRes<SInt>> shuffled = VectorUtils.listBuilder(shuffledMatrix.getHeight(),
          i -> shuffledMatrix.getRow(i).get(0));
      return new LeakyFrequencyTable(shuffled).buildComputation(seq);
    }).par((par, frequencyTable) -> DRes.of(frequencyTable.stream().map(pair -> new Pair<>(par.numeric().open(pair.getFirst()), pair.getSecond())).collect(
        Collectors.toList()))).seq((seq, frequencyTable) -> {
      List<Pair<BigInteger, Integer>> uncorrected = frequencyTable.stream().map(pair -> new Pair<>(pair.getFirst().out(), pair.getSecond())).collect(Collectors.toList());
      List<Pair<BigInteger, Integer>> result = new ArrayList<>();
      for (Pair<BigInteger, Integer> frequencyPair : uncorrected) {
        if (!frequencyPair.getFirst().equals(paddingValue)) {
          result.add(frequencyPair);
        } else {
          if (frequencyPair.getSecond() > padding) {
            result.add(new Pair<>(paddingValue, frequencyPair.getSecond() - padding));
          }
        }
      }
      return DRes.of(result);
    });
  }


  @Override
  public DRes<List<DRes<SFixed>>> coxRegressionDiscrete(List<SurvivalInfoDiscrete> data,
      int iterations,
      double alpha, double[] beta) {
    return builder
        .seq(seq -> new CoxRegressionDiscrete(data, iterations, alpha, beta).buildComputation(seq));
  }

  @Override
  public DRes<List<DRes<SFixed>>> coxRegressionContinuous(List<SurvivalInfoContinuous> data,
      int iterations, double alpha, double[] beta) {
    return builder.seq(
        seq -> new CoxRegressionContinuous(data, iterations, alpha, beta).buildComputation(seq));
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> logisticRegression(Matrix<DRes<SFixed>> data,
      ArrayList<DRes<SFixed>> expected, double[] beta, IntToDoubleFunction rate, int epochs) {
    return new LogisticRegression(data, expected, beta, rate, epochs).buildComputation(builder);
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramDiscrete(int[] buckets, List<DRes<SInt>> data) {
    return histogramDiscrete(
        Arrays.stream(buckets).mapToObj(builder.numeric()::known)
            .collect(Collectors.toList()),
        data);
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramDiscrete(List<DRes<SInt>> buckets, List<DRes<SInt>> data) {
    return new Histogram(buckets, data).buildComputation(builder);
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramContinuous(List<DRes<SFixed>> buckets,
      List<DRes<SFixed>> data) {
    return builder.seq(seq -> {
      List<DRes<SInt>> intBuckets =
          buckets.stream().map(bi -> bi.out().getSInt()).collect(Collectors.toList());
      List<DRes<SInt>> intData =
          data.stream().map(xi -> xi.out().getSInt()).collect(Collectors.toList());
      return new DefaultStatistics(seq).histogramDiscrete(intBuckets, intData);
    });
  }

  @Override
  public DRes<List<DRes<SInt>>> histogramContinuous(double[] buckets, List<DRes<SFixed>> data) {
    return histogramContinuous(
        Arrays.stream(buckets).mapToObj(FixedNumeric.using(builder)::known)
            .collect(Collectors.toList()),
        data);
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramDiscrete(
      Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets, List<Pair<DRes<SInt>, DRes<SInt>>> data) {
    return new TwoDimensionalHistogram(buckets, data).buildComputation(builder);
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramContinuous(
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
      return new DefaultStatistics(seq).twoDimensionalHistogramDiscrete(intBuckets, intData);
    });
  }

  @Override
  public DRes<MultiDimensionalArray<DRes<SInt>>> multiDimensionalHistogramDiscrete(
      List<List<DRes<SInt>>> buckets, Matrix<DRes<SInt>> data) {
    return new MultiDimensionalHistogram(buckets, data).buildComputation(builder);
  }

  @Override
  public DRes<MultiDimensionalArray<List<DRes<SInt>>>> kAnonymity(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes, List<List<DRes<SInt>>> buckets, int k) {
    return new KAnonymity(data, sensitiveAttributes, buckets, k).buildComputation(builder);
  }

}
