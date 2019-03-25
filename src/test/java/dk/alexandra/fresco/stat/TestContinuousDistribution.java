package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

/**
 * Generic test for continuous distribtion sampling.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 * @param <ResourcePoolT>
 */
public class TestContinuousDistribution<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

  private int n;
  private Supplier<Computation<SReal, ProtocolBuilderNumeric>> sampler;
  private double alpha;
  private RealDistribution expected;

  /**
   * Create a new test which samples n times and compares the result to an expected distribution
   * using a Kolmogorov Smirnov Test for goodness of fit with significance alpha.
   * 
   * @param n
   * @param sampler
   * @param expected
   * @param alpha
   */
  public TestContinuousDistribution(int n,
      Supplier<Computation<SReal, ProtocolBuilderNumeric>> sampler, RealDistribution expected,
      double alpha) {
    this.n = n;
    this.sampler = sampler;
    this.expected = expected;
    this.alpha = alpha;
  }

  @Override
  public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

    return new TestThread<>() {

      @Override
      public void test() throws Exception {


        Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = producer -> {

          List<DRes<SReal>> samples =
              Stream.generate(() -> sampler.get().buildComputation(producer)).limit(n)
                  .collect(Collectors.toList());

          List<DRes<BigDecimal>> opened =
              samples.stream().map(producer.realNumeric()::open).collect(Collectors.toList());

          return () -> opened.stream().map(DRes::out).collect(Collectors.toList());
        };

        List<BigDecimal> output = runApplication(testApplication);


        double[] outputAsDoubles = output.stream().mapToDouble(BigDecimal::doubleValue).toArray();

        double p =
            new KolmogorovSmirnovTest().kolmogorovSmirnovStatistic(expected, outputAsDoubles);

        assertTrue(p > alpha);

      }
    };

  }

}

