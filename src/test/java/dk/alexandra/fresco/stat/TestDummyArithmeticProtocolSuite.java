package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.builder.numeric.field.BigIntegerFieldDefinition;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.ModulusFinder;
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
      .field(new BigIntegerFieldDefinition(ModulusFinder.findSuitableModulus(256)))
      .fixedPointPrecesion(16).evaluationStrategy(EvaluationStrategy.SEQUENTIAL).numParties(2)
      .performanceLogging(false);

  @Test
  public void test_T_Test() throws Exception {
    runTest(new StatTests.TestTTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_two_sample_T_Test() throws Exception {
    runTest(new StatTests.TestTwoSampleTTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test() throws Exception {
    runTest(new StatTests.TestChiSquareTest<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test_known() throws Exception {
    runTest(new StatTests.TestChiSquareTestKnown<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_chi_square_test_known_buckets() throws Exception {
    runTest(new StatTests.TestChiSquareTestWithKnownBuckets<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_linear_regression() throws Exception {
    runTest(new StatTests.TestLinearRegression<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_correlation() throws Exception {
    runTest(new StatTests.TestCorrelation<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_catagorical_distribution_sampling() throws Exception {
    double[] p = new double[] {0.1, 0.2, 0.1, 0.6};

    runTest(
        new TestDiscreteDistribution<>(100, () -> new SampleCatagoricalDistribution(p), p, 0.05),
        TEST_PARAMETERS);
  }

  @Test
  public void test_bernoulli_distribution_sampling() throws Exception {
    double[] p = new double[] {0.6, 0.4};

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
    runTest(new StatTests.TestHistogramInt<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_histogram_fixed() throws Exception {
    runTest(new StatTests.TestHistogramFixed<>(), TEST_PARAMETERS);
  }

  @Test
  public void test_two_dim_histogram() throws Exception {
    runTest(new StatTests.TestTwoDimHistogram<>(), TEST_PARAMETERS);
  }

}
