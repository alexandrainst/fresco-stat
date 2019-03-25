package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.tests.LinearRegression.LinearFunction;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.TTest;

public class StatTests {

  public static class TestTTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<BigDecimal> data = List.of(65, 78, 88, 55, 48, 95, 66, 57, 79, 81).stream()
            .map(BigDecimal::valueOf).collect(Collectors.toList());
        double expectedMean = 75.0;

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            Statistics stat = new DefaultStatistics(builder);
            List<DRes<SReal>> input =
                data.stream().map(x -> builder.realNumeric().known(x)).collect(Collectors.toList());
            DRes<SReal> t =
                stat.ttest(input, builder.realNumeric().known(BigDecimal.valueOf(expectedMean)));
            return builder.realNumeric().open(t);
          };

          BigDecimal t = runApplication(testApplication);
          double clearT = new TTest().t(expectedMean,
              data.stream().mapToDouble(BigDecimal::doubleValue).toArray());

          assertTrue(Math.abs(t.doubleValue() - clearT) < 0.01);
        }
      };
    }
  }

  public static class TestTwoSampleTTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<BigDecimal> data1 = List.of(42.1, 41.3, 42.4, 43.2, 41.8, 41.0, 41.8, 42.8, 42.3, 42.7)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());
        List<BigDecimal> data2 = List.of(42.7, 43.8, 42.5, 43.1, 44.0, 43.6, 43.3, 43.5, 41.7, 44.1)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            Statistics stat = new DefaultStatistics(builder);
            List<DRes<SReal>> input1 = data1.stream().map(x -> builder.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SReal>> input2 = data2.stream().map(x -> builder.realNumeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SReal> t = stat.ttest(input1, input2);
            return builder.realNumeric().open(t);
          };

          BigDecimal output = runApplication(testApplication);

          double clearT =
              new TTest().t(data1.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
                  data2.stream().mapToDouble(BigDecimal::doubleValue).toArray());

          assertTrue(Math.abs(clearT - output.doubleValue()) < 0.01);

        }
      };
    }
  }

  public static class TestChiSquareTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<BigDecimal> expected = List.of(58.0, 34.5, 7.0, 0.5).stream().map(BigDecimal::valueOf)
            .collect(Collectors.toList());
        List<BigInteger> observed =
            List.of(56, 36, 8, 0).stream().map(BigInteger::valueOf).collect(Collectors.toList());

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            Statistics stat = new DefaultStatistics(builder);

            List<DRes<SReal>> e = expected.stream().map(x -> builder.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SInt>> o = observed.stream().map(x -> builder.numeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SReal> x = stat.chiSquare(o, e);
            return builder.realNumeric().open(x);
          };

          BigDecimal output = runApplication(testApplication);

          double clearQ = new ChiSquareTest().chiSquare(
              expected.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
              observed.stream().mapToLong(BigInteger::longValue).toArray());

          assertTrue(Math.abs(output.doubleValue() - clearQ) < 0.01);

        }
      };
    }
  }

  public static class TestLinearRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<BigDecimal> x = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0).stream()
            .map(BigDecimal::valueOf).collect(Collectors.toList());
        List<BigDecimal> y = List.of(1.0, 2.0, 1.3, 3.75, 2.25).stream().map(BigDecimal::valueOf)
            .collect(Collectors.toList());

        double expectedA = 0.785;
        double expectedB = 0.425;

        @Override
        public void test() throws Exception {

          Application<Pair<BigDecimal, BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder -> {
                Statistics stat = new DefaultStatistics(builder);
                List<DRes<SReal>> xSecret = x.stream().map(x -> builder.realNumeric().input(x, 1))
                    .collect(Collectors.toList());
                List<DRes<SReal>> ySecret = y.stream().map(y -> builder.realNumeric().input(y, 2))
                    .collect(Collectors.toList());
                DRes<LinearFunction> f = stat.linearRegression(xSecret, ySecret);
                return builder.par(par -> {
                  Pair<DRes<BigDecimal>, DRes<BigDecimal>> result =
                      new Pair<>(par.realNumeric().open(f.out().getA()),
                          par.realNumeric().open(f.out().getB()));
                  return () -> new Pair<>(result.getFirst().out(), result.getSecond().out());
                });
              };

          Pair<BigDecimal, BigDecimal> output = runApplication(testApplication);
          assertTrue(output.getFirst().subtract(BigDecimal.valueOf(expectedA)).abs()
              .compareTo(BigDecimal.valueOf(0.001)) < 0);
          assertTrue(output.getSecond().subtract(BigDecimal.valueOf(expectedB)).abs()
              .compareTo(BigDecimal.valueOf(0.001)) < 0);
        }
      };
    }
  }

}
