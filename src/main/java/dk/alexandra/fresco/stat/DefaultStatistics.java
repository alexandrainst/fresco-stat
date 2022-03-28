package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.util.SIntPair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.anonymisation.LeakyKAnonymity;
import dk.alexandra.fresco.stat.descriptive.ContingencyTable;
import dk.alexandra.fresco.stat.descriptive.ContingencyTableCategorical;
import dk.alexandra.fresco.stat.descriptive.Histogram;
import dk.alexandra.fresco.stat.descriptive.LeakyFrequencyTable;
import dk.alexandra.fresco.stat.descriptive.MultiDimensionalHistogram;
import dk.alexandra.fresco.stat.descriptive.PearsonCorrelation;
import dk.alexandra.fresco.stat.descriptive.SampleMean;
import dk.alexandra.fresco.stat.descriptive.SampleMedian;
import dk.alexandra.fresco.stat.descriptive.SampleQuantiles;
import dk.alexandra.fresco.stat.descriptive.SampleStandardDeviation;
import dk.alexandra.fresco.stat.descriptive.SampleVariance;
import dk.alexandra.fresco.stat.descriptive.TwoDimensionalHistogram;
import dk.alexandra.fresco.stat.outlier.MahalanobisDistance;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import dk.alexandra.fresco.stat.regression.linear.SimpleLinearRegression;
import dk.alexandra.fresco.stat.regression.linear.SimpleLinearRegression.SimpleLinearRegressionResult;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.survival.cox.CoxRegression;
import dk.alexandra.fresco.stat.survival.cox.CoxRegression.CoxRegressionResult;
import dk.alexandra.fresco.stat.tests.ChiSquareTest;
import dk.alexandra.fresco.stat.tests.FTest;
import dk.alexandra.fresco.stat.tests.KruskallWallisTest;
import dk.alexandra.fresco.stat.tests.OneSampleTTest;
import dk.alexandra.fresco.stat.tests.TwoSampleTTest;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultStatistics implements Statistics {

  private final ProtocolBuilderNumeric builder;

  DefaultStatistics(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SFixed> sampleMean(List<DRes<SFixed>> data) {
    return new SampleMean(data).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleMedian(List<DRes<SFixed>> data) {
    return new SampleMedian(data).buildComputation(builder);
  }

  @Override
  public DRes<List<DRes<SFixed>>> samplePercentiles(List<DRes<SFixed>> data, double[] percentiles) {
    return new SampleQuantiles(data, percentiles).buildComputation(builder);
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
    int padding = Integer.bitCount(data.size()) == 1 ? 0
        : (Integer.highestOneBit(data.size()) << 1) - data.size();
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
    }).seq((seq, columnVector) -> Collections.using(seq).shuffle(DRes.of(columnVector)))
        .seq((seq, shuffledMatrix) -> {
          List<DRes<SInt>> shuffled = VectorUtils.listBuilder(shuffledMatrix.getHeight(),
              i -> shuffledMatrix.getRow(i).get(0));
          return new LeakyFrequencyTable(shuffled).buildComputation(seq);
        }).par((par, frequencyTable) -> DRes.of(frequencyTable.stream()
            .map(pair -> new Pair<>(par.numeric().open(pair.getFirst()), pair.getSecond())).collect(
                Collectors.toList()))).seq((seq, frequencyTable) -> {
          List<Pair<BigInteger, Integer>> uncorrected = frequencyTable.stream()
              .map(pair -> new Pair<>(pair.getFirst().out(), pair.getSecond()))
              .collect(Collectors.toList());
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


/*
  @Override
  public DRes<List<DRes<SFixed>>> coxRegressionDiscrete(List<SurvivalInfoDiscrete> data,
      int iterations,
      double alpha, double[] beta) {
    return builder
        .seq(seq -> new CoxRegressionDiscrete(data, iterations, alpha, beta).buildComputation(seq));
  }
*/

  @Override
  public DRes<CoxRegressionResult> coxRegressionContinuous(List<SurvivalEntry> data,
      int iterations, double alpha, double[] beta) {
    return builder.seq(
        seq -> new CoxRegression(data, iterations, alpha, beta).buildComputation(seq));
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
  public DRes<MultiDimensionalArray<List<DRes<SInt>>>> kAnonymize(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes, List<List<DRes<SInt>>> buckets, int k) {
    // The shuffle method expects a matrix whose height is a two power. If this is not the case, we
    // pad with zero rows and put an extra indicator attribute to be able to remove them afterwards.

    // Smallest representable value
    BigInteger paddingValue = BigInteger.valueOf(0);
    BigInteger paddingSensitive = BigInteger.valueOf(0);

    // Distance to next power of two
    int padding = Integer.bitCount(data.getHeight()) == 1 ? 0
        : (Integer.highestOneBit(data.getHeight()) << 1) - data.getHeight();
    boolean pad = padding > 0;


    return builder.par(par -> {

      // Create a matrix consisting of quasi identifiers, padding indicator and sensitive attributes
      // | quasi identifiers | padded? | sensitive |
      ArrayList<ArrayList<DRes<SInt>>> rows = new ArrayList<>();

      for (int i = 0; i < data.getHeight(); i++) {
        ArrayList<DRes<SInt>> row = new ArrayList<>(data.getRow(i));

        // Add padding indicator attribute. If the size happens to be a power of two, this may be removed
        if (pad) {
          row.add(par.numeric().known(0));
        }
        row.add(sensitiveAttributes.get(i));
        rows.add(row);
      }

      if (pad) {
        for (int i = 0; i < padding; i++) {
          ArrayList<DRes<SInt>> row = IntStream.range(0, data.getWidth())
              .mapToObj(j -> par.numeric().known(paddingValue))
              .collect(Collectors.toCollection(ArrayList::new));
          row.add(par.numeric().known(1)); // Add padding indicator attribute
          row.add(par.numeric().known(paddingSensitive));
          rows.add(row);
        }
      }
      return DRes.of(new Matrix<>(rows.size(), rows.get(0).size(), rows));
    }).seq((seq, columnVector) -> {
      // Shuffle the matrix we created above
      return Collections.using(seq).shuffle(DRes.of(columnVector));
    }).seq((seq, shuffledMatrix) -> {
      // Extract matrix of quasi-identifiers and list of sensitive attributes from shuffled matrix
      Matrix<DRes<SInt>> shuffledData = MatrixUtils
          .subMatrix(shuffledMatrix, 0, shuffledMatrix.getHeight(), 0,
              shuffledMatrix.getWidth() - 1);
      List<DRes<SInt>> shuffledSensitive = shuffledMatrix.getColumn(shuffledMatrix.getWidth() - 1);
      List<List<DRes<SInt>>> bucketsWithIndicator = new ArrayList<>(buckets);
      bucketsWithIndicator.add(List.of(seq.numeric().known(0)));

      // Run "leaky" k-anonymity algorithm on these inputs. Indices are
      return seq.seq(new LeakyKAnonymity(shuffledData, shuffledSensitive, bucketsWithIndicator, k));
    }).par((par, kAnonymousData) -> {
      MultiDimensionalArray<List<DRes<SInt>>> uncorrected = kAnonymousData.project(l -> l.get(0));
      return DRes.of(uncorrected);
    });
  }

  @Override
  public DRes<MultiDimensionalArray<List<BigInteger>>> kAnonymizeAndOpen(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes, List<List<DRes<SInt>>> buckets, int k) {
    return builder
        .seq(seq -> Statistics.using(seq).kAnonymize(data, sensitiveAttributes, buckets, k))
        .par((par, kAnonymousData) -> DRes.of(kAnonymousData.map(x -> VectorUtils.open(x, par))))
        .par((par, kAnonymousData) -> DRes.of(kAnonymousData.map(h -> h.stream().map(DRes::out)
            .filter(x -> !x.equals(BigInteger.ZERO)).collect(Collectors.toList()))));
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> mahalanobisDistance(List<List<DRes<SFixed>>> X) {
    return builder.seq(new MahalanobisDistance(X));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> contingencyTable(List<SIntPair> data, int firstRange,
      int secondRange) {
    return builder.seq(new ContingencyTableCategorical(data, firstRange, secondRange));
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> contingencyTable(
      List<Pair<List<DRes<SInt>>, List<DRes<SInt>>>> data) {
    return builder.seq(new ContingencyTable(data));
  }
}