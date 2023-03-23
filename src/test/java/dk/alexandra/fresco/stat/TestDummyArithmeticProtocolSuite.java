package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.field.MersennePrimeFieldDefinition;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestContingencyTable;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestCovariance;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestHistogramContinuous;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestHistogramDiscrete;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestHistogramDiscrete2;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestKAnonymity;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestKAnonymityOpen;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestMahalanobisDistance;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestMean;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestMedian;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestMultiDimHistogram;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestNoisyHistogram;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestPercentiles;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestStandardDeviation;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestTwoDimHistogram;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestVariance;
import dk.alexandra.fresco.stat.FilteredStatTests.TestFilteredHistogram;
import dk.alexandra.fresco.stat.FilteredStatTests.TestFilteredKAnonymity;
import dk.alexandra.fresco.stat.FilteredStatTests.TestFilteredMean;
import dk.alexandra.fresco.stat.FilteredStatTests.TestFilteredTTest;
import dk.alexandra.fresco.stat.FilteredStatTests.TestFilteredVariance;
import dk.alexandra.fresco.stat.LATests.TestBackSubstitution;
import dk.alexandra.fresco.stat.LATests.TestConvolution;
import dk.alexandra.fresco.stat.LATests.TestEigenvalues;
import dk.alexandra.fresco.stat.LATests.TestForwardSubstitution;
import dk.alexandra.fresco.stat.LATests.TestGramSchmidt;
import dk.alexandra.fresco.stat.LATests.TestLinearInverseProblem;
import dk.alexandra.fresco.stat.LATests.TestLinearInverseProblemNoSolution;
import dk.alexandra.fresco.stat.LATests.TestLinearInverseProblemOverdetermined;
import dk.alexandra.fresco.stat.LATests.TestLinearInverseProblemUnderdetermined;
import dk.alexandra.fresco.stat.LATests.TestMoorePenrosePseudoInverse;
import dk.alexandra.fresco.stat.LATests.TestQRDcomposition;
import dk.alexandra.fresco.stat.LATests.TestTriangularInverse;
import dk.alexandra.fresco.stat.LinRegTests.TestLinearRegression;
import dk.alexandra.fresco.stat.LinRegTests.TestLinearRegressionLarge;
import dk.alexandra.fresco.stat.LinRegTests.TestNoisySimpleLinearRegression;
import dk.alexandra.fresco.stat.LinRegTests.TestSimpleLinearRegression;
import dk.alexandra.fresco.stat.LogRegTests.TestLogRegPrediction;
import dk.alexandra.fresco.stat.LogRegTests.TestLogRegSGDSingleEpoch;
import dk.alexandra.fresco.stat.LogRegTests.TestLogisticRegression;
import dk.alexandra.fresco.stat.Optimisation.TestMaximum;
import dk.alexandra.fresco.stat.Optimisation.TestMinimum;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxGradient;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxHessian;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxRegression;
import dk.alexandra.fresco.stat.TestsTests.TestChiSquareTest;
import dk.alexandra.fresco.stat.TestsTests.TestChiSquareTestKnown;
import dk.alexandra.fresco.stat.TestsTests.TestFTest;
import dk.alexandra.fresco.stat.TestsTests.TestKruskallWallis;
import dk.alexandra.fresco.stat.TestsTests.TestKruskallWallisFixedPoint;
import dk.alexandra.fresco.stat.TestsTests.TestTTest;
import dk.alexandra.fresco.stat.TestsTests.TestTwoSampleTTest;
import dk.alexandra.fresco.stat.TestsTests.TestTwoSampleTTestDifferentSizes;
import dk.alexandra.fresco.stat.TestsTests.TestWilcoxonTest;
import dk.alexandra.fresco.stat.UtilTests.TestArgMax;
import dk.alexandra.fresco.stat.UtilTests.TestIndicator;
import dk.alexandra.fresco.stat.UtilTests.TestMax;
import dk.alexandra.fresco.stat.UtilTests.TestParallelIndicator;
import dk.alexandra.fresco.stat.UtilTests.TestProduct;
import dk.alexandra.fresco.stat.mlp.NNTests.TestBackwardPropagation;
import dk.alexandra.fresco.stat.mlp.NNTests.TestFit;
import dk.alexandra.fresco.stat.mlp.NNTests.TestForwardPropagation;
import dk.alexandra.fresco.stat.mlp.NNTests.TestPrediction;
import dk.alexandra.fresco.stat.mlp.NNTests.TestSingleStepTraining;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.Ignore;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  private static final TestParameters TEST_PARAMETERS = new TestParameters().maxBitLength(400)
      .field(MersennePrimeFieldDefinition.find(512))
      .fixedPointPrecesion(32).evaluationStrategy(EvaluationStrategy.SEQUENTIAL).numParties(2)
      .performanceLogging(false);

  @Test
  public void test_mean() {
    runTest(new TestMean<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_median() {
    runTest(new TestMedian<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_percentiles() {
    runTest(new TestPercentiles<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_variance() {
    runTest(new TestVariance<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_covariance() {
    runTest(new TestCovariance<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_mahalanobis_distance() {
    runTest(new TestMahalanobisDistance<>(), TEST_PARAMETERS.field(MersennePrimeFieldDefinition.find(512)).maxBitLength(400));
  }

  @Test
  public void test_standard_deviation() {
    runTest(new TestStandardDeviation<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_T_Test() {
    runTest(new TestTTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_two_sample_T_Test() {
    runTest(new TestTwoSampleTTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_two_sample_T_Test_different_sizes() {
    runTest(new TestTwoSampleTTestDifferentSizes<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test() {
    runTest(new TestChiSquareTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test_known() {
    runTest(new TestChiSquareTestKnown<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_simple_linear_regression() {
    runTest(new TestSimpleLinearRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_noisy_simple_linear_regression() {
    runTest(new TestNoisySimpleLinearRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_linear_regression() {
    runTest(new TestLinearRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_large_linear_regression() {
    runTest(new TestLinearRegressionLarge<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_correlation() {
    runTest(new DescriptiveStatTests.TestCorrelation<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_categorical_distribution_sampling() {
    double[] p = new double[]{0.1, 0.2, 0.5, 0.2};

    runTest(
        new TestDiscreteDistribution<>(200,
            seq -> Sampler.using(seq).sampleCategoricalDistribution(p), p, 0.1),
        TEST_PARAMETERS);
  }

  @Test
  public void test_categorical_distribution_sampling_secret_probabilities() {
    double[] p = new double[]{0.1, 0.2, 0.5, 0.2};

    runTest(
        new TestDiscreteDistribution<>(200, seq -> {
          ArrayList<DRes<SFixed>> secretP = VectorUtils
              .listBuilder(p.length, i -> FixedNumeric.using(seq).input(p[i], 1));
          return Sampler.using(seq).sampleCategoricalDistribution(secretP);
        }, p, 0.1),
        TEST_PARAMETERS);
  }

  @Test
  public void test_categorical_distribution_sampling_non_normalized_secret_probabilities() {
    double[] p = new double[]{0.1, 0.2, 0.7, 0.9};

    double sum = Arrays.stream(p).sum();
    double[] normalizedP = Arrays.stream(p).map(pi -> pi / sum).toArray();

    runTest(
        new TestDiscreteDistribution<>(200, seq -> {
          ArrayList<DRes<SFixed>> secretP = VectorUtils
              .listBuilder(p.length, i -> FixedNumeric.using(seq).input(p[i], 1));
          return Sampler.using(seq).sampleCategoricalDistribution(secretP, false);
        }, p, 0.1),
        TEST_PARAMETERS);
  }

  @Test
  public void test_bernoulli_distribution_sampling() {
    double[] p = new double[]{0.6, 0.4};

    runTest(
        new TestDiscreteDistribution<>(200,
            seq -> Sampler.using(seq).sampleBernoulliDistribution(0.6), p, 0.05),
        TEST_PARAMETERS);
  }

  @Test
  public void test_bernoulli_distribution_sampling_secret_param() {
    double[] p = new double[]{0.6, 0.4};

    runTest(
        new TestDiscreteDistribution<>(200,
            seq -> Sampler.using(seq)
                .sampleBernoulliDistribution(FixedNumeric.using(seq).known(0.6)),
            p, 0.05),
        TEST_PARAMETERS);
  }

  @Test
  public void test_laplace_distribution_sampling() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleLaplaceDistribution(1.5),
        new LaplaceDistribution(0, 1.5), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_laplace_distribution_sampling_secret_param() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleLaplaceDistribution(FixedNumeric.using(seq).known(1.5)),
        new LaplaceDistribution(0, 1.5), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_uniform_distribution_sampling() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleUniformDistribution(),
        new UniformRealDistribution(0, 1), 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_uniform_distribution_mean() {
    runTest(new TestContinuousDistributionMean<>(1000,
        seq -> Sampler.using(seq).sampleUniformDistribution(),
        0.5, 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_normal_distribution_sampling() {
    runTest(
        new TestContinuousDistribution<>(1000, seq -> Sampler.using(seq).sampleNormalDistribution(),
            new NormalDistribution(0, 1), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_exponential_distribution_sampling() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleExponentialDistribution(0.5),
        new ExponentialDistribution(0.5), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_exponential_distribution_sampling_secret_param() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleExponentialDistribution(FixedNumeric.using(seq).known(0.5)),
        new ExponentialDistribution(0.5), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_exponential_distribution_mean() {
    runTest(new TestContinuousDistributionMean<>(1000,
        seq -> Sampler.using(seq).sampleExponentialDistribution(0.5),
        0.5, 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_leaky_frequency_table() {
    runTest(new DescriptiveStatTests.TestLeakyFrequencyTable<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_frequency_table() {
    runTest(new DescriptiveStatTests.TestFrequencyTable<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_f_test() {
    runTest(new TestFTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_leaky_ranking() {
    runTest(new DescriptiveStatTests.TestLeakyRanks<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_ranks() {
    runTest(new DescriptiveStatTests.TestRanks<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_kruskall_wallis() {
    runTest(new TestKruskallWallis<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_kruskall_wallis_fixed_point() {
    runTest(new TestKruskallWallisFixedPoint<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_cox_gradient_continuous() {
    runTest(new TestCoxGradient<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_cox_hessian_continuous() {
    runTest(new TestCoxHessian<>(), TEST_PARAMETERS);
  }

  // Very slow
 // @Ignore
  @Test
  public void test_cox_regression_continuous() {
    runTest(new TestCoxRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_find_ties() {
    runTest(new DescriptiveStatTests.TestTiedGroups<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_logistic_regression_prediction() {
    runTest(new TestLogRegPrediction<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_logistic_regression_sgd_single_epoch() {
    runTest(new TestLogRegSGDSingleEpoch<>(), TEST_PARAMETERS);
  }

  @Test
  @Ignore
  public void test_logistic_regression() {
    runTest(new TestLogisticRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_gram_schmidt() {
    runTest(new TestGramSchmidt<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_qr_decomposition_rectangular() {
    Random random = new Random(1234);
    for (int i = 0; i < 10; i++) {
      runTest(new TestQRDcomposition<>(random.nextLong(), true), TEST_PARAMETERS);
    }
    for (int i = 0; i < 10; i++) {
      runTest(new TestQRDcomposition<>(random.nextLong(), false), TEST_PARAMETERS);
    }
  }

  @Test
  public void test_inverse_of_triangular_matrix() {
    runTest(new TestTriangularInverse<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_find_eigenvalues() {
    runTest(new TestEigenvalues<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_forward_substitution() {
    runTest(new TestForwardSubstitution<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_backward_substitution() {
    runTest(new TestBackSubstitution<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_linear_inverse_problem() {
    runTest(new TestLinearInverseProblem<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_linear_inverse_problem_underdetermined() {
    runTest(new TestLinearInverseProblemUnderdetermined<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_linear_inverse_problem_no_solution() {
    runTest(new TestLinearInverseProblemNoSolution<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_linear_inverse_problem_overdetermined() {
    runTest(new TestLinearInverseProblemOverdetermined<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_moore_penrose_pseudo_inverse() {
    Random random = new Random(1234);
    for (int i = 0; i < 10; i++) {
      runTest(new TestMoorePenrosePseudoInverse<>(random.nextLong(), true), TEST_PARAMETERS);
    }
    for (int i = 0; i < 10; i++) {
      runTest(new TestMoorePenrosePseudoInverse<>(random.nextLong(), false), TEST_PARAMETERS);
    }
  }

  @Test
  public void test_convolution() {
    runTest(new TestConvolution<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_product() {
    runTest(new TestProduct<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_histogram_discrete() {
    runTest(new TestHistogramDiscrete<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_histogram_discrete_debug() {
    runTest(new TestHistogramDiscrete2<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_noisy_histogram() {
    runTest(new TestNoisyHistogram<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_histogram_continuous() {
    runTest(new TestHistogramContinuous<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_histogram_continuous_two_dimensions() {
    runTest(new TestTwoDimHistogram<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_multidimensional_histogram() {
    runTest(new TestMultiDimHistogram<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_k_anonymity() {
    runTest(new TestKAnonymity<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_k_anonymity_open() {
    runTest(new TestKAnonymityOpen<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_forward_propagation() {
    runTest(new TestForwardPropagation<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_backward_propagation() {
    runTest(new TestBackwardPropagation<>(), TEST_PARAMETERS);
  }

  @Test
  @Ignore
  public void test_single_step_training() {
    runTest(new TestSingleStepTraining<>(), TEST_PARAMETERS);
  }

  @Test
  @Ignore
  public void test_fit() {
    runTest(new TestFit<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_prediction() {
    runTest(new TestPrediction<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_arg_max() {
    runTest(new TestArgMax<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_max() {
    runTest(new TestMax<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_filtered_mean() {
    runTest(new TestFilteredMean<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_filtered_variance() {
    runTest(new TestFilteredVariance<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_filtered_ttest() {
    runTest(new TestFilteredTTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_filtered_histogram() {
    runTest(new TestFilteredHistogram<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_filtered_k_anonymisation() {
    runTest(new TestFilteredKAnonymity<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_contingency_table() {
    runTest(new TestContingencyTable<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_indicator() {
    runTest(new TestIndicator<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_parallel_indicator() {
    runTest(new TestParallelIndicator<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_minima() {
    runTest(new TestMinimum<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_maxima() {
    runTest(new TestMaximum<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_wilcoxon_test() {
    runTest(new TestWilcoxonTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_fft() {
    runTest(new LATests.TestFFT<>(), TEST_PARAMETERS);
  }

}
