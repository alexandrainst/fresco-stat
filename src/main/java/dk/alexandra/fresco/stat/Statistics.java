package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.util.SIntPair;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression.LinearRegressionResult;
import dk.alexandra.fresco.stat.regression.linear.SimpleLinearRegression.SimpleLinearRegressionResult;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.survival.cox.CoxRegression.CoxRegressionResult;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * This computation library contains various statistical functions.
 */
public interface Statistics extends ComputationDirectory {

  static Statistics using(ProtocolBuilderNumeric builder) {
    return new DefaultStatistics(builder);
  }

  /**
   * Compute the sample mean of the given data.
   *
   * @param data A dataset.
   * @return The sample mean.
   */
  DRes<SFixed> sampleMean(List<DRes<SFixed>> data);

  /**
   * Compute the sample median of the sample set.
   *
   * @param data Samples.
   * @return The median.
   */
  DRes<SFixed> sampleMedian(List<DRes<SFixed>> data);

  /**
   * Compute the sample percentiles of a sample set.
   *
   * @param data Samples.
   * @return The median.
   */
  DRes<List<DRes<SFixed>>> samplePercentiles(List<DRes<SFixed>> data, double[] quantiles);


  /**
   * Compute the sample variance of the given data, assuming the sample mean has already been
   * calculated.
   *
   * @param data A dataset.
   * @param mean The sample mean for the given dataset.
   * @return The sample variance.
   */
  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, DRes<SFixed> mean);

  /**
   * Compute the sample variance of the given data.
   *
   * @param data A dataset.
   * @return The sample variance.
   */
  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data);

  /**
   * Compute the sample standard deviation of the data given that the sample mean has already been
   * calculated.
   *
   * @param data A dataset.
   * @param mean The sample mean for the given dataset.
   * @return The sample standard deviation.
   */
  DRes<SFixed> sampleStandardDeviation(List<DRes<SFixed>> data, DRes<SFixed> mean);

  /**
   * Compute the standard deviation of the data.
   *
   * @param data A dataset.
   * @return The sample standard deviation.
   */
  DRes<SFixed> sampleStandardDeviation(List<DRes<SFixed>> data);

  /**
   * Compute the test statistics for a Student's t-test for the hypothesis that the mean of the
   * sample is equal to <code>mu</code>.
   *
   * @param data A dataset.
   * @param mu   The parameter for the t-test.
   * @return The test statistics.
   */
  DRes<SFixed> ttest(List<DRes<SFixed>> data, DRes<SFixed> mu);

  /**
   * Compute the test statistics for a two-sample Student's t-test for the hypothesis that the mean
   * of the two samples are equal. It is assumed that the two samples have the same variance.
   *
   * @param data1 A dataset.
   * @param data2 A dataset.
   * @return The test statistics for the hypothesis that the two datasets have the same mean.
   */
  DRes<SFixed> ttest(List<DRes<SFixed>> data1, List<DRes<SFixed>> data2);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test.
   *
   * @param observed The observed data.
   * @param expected The expected number of observations in each bucket.
   * @return The test statistics that the observed data fits the distribution of the expected.
   */
  DRes<SFixed> chiSquare(List<DRes<SInt>> observed, List<DRes<SFixed>> expected);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test.
   *
   * @param observed The observed data.
   * @param expected The expected number of observations in each bucket.
   * @return The test statistics that the observed data fits the distribution of the expected.
   */
  DRes<SFixed> chiSquare(List<DRes<SInt>> observed, double[] expected);

  /**
   * Compute estimates for the parameters <i>b</i> of a linear model such that <i>b0 x0 + ... + bk
   * xk = y</i>.
   *
   * @param x The dataset.
   * @param y The dependant values
   * @return An estimation for the parameters of a linear model for the given data.
   */
  DRes<LinearRegressionResult> linearRegression(List<ArrayList<DRes<SFixed>>> x,
      ArrayList<DRes<SFixed>> y);

  /**
   * Compute simple linear regression on two samples.
   *
   * @param x The dataset.
   * @param y The dependant values.
   * @return An estimation for the parameters of a linear model.
   */
  DRes<SimpleLinearRegressionResult> simpleLinearRegression(List<DRes<SFixed>> x,
      List<DRes<SFixed>> y);

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
   * Compute the F-test statistics for the null hypothesis that the given datasets have the same
   * mean.
   *
   * @param observed A list of datasets.
   * @return The test statistics.
   */
  DRes<SFixed> ffest(List<List<DRes<SFixed>>> observed);

  /**
   * Compute the Kruskall-Wallis test statistics for the null hypothesis that the given samples are
   * drawn from same the distribution.
   *
   * @param observed
   * @return
   */
  DRes<SFixed> kruskallWallisTest(List<List<DRes<SFixed>>> observed);

  /**
   * Compute a frequency table for the data. Note that the frequencies will be leaked but the
   * corresponding values will not.
   *
   * @param data A dataset
   * @return A frequency table.
   */
  DRes<List<Pair<DRes<SInt>, Integer>>> leakyFrequencyTable(List<DRes<SInt>> data);

  /**
   * Compute a frequency table for the data.
   *
   * @param data A dataset
   * @return A frequency table.
   */
  DRes<List<Pair<BigInteger, Integer>>> frequencyTable(List<DRes<SInt>> data);

//  /**
//   * Estimate the parameters of a Cox model on the given data. Here it's assumed that each covariate
//   * only takes values in a (small) finite set, e.g. when they indicate group membership. If many
//   * different values are possible, use {@link #coxRegressionContinuous} instead.
//   *
//   * @param data       The data set.
//   * @param iterations The number of iterations.
//   * @param alpha      The learning rate.
//   * @param beta       The initial coefficient guess.
//   * @return
//   */
//  CoxRegressionResult coxRegressionDiscrete(List<SurvivalInfoDiscrete> data,
//      int iterations, double alpha, double[] beta);

  /**
   * Estimate the parameters of a Cox model on the given data.
   *
   * @param data       The data set.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   * @param beta       The initial coefficient guess.
   * @return
   */
  DRes<CoxRegressionResult> coxRegressionContinuous(List<SurvivalEntry> data,
      int iterations, double alpha, double[] beta);

  /**
   * Compute the histogram for the given sample.
   *
   * @param buckets Upper bound for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<List<DRes<SInt>>> histogramDiscrete(int[] buckets, List<DRes<SInt>> data);

  /**
   * Compute the histogram for the given sample.
   *
   * @param buckets Upper bound for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<List<DRes<SInt>>> histogramContinuous(double[] buckets, List<DRes<SFixed>> data);

  /**
   * Compute the histogram for the given sample.
   *
   * @param buckets Upper bound for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<List<DRes<SInt>>> histogramDiscrete(List<DRes<SInt>> buckets, List<DRes<SInt>> data);

  /**
   * Compute the histogram for the given sample.
   *
   * @param buckets Upper bound for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<List<DRes<SInt>>> histogramContinuous(List<DRes<SFixed>> buckets, List<DRes<SFixed>> data);

  /**
   * Compute the histogram for the given two-dimensional sample.
   *
   * @param buckets Upper bounds for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramDiscrete(
      Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets,
      List<Pair<DRes<SInt>, DRes<SInt>>> data);

  /**
   * Compute the histogram for the given two-dimensional sample.
   *
   * @param buckets Upper bounds for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<Matrix<DRes<SInt>>> twoDimensionalHistogramContinuous(
      Pair<List<DRes<SFixed>>, List<DRes<SFixed>>> buckets,
      List<Pair<DRes<SFixed>, DRes<SFixed>>> data);

  /**
   * Compute the histogram for the given multi-dimensional sample.
   *
   * @param buckets Upper bounds for the buckets to use in the histogram.
   * @param data    The sample data.
   * @return
   */
  DRes<MultiDimensionalArray<DRes<SInt>>> multiDimensionalHistogramDiscrete(
      List<List<DRes<SInt>>> buckets,
      Matrix<DRes<SInt>> data);

  /**
   * Compute a k-anonymized version of the given datset.
   * <p>
   * Each row in the data set are the quasi-identifiers of an individual with a corresponding entry
   * in the list of values of the sensitive attribute. The buckets indicates the desired
   * generalization of the quasi-identifiers as in a histogram. K is the smallest allowed number of
   * individuals in each bucket.
   * <p>
   * The output is a histogram on the given buckets with the value in the histogram being a list of
   * size data.getHeight() with a non-zero entry <i>x</i> at index <i>i</i> indicating that the data
   * point at row <i>i</i> is in this bucket and that the corresponding sensitive attribute was
   * <i>x</i>.
   *
   * @param data                The quasi identifiers for each individual.
   * @param sensitiveAttributes The corresponding sensitive attributes. Must be non-zero
   * @param buckets             The buckets defining the desired generalization.
   * @param k                   The smallest allowed number of individuals in each bucket.
   * @return A k-anonymous data set with all buckets with fewer than k elements suppressed.
   */
  DRes<MultiDimensionalArray<List<DRes<SInt>>>> kAnonymize(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes,
      List<List<DRes<SInt>>> buckets, int k);

  /**
   * Compute a k-anonymized version of the given dataset and open it to all parties.
   * <p>
   * Each row in the data set are the quasi-identifiers of an individual with a corresponding entry
   * in the list of values of the sensitive attribute. The buckets indicates the desired
   * generalization of the quasi-identifiers as in a histogram. K is the smallest allowed number of
   * individuals in each bucket.
   * <p>
   * The output is a histogram on the given buckets with the value corresponding to a bucket is a
   * list of the sensitive attributes from the original dataset which ended up in this bucket.
   *
   * @param data                The quasi identifiers for each individual.
   * @param sensitiveAttributes The corresponding sensitive attributes. Must be non-zero.
   * @param buckets             The buckets defining the desired generalization.
   * @param k                   The smallest allowed number of individuals in each bucket.
   * @return A k-anonymous data set with all buckets with fewer than k elements suppressed.
   */
  DRes<MultiDimensionalArray<List<BigInteger>>> kAnonymizeAndOpen(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes, List<List<DRes<SInt>>> buckets, int k);


  /**
   * Compute the <a href="https://en.wikipedia.org/wiki/Mahalanobis_distance">Mahalanobis
   * Distance</a> of all samples in a data set. This may be used to detect outliers in the data set
   * which may be filtered out obliviously (see {@link dk.alexandra.fresco.stat.FilteredStatistics}
   * before using the data set for analysis.
   *
   * @param X The data set
   */
  DRes<ArrayList<DRes<SFixed>>> mahalanobisDistance(List<List<DRes<SFixed>>> X);

  /**
   * Given a list of observations, each consisting of two categorical values <i>(x,y)</i> with <i>0
   * &le; firstRange</i> and <i>0 &le; y < secondRange</i>, this computation outputs a contingency
   * table of size <i>firstRange x secondRange</i>, where the <i>(i,j)</i>'th entry is the number of
   * observations such that <i>x = i</i> and <i>y = j</i>.
   *
   * <p>Note that it is much more efficient to encode the data using indicator vectors and use
   * {@link #contingencyTable(List)} instead if possible.</p>
   *
   * @param data        The observations encoded as pairs of secret integers
   * @param firstRange  The range of the first attribute
   * @param secondRange The range of the second attribute
   * @return A contingency table where the <i>(i,j)</i>'th entry is the number of observations
   * <i>(x,y)</i> such that <i>x = i</i> and <i>y = j</i>.
   */
  DRes<Matrix<DRes<SInt>>> contingencyTable(List<SIntPair> data, int firstRange, int secondRange);


  /**
   * Compute a contingency table for a list of observations with two categorical variables encoded
   * as follows: Each row is an observation which consists of two attributes, each of which is
   * encoded as a 0-1 vector with exactly one non-zero entry to indicate the value of the
   * attribute.
   *
   * @param data A list of observations encoded as 0-1 indicator vectors
   * @return A contingency table where the <i>(i,j)</i>'th entry is the number of observations
   * <i>(x,y)</i> such that <i>x = i</i> and <i>y = j</i>.
   */
  DRes<Matrix<DRes<SInt>>> contingencyTable(List<Pair<List<DRes<SInt>>, List<DRes<SInt>>>> data);

}
