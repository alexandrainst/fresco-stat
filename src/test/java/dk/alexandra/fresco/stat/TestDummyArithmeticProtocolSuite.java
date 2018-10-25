package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.suite.dummy.arithmetic.AbstractDummyArithmeticTest;
import org.junit.Test;

public class TestDummyArithmeticProtocolSuite extends AbstractDummyArithmeticTest {

  @Test
  public void test_T_Test() throws Exception {
    runTest(new StatTests.TestTTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }
  
  @Test
  public void test_two_sample_T_Test() throws Exception {
    runTest(new StatTests.TestTwoSampleTTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }
  
  @Test
  public void test_chi_square_test() throws Exception {
    runTest(new StatTests.TestChiSquareTest<>(), EvaluationStrategy.SEQUENTIAL, 2);
  }
}
