package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestHistogramContinuous;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestHistogramDiscrete;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestKAnonymity;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestKAnonymityOpen;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestMedian;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestMultiDimHistogram;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestNoisyHistogram;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestPercentiles;
import dk.alexandra.fresco.stat.DescriptiveStatTests.TestSqrt;
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
import dk.alexandra.fresco.stat.LinRegTests.TestNoisySimpleLinearRegression;
import dk.alexandra.fresco.stat.LinRegTests.TestSimpleLinearRegression;
import dk.alexandra.fresco.stat.LogRegTests.TestLogRegPrediction;
import dk.alexandra.fresco.stat.LogRegTests.TestLogRegSGDSingleEpoch;
import dk.alexandra.fresco.stat.LogRegTests.TestLogisticRegression;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxGradient;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxRegressionContinuous;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxRegressionDiscrete;
import dk.alexandra.fresco.stat.TestsTests.TestChiSquareTest;
import dk.alexandra.fresco.stat.TestsTests.TestChiSquareTestKnown;
import dk.alexandra.fresco.stat.TestsTests.TestFTest;
import dk.alexandra.fresco.stat.TestsTests.TestKruskallWallis;
import dk.alexandra.fresco.stat.TestsTests.TestKruskallWallisFixedPoint;
import dk.alexandra.fresco.stat.TestsTests.TestTTest;
import dk.alexandra.fresco.stat.TestsTests.TestTwoSampleTTest;
import dk.alexandra.fresco.stat.TestsTests.TestTwoSampleTTestDifferentSizes;
import dk.alexandra.fresco.stat.UtilTests.TestArgMax;
import dk.alexandra.fresco.stat.UtilTests.TestMax;
import dk.alexandra.fresco.stat.UtilTests.TestProduct;
import dk.alexandra.fresco.stat.mlp.NNTests.TestBackwardPropagation;
import dk.alexandra.fresco.stat.mlp.NNTests.TestFit;
import dk.alexandra.fresco.stat.mlp.NNTests.TestForwardPropagation;
import dk.alexandra.fresco.stat.mlp.NNTests.TestPrediction;
import dk.alexandra.fresco.stat.mlp.NNTests.TestSingleStepTraining;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import dk.alexandra.fresco.suite.crt.AbstractDummyCRTTest;
import dk.alexandra.fresco.suite.crt.comparison.CRTComparison;
import dk.alexandra.fresco.suite.crt.fixed.CRTAdvancedFixedNumeric;
import dk.alexandra.fresco.suite.crt.fixed.CRTFixedNumeric;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestCRTSuite extends AbstractDummyCRTTest {

  @Before
  public void setup() {
    FixedNumeric.load(CRTFixedNumeric::new);
    AdvancedFixedNumeric.load(CRTAdvancedFixedNumeric::new);
    Comparison.load(CRTComparison::new);
  }

  @Test
  public void test_mean() {
    runTest(new DescriptiveStatTests.TestMean<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_median() {
    runTest(new TestMedian<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_percentiles() {
    runTest(new TestPercentiles<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_variance() {
    runTest(new TestVariance<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_standard_deviation() {
    runTest(new TestStandardDeviation<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_T_Test() {
    runTest(new TestTTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_two_sample_T_Test() {
    runTest(new TestTwoSampleTTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_two_sample_T_Test_different_sizes() {
    runTest(new TestTwoSampleTTestDifferentSizes<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_chi_square_test() {
    runTest(new TestChiSquareTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_chi_square_test_known() {
    runTest(new TestChiSquareTestKnown<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_simple_linear_regression() {
    runTest(new TestSimpleLinearRegression<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_noisy_simple_linear_regression() {
    runTest(new TestNoisySimpleLinearRegression<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_linear_regression() {
    runTest(new TestLinearRegression<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_correlation() {
    runTest(new DescriptiveStatTests.TestCorrelation<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_categorical_distribution_sampling() {
    double[] p = new double[]{0.1, 0.2, 0.5, 0.2};

    runTest(
        new TestDiscreteDistribution<>(200,
            seq -> Sampler.using(seq).sampleCategoricalDistribution(p), p, 0.1),
        EvaluationStrategy.SEQUENTIAL, 2);
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
        EvaluationStrategy.SEQUENTIAL, 2);
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
        }, normalizedP, 0.1),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_bernoulli_distribution_sampling() {
    double[] p = new double[]{0.6, 0.4};

    runTest(
        new TestDiscreteDistribution<>(200,
            seq -> Sampler.using(seq).sampleBernoulliDistribution(0.6), p, 0.05),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_bernoulli_distribution_sampling_secret_param() {
    double[] p = new double[]{0.6, 0.4};

    runTest(
        new TestDiscreteDistribution<>(200,
            seq -> Sampler.using(seq)
                .sampleBernoulliDistribution(FixedNumeric.using(seq).known(0.6)),
            p, 0.05),
        EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_laplace_distribution_sampling() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleLaplaceDistribution(1.5),
        new LaplaceDistribution(0, 1.5), 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

//  @Test
  public void test_laplace_distribution_sampling_secret_param() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleLaplaceDistribution(FixedNumeric.using(seq).known(1.5)),
        new LaplaceDistribution(0, 1.5), 0.05), EvaluationStrategy.SEQUENTIAL, 2);
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
            new NormalDistribution(0, 1), 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_exponential_distribution_sampling() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleExponentialDistribution(0.5),
        new ExponentialDistribution(0.5), 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_exponential_distribution_sampling_secret_param() {
    runTest(new TestContinuousDistribution<>(1000,
        seq -> Sampler.using(seq).sampleExponentialDistribution(FixedNumeric.using(seq).known(0.5)),
        new ExponentialDistribution(0.5), 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_exponential_distribution_mean() {
    runTest(new TestContinuousDistributionMean<>(1000,
        seq -> Sampler.using(seq).sampleExponentialDistribution(0.5),
        0.5, 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_leaky_frequency_table() {
    runTest(new DescriptiveStatTests.TestLeakyFrequencyTable<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_frequency_table() {
    runTest(new DescriptiveStatTests.TestFrequencyTable<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_f_test() {
    runTest(new TestFTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_leaky_ranking() {
    runTest(new DescriptiveStatTests.TestLeakyRanks<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_ranks() {
    runTest(new DescriptiveStatTests.TestRanks<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_kruskall_wallis() {
    runTest(new TestKruskallWallis<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_kruskall_wallis_fixed_point() {
    runTest(new TestKruskallWallisFixedPoint<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_cox_gradient() {
    runTest(new TestCoxGradient<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_cox_regression_discrete() {
    runTest(new TestCoxRegressionDiscrete<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_cox_regression_continuous() {
    runTest(new TestCoxRegressionContinuous<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_find_ties() {
    runTest(new DescriptiveStatTests.TestTiedGroups<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_logistic_regression_prediction() {
    runTest(new TestLogRegPrediction<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_logistic_regression_sgd_single_epoch() {
    runTest(new TestLogRegSGDSingleEpoch<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  @Ignore
  public void test_logistic_regression() {
    runTest(new TestLogisticRegression<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_gram_schmidt() {
    runTest(new TestGramSchmidt<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_qr_decomposition_rectangular() {
    Random random = new Random(1234);
    for (int i = 0; i < 10; i++) {
      runTest(new TestQRDcomposition<>(random.nextLong(), true), EvaluationStrategy.SEQUENTIAL, 2);
    }
    for (int i = 0; i < 10; i++) {
      runTest(new TestQRDcomposition<>(random.nextLong(), false), EvaluationStrategy.SEQUENTIAL, 2);
    }
  }

  @Test
  public void test_inverse_of_triangular_matrix() {
    runTest(new TestTriangularInverse<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_find_eigenvalues() {
    runTest(new TestEigenvalues<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_forward_substitution() {
    runTest(new TestForwardSubstitution<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_backward_substitution() {
    runTest(new TestBackSubstitution<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_linear_inverse_problem() {
    runTest(new TestLinearInverseProblem<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_linear_inverse_problem_underdetermined() {
    runTest(new TestLinearInverseProblemUnderdetermined<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_linear_inverse_problem_no_solution() {
    runTest(new TestLinearInverseProblemNoSolution<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_linear_inverse_problem_overdetermined() {
    runTest(new TestLinearInverseProblemOverdetermined<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_moore_penrose_pseudo_inverse() {
    Random random = new Random(1234);
    for (int i = 0; i < 10; i++) {
      runTest(new TestMoorePenrosePseudoInverse<>(random.nextLong(), true), EvaluationStrategy.SEQUENTIAL, 2);
    }
    for (int i = 0; i < 10; i++) {
      runTest(new TestMoorePenrosePseudoInverse<>(random.nextLong(), false), EvaluationStrategy.SEQUENTIAL, 2);
    }
  }

  @Test
  public void test_convolution() {
    runTest(new TestConvolution<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_product() {
    runTest(new TestProduct<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_histogram_discrete() {
    runTest(new TestHistogramDiscrete<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_noisy_histogram() {
    runTest(new TestNoisyHistogram<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_histogram_continuous() {
    runTest(new TestHistogramContinuous<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_histogram_continuous_two_dimensions() {
    runTest(new TestTwoDimHistogram<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_multidimensional_histogram() {
    runTest(new TestMultiDimHistogram<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_k_anonymity() {
    runTest(new TestKAnonymity<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_k_anonymity_open() {
    runTest(new TestKAnonymityOpen<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_forward_propagation() {
    runTest(new TestForwardPropagation<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_backward_propagation() {
    runTest(new TestBackwardPropagation<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  @Ignore
  public void test_single_step_training() {
    runTest(new TestSingleStepTraining<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  @Ignore
  public void test_fit() {
    runTest(new TestFit<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_prediction() {
    runTest(new TestPrediction<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_arg_max() {
    runTest(new TestArgMax<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_max() {
    runTest(new TestMax<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_filtered_mean() {
    runTest(new TestFilteredMean<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_filtered_variance() {
    runTest(new TestFilteredVariance<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_filtered_ttest() {
    runTest(new TestFilteredTTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_filtered_histogram() {
    runTest(new TestFilteredHistogram<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_filtered_k_anonymisation() {
    runTest(new TestFilteredKAnonymity<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_sqrt() {
    runTest(new TestSqrt<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }

}
