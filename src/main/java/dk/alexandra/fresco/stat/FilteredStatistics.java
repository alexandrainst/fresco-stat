package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.filtered.OneSampleTTestFiltered.FilteredTTestResult;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import java.math.BigInteger;
import java.util.List;

/**
 * Various analysis methods for filtered data, eg a data set and a bit vector of the same length
 * with entry <i>i</i> indicating whether the <i>i</i>'th data point should be considered.
 */
public interface FilteredStatistics {

  static FilteredStatistics using(ProtocolBuilderNumeric builder) {
    return new DefaultFilteredStatistics(builder);
  }

  /**
   * Compute the sample mean of filtered data set.
   *
   * @param data   A data set
   * @param filter A filter
   * @return The sample mean of the filtered data set
   */
  DRes<SFixed> sampleMean(List<DRes<SFixed>> data, List<DRes<SInt>> filter);

  /**
   * Compute the sample variance of a filtered data set.
   *
   * @param data   A data set
   * @param mean   The precomputed mean of the filtered data ste.
   * @param filter A filter
   * @return The sample variance of the filtered data set.
   */
  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, DRes<SFixed> mean, List<DRes<SInt>> filter);

  /**
   * Compute the sample variance of a filtered data set.
   *
   * @param data   A data set
   * @param filter A filter
   * @return The sample variance of the filtered data set
   */
  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, List<DRes<SInt>> filter);

  /**
   * Compute the test statistics for a student t-test on the filtered data set.
   *
   * @param data   A data set
   * @param mu     The parameter for the t-test, eg. the mean under the null hypothesis
   * @param filter A filter
   * @return A pair contaning the test statistics for the test and the number of elements in the
   * filtered data set.
   */
  DRes<FilteredTTestResult> ttest(List<DRes<SFixed>> data, DRes<SFixed> mu, List<DRes<SInt>> filter);

  /**
   * Compute a histogram on a filtered data set. Note that upper limits are soft, lower are hard.
   *
   * @param buckets The buckets for the histogram
   * @param data    A data set
   * @param filter  A filter
   * @return A list containing the number of elements of the filtered data set in each bucket.
   */
  DRes<List<DRes<SInt>>> histogram(List<DRes<SInt>> buckets, List<DRes<SInt>> data,
      List<DRes<SInt>> filter);

  /**
   * Compute a k-anonymized version of the given filtered datset.
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
   * @param sensitiveAttributes The corresponding sensitive attributes. Must be non-zero.
   * @param buckets             The buckets defining the desired generalization.
   * @param k                   The smallest allowed number of individuals in each bucket.
   * @param filter              A filter.
   *
   * @return A k-anonymous data set with all buckets with fewer than k elements suppressed.
   */
  DRes<MultiDimensionalArray<List<DRes<SInt>>>> kAnonymize(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes,
      List<List<DRes<SInt>>> buckets, int k, List<DRes<SInt>> filter);

  /**
   * Compute a k-anonymized version of the given filtered datset.
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
   * @param sensitiveAttributes The corresponding sensitive attributes. Must be non-zero.
   * @param buckets             The buckets defining the desired generalization.
   * @param k                   The smallest allowed number of individuals in each bucket.
   * @param filter              A filter.
   *
   * @return A k-anonymous data set with all buckets with fewer than k elements suppressed.
   */
  DRes<MultiDimensionalArray<List<BigInteger>>> kAnonymizeAndOpen(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes, List<List<DRes<SInt>>> buckets, int k, List<DRes<SInt>> filter);


}