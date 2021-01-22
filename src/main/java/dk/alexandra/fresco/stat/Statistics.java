package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.regression.SimpleLinearRegression.LinearFunction;
import dk.alexandra.fresco.stat.survival.SurvivalInfoContinuous;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import java.util.List;

public interface Statistics extends ComputationDirectory {

  static Statistics using(ProtocolBuilderNumeric builder) {
    return new DefaultStatistics(builder);
  }

  /**
   * Compute the sample mean of the given data.
   *
   * @param data
   * @return
   */
  DRes<SFixed> sampleMean(List<DRes<SFixed>> data);

  /**
   * Compute the sample variance of the given data, assuming the sample mean has already been
   * calculated.
   *
   * @param data
   * @param mean
   * @return
   */
  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, DRes<SFixed> mean);

  /**
   * Compute the sample variance of the given data.
   *
   * @param data
   * @return
   */
  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data);

  /**
   * Compute the sample standard deviation of the data given that the sample mean has already been
   * calculated.
   *
   * @param data
   * @param mean
   * @return
   */
  DRes<SFixed> sampleStandardDeviation(List<DRes<SFixed>> data, DRes<SFixed> mean);

  /**
   * Compute the standard deviation of the data.
   *
   * @param data
   * @return
   */
  DRes<SFixed> sampleStandardDeviation(List<DRes<SFixed>> data);

  /**
   * Compute the test statistics for a Student's t-test for the hypothesis that the mean of the
   * sample is equal to <code>mu</code>.
   *
   * @param data
   * @param mu
   * @return
   */
  DRes<SFixed> ttest(List<DRes<SFixed>> data, DRes<SFixed> mu);

  /**
   * Compute the test statistics for a two-sample Student's t-test for the hypothesis that the mean
   * of the two samples are equal. It is assumed that the two samples have the same variance.
   *
   * @param data1
   * @param data2
   * @return
   */
  DRes<SFixed> ttest(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test.
   *
   * @param observed
   * @param expected
   * @return
   */
  DRes<SFixed> chiSquare(List<DRes<SInt>> observed, List<DRes<SFixed>> expected);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test.
   *
   * @param observed
   * @param expected
   * @return
   */
  DRes<SFixed> chiSquare(List<DRes<SInt>> observed, double[] expected);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test on the given data.
   *
   * @param data     Data points
   * @param buckets  Upper bounds for the buckets to consider in the test.
   * @param expected The expected number of elements to end up in each bucket. Note that there
   *                 should be one more element in buckets than in expected.
   * @return
   */
  DRes<SFixed> chiSquare(List<DRes<SFixed>> data, List<DRes<SFixed>> buckets,
      List<DRes<SFixed>> expected);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test on the given data.
   *
   * @param data     Data points
   * @param buckets  Upper bounds for the buckets to consider in the test.
   * @param expected The expected number of elements to end up in each bucket. Note that there
   *                 should be one more element in buckets than in expected.
   * @return
   */
  DRes<SFixed> chiSquare(List<DRes<SFixed>> data, List<DRes<SFixed>> buckets, double[] expected);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test on the given data.
   *
   * @param data     Data points
   * @param buckets  Upper bounds for the buckets to consider in the test.
   * @param expected The expected number of elements to end up in each bucket. Note that there
   *                 should be one more element in buckets than in expected.
   * @return
   */
  DRes<SFixed> chiSquare(List<DRes<SFixed>> data, double[] buckets, double[] expected);

  /**
   * Compute simple linear regression on two samples.
   *
   * @param x
   * @param y
   * @return
   */
  DRes<LinearFunction> linearRegression(List<DRes<SFixed>> x, List<DRes<SFixed>> y);

  /**
   * Compute simple linear regression on two samples assuming that the means has already been
   * calculated.
   *
   * @param x
   * @param y
   * @return
   */
  DRes<LinearFunction> linearRegression(List<DRes<SFixed>> x, DRes<SFixed> meanX,
      List<DRes<SFixed>> y,
      DRes<SFixed> meanY);

  /**
   * Compute Pearson's correlation coefficient on the two samples. Here it's assumed that the sample
   * means has already been calculated.
   *
   * @param data1
   * @param mean1
   * @param data2
   * @param mean2
   * @return
   */
  DRes<SFixed> correlation(List<DRes<SFixed>> data1, DRes<SFixed> mean1, List<DRes<SFixed>> data2,
      DRes<SFixed> mean2);

  /**
   * Compute Pearson's correlation coefficient on the two samples.
   *
   * @param data1
   * @param data2
   * @return
   */
  DRes<SFixed> correlation(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2);

  /**
   * Compute the histogram for the given sample.
   *
   * @param buckets Upper bound for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<List<DRes<SInt>>> histogramInt(List<DRes<SInt>> buckets, List<DRes<SInt>> data);

  /**
   * Compute the histogram for the given sample.
   *
   * @param buckets Upper bound for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<List<DRes<SInt>>> histogramReal(List<DRes<SFixed>> buckets, List<DRes<SFixed>> data);

  /**
   * Compute the histogram for the given sample.
   *
   * @param buckets Upper bound for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<List<DRes<SInt>>> histogramReal(double[] buckets, List<DRes<SFixed>> data);

  /**
   * Compute the histogram for the given two-dimensional sample.
   *
   * @param buckets Upper bounds for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramInt(
      Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets,
      List<Pair<DRes<SInt>, DRes<SInt>>> data);

  /**
   * Compute the histogram for the given two-dimensional sample.
   *
   * @param buckets Upper bounds for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramReal(
      Pair<List<DRes<SFixed>>, List<DRes<SFixed>>> buckets,
      List<Pair<DRes<SFixed>, DRes<SFixed>>> data);

  /**
   * Compute the F-test statistics for the null hypothesis that the given samples are drawn from
   * populations with the same mean.
   *
   * @param observed A list of samples.
   * @return
   */
  DRes<SFixed> ffest(List<List<DRes<SFixed>>> observed);

  /**
   * Compute the Kruskall-Wallis test statistics for the null hypothesis that the given samples are
   * drawn from the distribution.
   *
   * @param observed
   * @return
   */
  DRes<SFixed> kruskallWallisTest(List<List<DRes<SFixed>>> observed);

  /**
   * Compute a frequency table for the data. Note that the frequencies will be leaked even though
   * the corresponding values will not.
   *
   * @param data
   * @return
   */
  DRes<List<Pair<DRes<SInt>, Integer>>> leakyFrequencies(List<DRes<SInt>> data);

  /**
   * Estimate the parameters of a Cox model on the given data. Here it's assumed that each covariate
   * only takes values in a (small) finite set, e.g. when they indicate group membership. If many
   * different values are possible, use {@link #coxRegressionContinuous(List)} instead;
   *
   * @param data
   * @return
   */
  DRes<List<DRes<SFixed>>> coxRegressionDiscrete(List<SurvivalInfoDiscrete> data);

  /**
   * Estimate the parameters of a Cox model on the given data.
   *
   * @param data       The data set.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   * @param beta       The initial coefficient guess.
   * @return
   */
  DRes<List<DRes<SFixed>>> coxRegressionDiscrete(List<SurvivalInfoDiscrete> data,
      int iterations, double alpha, double[] beta);

  /**
   * Estimate the parameters of a Cox model on the given data.
   *
   * @param data
   * @return
   */
  DRes<List<DRes<SFixed>>> coxRegressionContinuous(List<SurvivalInfoContinuous> data);

  /**
   * Estimate the parameters of a Cox model on the given data.
   *
   * @param data       The data set.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   * @param beta       The initial coefficient guess.
   * @return
   */
  DRes<List<DRes<SFixed>>> coxRegressionContinuous(List<SurvivalInfoContinuous> data,
      int iterations, double alpha, double[] beta);
}
