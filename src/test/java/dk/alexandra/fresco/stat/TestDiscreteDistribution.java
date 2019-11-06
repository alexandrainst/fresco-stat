package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

/**
 * Generic test for continuous distribtion sampling.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 * @param <ResourcePoolT>
 */
public class TestDiscreteDistribution<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

  private int n;
  private Supplier<Computation<SInt, ProtocolBuilderNumeric>> sampler;
  private double alpha;
  private double[] distribution;

  /**
   * Create a new test which samples n times and compares the result to an expected distribution
   * using a Chi-squared Test for goodness of fit with significance alpha.
   * 
   * @param n
   * @param sampler
   * @param expected
   * @param alpha
   */
  public TestDiscreteDistribution(int n,
      Supplier<Computation<SInt, ProtocolBuilderNumeric>> sampler, double[] distribution,
      double alpha) {
    this.n = n;
    this.sampler = sampler;
    this.distribution = distribution;
    this.alpha = alpha;
  }

  @Override
  public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

    return new TestThread<>() {

      @Override
      public void test() throws Exception {

        Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = producer -> {

          List<DRes<SInt>> samples = Stream.generate(() -> sampler.get().buildComputation(producer))
              .limit(n).collect(Collectors.toList());

          List<DRes<BigInteger>> opened =
              samples.stream().map(producer.numeric()::open).collect(Collectors.toList());

          return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
        };

        List<BigInteger> output = runApplication(testApplication);

        Map<Integer, Long> counts = output.stream().mapToInt(BigInteger::intValue).boxed()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        long[] observed = IntStream.range(0, distribution.length)
            .mapToLong(i -> counts.containsKey(i) ? counts.get(i) : 0).toArray();

        double[] expected = Arrays.stream(distribution).map(p -> p * n).toArray();

        double p = new ChiSquareTest().chiSquare(expected, observed);
        assertTrue(p > alpha);
      }
    };

  }

}
