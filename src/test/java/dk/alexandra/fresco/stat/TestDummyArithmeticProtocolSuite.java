package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.builder.numeric.field.MersennePrimeFieldDefinition;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.stat.LATests.TestBackwardSubstitution;
import dk.alexandra.fresco.stat.LATests.TestEigenvalues;
import dk.alexandra.fresco.stat.LATests.TestForwardsubstitution;
import dk.alexandra.fresco.stat.LATests.TestGramSchmidt;
import dk.alexandra.fresco.stat.LATests.TestLinearInverseProblem;
import dk.alexandra.fresco.stat.LATests.TestLinearInverseProblemOverdetermined;
import dk.alexandra.fresco.stat.LATests.TestLinearInverseProblemUnderdetermined;
import dk.alexandra.fresco.stat.LATests.TestQR;
import dk.alexandra.fresco.stat.LATests.TestQRRectangular;
import dk.alexandra.fresco.stat.LATests.TestTriangularInverse;
import dk.alexandra.fresco.stat.LinRegTests.TestLinearRegression;
import dk.alexandra.fresco.stat.LogRegTests.TestLogRegPrediction;
import dk.alexandra.fresco.stat.LogRegTests.TestLogRegSGDSingleEpoch;
import dk.alexandra.fresco.stat.LogRegTests.TestLogisticRegression;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxGradient;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxRegressionContinuous;
import dk.alexandra.fresco.stat.SurvivalAnalysisTests.TestCoxRegressionDiscrete;
import dk.alexandra.fresco.stat.TestsTests.TestChiSquareTest;
import dk.alexandra.fresco.stat.TestsTests.TestChiSquareTestKnown;
import dk.alexandra.fresco.stat.TestsTests.TestChiSquareTestWithKnownBuckets;
import dk.alexandra.fresco.stat.TestsTests.TestFTest;
import dk.alexandra.fresco.stat.TestsTests.TestKruskallWallis;
import dk.alexandra.fresco.stat.TestsTests.TestTTest;
import dk.alexandra.fresco.stat.TestsTests.TestTwoSampleTTest;
import dk.alexandra.fresco.stat.sampling.SampleBernoulliDistribution;
import dk.alexandra.fresco.stat.sampling.SampleCatagoricalDistribution;
import dk.alexandra.fresco.stat.sampling.SampleExponentialDistribution;
import dk.alexandra.fresco.stat.sampling.SampleGammaDistribution;
import dk.alexandra.fresco.stat.sampling.SampleLaplaceDistribution;
import dk.alexandra.fresco.stat.sampling.SampleNormalDistribution;
import dk.alexandra.fresco.stat.sampling.SampleUniformDistribution;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  private static final TestParameters TEST_PARAMETERS = new TestParameters().maxBitLength(128)
      .field(MersennePrimeFieldDefinition.find(256))
      .fixedPointPrecesion(16).evaluationStrategy(EvaluationStrategy.SEQUENTIAL).numParties(2)
      .performanceLogging(false);

  @Test
  public void test_T_Test() throws Exception {
    runTest(new TestTTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_two_sample_T_Test() throws Exception {
    runTest(new TestTwoSampleTTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test() throws Exception {
    runTest(new TestChiSquareTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test_known() throws Exception {
    runTest(new TestChiSquareTestKnown<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test_known_buckets() throws Exception {
    runTest(new TestChiSquareTestWithKnownBuckets<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_linear_regression() throws Exception {
    runTest(new TestLinearRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_correlation() throws Exception {
    runTest(new DescriptiveStatTests.TestCorrelation<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_catagorical_distribution_sampling() throws Exception {
    double[] p = new double[]{0.1, 0.2, 0.1, 0.6};

    runTest(
        new TestDiscreteDistribution<>(100, () -> new SampleCatagoricalDistribution(p), p, 0.05),
        TEST_PARAMETERS);
  }

  @Test
  public void test_bernoulli_distribution_sampling() throws Exception {
    double[] p = new double[]{0.6, 0.4};

    runTest(
        new TestDiscreteDistribution<>(100, () -> new SampleBernoulliDistribution(0.7), p, 0.05),
        TEST_PARAMETERS);
  }

  @Test
  public void test_laplace_distribution_sampling() throws Exception {
    runTest(new TestContinuousDistribution<>(100, () -> new SampleLaplaceDistribution(10),
        new LaplaceDistribution(0.0, 10), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_uniform_distribution_sampling() throws Exception {
    runTest(new TestContinuousDistribution<>(100, () -> new SampleUniformDistribution(),
        new UniformRealDistribution(0.0, 1.0), 0.05), EvaluationStrategy.SEQUENTIAL, 2);
  }

  @Test
  public void test_normal_distribution_sampling() throws Exception {
    runTest(new TestContinuousDistribution<>(100, () -> new SampleNormalDistribution(),
        new NormalDistribution(0.0, 1.0), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_gamma_distribution_sampling() throws Exception {
    runTest(new TestContinuousDistribution<>(100, () -> new SampleGammaDistribution(9, 0.5),
        new GammaDistribution(9.0, 0.5), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_exponential_distribution_sampling() throws Exception {
    runTest(new TestContinuousDistribution<>(100, () -> new SampleExponentialDistribution(0.5),
        new ExponentialDistribution(2.0), 0.05), TEST_PARAMETERS);
  }

  @Test
  public void test_histogram_int() throws Exception {
    runTest(new DescriptiveStatTests.TestHistogramInt<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_histogram_fixed() throws Exception {
    runTest(new DescriptiveStatTests.TestHistogramFixed<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_two_dim_histogram() throws Exception {
    runTest(new DescriptiveStatTests.TestTwoDimHistogram<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_leaky_frequencies() {
    runTest(new DescriptiveStatTests.TestLeakyFrequencyTable<>(), TEST_PARAMETERS);
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
  public void test_cox_gradient() {
    runTest(new TestCoxGradient<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_cox_regression_discrete() {
    runTest(new TestCoxRegressionDiscrete<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_cox_regression_continuous() {
    runTest(new TestCoxRegressionContinuous<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_find_ties() {
    runTest(new DescriptiveStatTests.TestTiedGroups<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_logistic_regression_prediction() throws Exception {
    runTest(new TestLogRegPrediction<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_logistic_regression_sgd_single_epoch() throws Exception {
    runTest(new TestLogRegSGDSingleEpoch<>(), TEST_PARAMETERS);
  }

  // Slow test
  @Test
  public void test_logistic_regression() throws Exception {
    runTest(new TestLogisticRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_gram_schmidt() {
    runTest(new TestGramSchmidt<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_qr_decomposition() {
    runTest(new TestQR<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_qr_decomposition_rectangular() {
    runTest(new TestQRRectangular<>(), TEST_PARAMETERS);
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
    runTest(new TestForwardsubstitution<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_backward_substitution() {
    runTest(new TestBackwardSubstitution<>(), TEST_PARAMETERS);
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
  public void test_linear_inverse_problem_overdetermined() {
    runTest(new TestLinearInverseProblemOverdetermined<>(), TEST_PARAMETERS);
  }
}
