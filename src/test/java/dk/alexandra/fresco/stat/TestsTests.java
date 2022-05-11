package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.tests.KruskallWallisTest;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.junit.Assert;

public class TestsTests {


  public static class TestTTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<BigDecimal> data = List.of(65, 78, 88, 55, 48, 95, 66, 57, 79, 81).stream()
            .map(BigDecimal::valueOf).collect(Collectors.toList());
        final double expectedMean = 75.0;

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            FixedNumeric numeric = FixedNumeric.using(builder);
            List<DRes<SFixed>> input =
                data.stream().map(numeric::known).collect(Collectors.toList());
            DRes<SFixed> t = Statistics.using(builder)
                .ttest(input, numeric.known(BigDecimal.valueOf(expectedMean)));
            return numeric.open(t);
          };

          BigDecimal t = runApplication(testApplication);
          double clearT = new TTest().t(expectedMean,
              data.stream().mapToDouble(BigDecimal::doubleValue).toArray());

          assertEquals(t.doubleValue(), clearT, 0.01);
        }
      };
    }
  }

  public static class TestTwoSampleTTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<BigDecimal> data1 = List
            .of(42.1, 41.3, 42.4, 43.2, 41.8, 41.0, 41.8, 42.8, 42.3, 42.7)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());
        final List<BigDecimal> data2 = List
            .of(42.7, 43.8, 42.5, 43.1, 44.0, 43.6, 43.3, 43.5, 41.7, 44.1)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SFixed>> input1 = data1.stream()
                .map(x -> FixedNumeric.using(builder).input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SFixed>> input2 = data2.stream()
                .map(x -> FixedNumeric.using(builder).input(x, 2))
                .collect(Collectors.toList());
            DRes<SFixed> t = Statistics.using(builder).ttest(input1, input2);
            return FixedNumeric.using(builder).open(t);
          };

          BigDecimal output = runApplication(testApplication);

          double clearT =
              new TTest()
                  .homoscedasticT(data1.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
                      data2.stream().mapToDouble(BigDecimal::doubleValue).toArray());

          assertTrue(Math.abs(clearT - output.doubleValue()) < 0.01);

        }
      };
    }
  }

  public static class TestTwoSampleTTestDifferentSizes<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<BigDecimal> data1 = List
            .of(42.1, 41.3, 42.4, 43.2, 41.8, 41.0, 41.8, 42.8, 42.3)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());
        final List<BigDecimal> data2 = List
            .of(42.7, 43.8, 42.5, 43.1, 44.0, 43.6, 43.3, 43.5)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SFixed>> input1 = data1.stream()
                .map(x -> FixedNumeric.using(builder).input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SFixed>> input2 = data2.stream()
                .map(x -> FixedNumeric.using(builder).input(x, 2))
                .collect(Collectors.toList());
            DRes<SFixed> t = Statistics.using(builder).ttest(input1, input2);
            return FixedNumeric.using(builder).open(t);
          };

          BigDecimal output = runApplication(testApplication);

          double clearT =
              new TTest()
                  .homoscedasticT(data1.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
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

        final List<Double> expected = List.of(58.0, 34.5, 7.0, 0.5);
        final List<Integer> observed = List.of(56, 36, 8, 0);

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SFixed>> e = expected.stream()
                .map(x -> FixedNumeric.using(builder).input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SInt>> o = observed.stream().map(x -> builder.numeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SFixed> x = Statistics.using(builder).chiSquare(o, e);
            return FixedNumeric.using(builder).open(x);
          };

          BigDecimal output = runApplication(testApplication);

          double[] e = expected.stream().mapToDouble(i -> i).toArray();
          long[] o = observed.stream().mapToLong(i -> i).toArray();
          double clearQ = new ChiSquareTest().chiSquare(e, o);

          assertEquals(output.doubleValue(), clearQ, 0.01);

        }
      };
    }
  }

  public static class TestChiSquareTestKnown<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final double[] expected = new double[]{58.0, 34.5, 7.0, 0.5};
        final List<Integer> observed = List.of(56, 36, 8, 0);

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SInt>> o = observed.stream().map(x -> builder.numeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SFixed> x = Statistics.using(builder).chiSquare(o, expected);
            return FixedNumeric.using(builder).open(x);
          };

          BigDecimal output = runApplication(testApplication);

          long[] o = observed.stream().mapToLong(i -> i).toArray();
          double clearQ = new ChiSquareTest().chiSquare(expected, o);

          assertEquals(output.doubleValue(), clearQ, 0.01);

        }
      };
    }
  }

  public static class TestFTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {


        final List<List<Integer>> data = DescriptiveStatTests.ranksDataset();

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SFixed>> input1 = data.get(0).stream()
                .map(x -> FixedNumeric.using(builder).input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SFixed>> input2 = data.get(1).stream()
                .map(x -> FixedNumeric.using(builder).input(x, 2))
                .collect(Collectors.toList());
            List<DRes<SFixed>> input3 = data.get(2).stream()
                .map(x -> FixedNumeric.using(builder).input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SFixed>> input4 = data.get(3).stream()
                .map(x -> FixedNumeric.using(builder).input(x, 2))
                .collect(Collectors.toList());

            Statistics statistics = Statistics.using(builder);
            DRes<SFixed> f = statistics.ffest(List.of(input1, input2, input3, input4));
            return FixedNumeric.using(builder).open(f);
          };

          BigDecimal output = runApplication(testApplication);

          OneWayAnova oneWayAnova = new OneWayAnova();
          double f = oneWayAnova.anovaFValue(List.of(
              data.get(0).stream().mapToDouble(Double::valueOf).toArray(),
              data.get(1).stream().mapToDouble(Double::valueOf).toArray(),
              data.get(2).stream().mapToDouble(Double::valueOf).toArray(),
              data.get(3).stream().mapToDouble(Double::valueOf).toArray()));

          assertEquals(output.doubleValue(), f, 0.01);
        }
      };
    }
  }

  public static class TestKruskallWallis<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<List<Integer>> data = DescriptiveStatTests.ranksDataset();

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<List<DRes<SInt>>> input = data.stream().map(
                sample -> sample.stream().map(x -> builder.numeric().input(x, 1))
                    .collect(Collectors.toList())).collect(Collectors.toList());
            DRes<SFixed> h = new KruskallWallisTest(input).buildComputation(builder);
            return FixedNumeric.using(builder).open(h);
          };

          // Data and expected values from example 12.3 in Blæsild & Granfeldt: "Statistics with
          // applications in biology and geology".
          BigDecimal output = runApplication(testApplication);
          assertEquals(29.4203, output.doubleValue(), 0.01);
        }
      };
    }
  }

  public static class TestKruskallWallisFixedPoint<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<List<Integer>> data = DescriptiveStatTests.ranksDataset();

        @Override
        public void test() {
          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<List<DRes<SFixed>>> input = data.stream().map(
                    sample -> sample.stream().map(x -> FixedNumeric.using(seq).input(x, 1))
                        .collect(Collectors.toList())).collect(Collectors.toList());
                return DRes.of(input);
              }).seq((seq, input) -> {
                Statistics statistics = Statistics.using(seq);
                DRes<SFixed> h = statistics.kruskallWallisTest(input);
                return FixedNumeric.using(seq).open(h);
              });

          // Data and expected values from example 12.3 in Blæsild & Granfeldt: "Statistics with
          // applications in biology and geology".
          BigDecimal output = runApplication(testApplication);
          assertEquals(29.4203, output.doubleValue(), 0.01);
        }
      };
    }
  }

  public static class TestWilcoxonTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        // Example from https://en.wikipedia.org/wiki/Wilcoxon_signed-rank_test#Example
        final List<BigDecimal> data1 = List
            .of(125, 115, 130, 140, 140, 115, 140, 125, 140, 135)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());
        final List<BigDecimal> data2 = List
            .of(110, 122, 125, 120, 140, 124, 123, 137, 135, 145)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SFixed>> input1 = data1.stream()
                .map(x -> FixedNumeric.using(builder).input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SFixed>> input2 = data2.stream()
                .map(x -> FixedNumeric.using(builder).input(x, 2))
                .collect(Collectors.toList());

            DRes<SFixed> W = Statistics.using(builder).twoSampleWilcoxonTest(input1, input2);
            return FixedNumeric.using(builder).open(W);
          };

          BigDecimal output = runApplication(testApplication);

          Assert.assertEquals(9.0, output.doubleValue(), 0.0001);

        }
      };
    }
  }

}
