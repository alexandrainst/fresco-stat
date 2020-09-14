package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertArrayEquals;
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
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.LeakyBreakTies;
import dk.alexandra.fresco.stat.descriptive.LeakyFrequencyTable;
import dk.alexandra.fresco.stat.descriptive.Ranks;
import dk.alexandra.fresco.stat.tests.FTest;
import dk.alexandra.fresco.stat.tests.KruskallWallisTest;
import dk.alexandra.fresco.stat.tests.LinearRegression.LinearFunction;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;
import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;

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
            List<DRes<SReal>> input =
                data.stream().map(x -> builder.realNumeric().known(x)).collect(Collectors.toList());
            DRes<SReal> t = Statistics.using(builder)
                .ttest(input, builder.realNumeric().known(BigDecimal.valueOf(expectedMean)));
            return builder.realNumeric().open(t);
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

        List<BigDecimal> data1 = List.of(42.1, 41.3, 42.4, 43.2, 41.8, 41.0, 41.8, 42.8, 42.3, 42.7)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());
        List<BigDecimal> data2 = List.of(42.7, 43.8, 42.5, 43.1, 44.0, 43.6, 43.3, 43.5, 41.7, 44.1)
            .stream().map(BigDecimal::valueOf).collect(Collectors.toList());

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SReal>> input1 = data1.stream().map(x -> builder.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SReal>> input2 = data2.stream().map(x -> builder.realNumeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SReal> t = Statistics.using(builder).ttest(input1, input2);
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

        List<Double> expected = List.of(58.0, 34.5, 7.0, 0.5);
        List<Integer> observed = List.of(56, 36, 8, 0);

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SReal>> e = expected.stream().map(x -> builder.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SInt>> o = observed.stream().map(x -> builder.numeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SReal> x = Statistics.using(builder).chiSquare(o, e);
            return builder.realNumeric().open(x);
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

  public static class TestChiSquareTestWithKnownBuckets<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        double[] data = new double[]{.1, .5, .7, .3, .9, .5, 3.4, .5, -.1, -.3};
        double[] buckets = new double[]{.0, .5, 1.0};
        double[] expected = new double[]{2.1, 4.9, 2.1, 0.9};

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SReal>> secretData = Arrays.stream(data)
                .mapToObj(builder.realNumeric()::known)
                .collect(Collectors.toList());
            DRes<SReal> x = Statistics.using(builder).chiSquare(secretData, buckets, expected);
            return builder.realNumeric().open(x);
          };

          BigDecimal output = runApplication(testApplication);

          long[] o = new long[]{2, 5, 2, 1};
          double clearQ = new ChiSquareTest().chiSquare(expected, o);

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

        double[] expected = new double[]{58.0, 34.5, 7.0, 0.5};
        List<Integer> observed = List.of(56, 36, 8, 0);

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SInt>> o = observed.stream().map(x -> builder.numeric().input(x, 2))
                .collect(Collectors.toList());
            DRes<SReal> x = Statistics.using(builder).chiSquare(o, expected);
            return builder.realNumeric().open(x);
          };

          BigDecimal output = runApplication(testApplication);

          long[] o = observed.stream().mapToLong(i -> i).toArray();
          double clearQ = new ChiSquareTest().chiSquare(expected, o);

          assertEquals(output.doubleValue(), clearQ, 0.01);

        }
      };
    }
  }

  public static class TestLinearRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Double> x = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> y = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

        @Override
        public void test() throws Exception {

          Application<Pair<BigDecimal, BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder -> {
                List<DRes<SReal>> xSecret = x.stream().map(x -> builder.realNumeric().input(x, 1))
                    .collect(Collectors.toList());
                List<DRes<SReal>> ySecret = y.stream().map(y -> builder.realNumeric().input(y, 2))
                    .collect(Collectors.toList());
                DRes<LinearFunction> f = Statistics.using(builder)
                    .linearRegression(xSecret, ySecret);
                return builder.par(par -> {
                  Pair<DRes<BigDecimal>, DRes<BigDecimal>> result =
                      new Pair<>(par.realNumeric().open(f.out().getA()),
                          par.realNumeric().open(f.out().getB()));
                  return () -> new Pair<>(result.getFirst().out(), result.getSecond().out());
                });
              };

          SimpleRegression simpleRegression = new SimpleRegression();
          for (int i = 0; i < x.size(); i++) {
            simpleRegression.addData(x.get(i), y.get(i));
          }
          RegressionResults result = simpleRegression.regress();

          Pair<BigDecimal, BigDecimal> output = runApplication(testApplication);
          assertTrue(output.getFirst().subtract(BigDecimal.valueOf(result.getParameterEstimate(0)))
              .abs().compareTo(BigDecimal.valueOf(0.001)) < 0);
          assertTrue(output.getSecond().subtract(BigDecimal.valueOf(result.getParameterEstimate(1)))
              .abs().compareTo(BigDecimal.valueOf(0.001)) < 0);
        }
      };
    }
  }

  public static class TestCorrelation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Double> x = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        List<Double> y = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SReal>> xSecret =
                x.stream().map(x -> builder.realNumeric().input(x, 1)).collect(Collectors.toList());
            List<DRes<SReal>> ySecret =
                y.stream().map(y -> builder.realNumeric().input(y, 2)).collect(Collectors.toList());
            DRes<SReal> r = Statistics.using(builder).correlation(xSecret, ySecret);
            return builder.realNumeric().open(r);
          };

          double[] xArray = x.stream().mapToDouble(i -> i).toArray();
          double[] yArray = y.stream().mapToDouble(i -> i).toArray();

          PearsonsCorrelation correlation = new PearsonsCorrelation();
          double expected = correlation.correlation(xArray, yArray);

          BigDecimal output = runApplication(testApplication);
          System.out.println(output + " ~ " + expected);
          assertTrue(Math.abs(expected - output.doubleValue()) < 0.01);
        }
      };
    }
  }


  public static class TestHistogramInt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Integer> x = Arrays.asList(1, 5, 7, 3, 9, 5, 34, 5, -1, -3);
        List<Integer> buckets = Arrays.asList(0, 5, 10);
        List<Integer> expected = Arrays.asList(2, 5, 2, 1);

        @Override
        public void test() throws Exception {

          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> {
            return builder.seq(seq -> {
              List<DRes<SInt>> xSecret =
                  x.stream().map(x -> seq.numeric().input(x, 1)).collect(Collectors.toList());
              List<DRes<SInt>> bSecret =
                  buckets.stream().map(b -> seq.numeric().input(b, 2)).collect(Collectors.toList());
              DRes<List<DRes<SInt>>> h = Statistics.using(seq).histogramInt(bSecret, xSecret);
              return h;
            }).seq((seq, h) -> {
              List<DRes<BigInteger>> out =
                  h.stream().map(seq.numeric()::open).collect(Collectors.toList());
              return () -> out.stream().map(DRes::out).collect(Collectors.toList());
            });
          };

          List<BigInteger> output = runApplication(testApplication);
          for (int i = 0; i < output.size(); i++) {
            assertEquals(expected.get(i).intValue(), output.get(i).intValue());
          }
        }
      };
    }
  }

  public static class TestHistogramFixed<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Double> x = Arrays.asList(.1, .5, .7, .3, .9, .5, 3.4, .5, -.1, -.3);
        List<Double> buckets = Arrays.asList(.0, .5, 1.0);
        List<Integer> expected = Arrays.asList(2, 5, 2, 1);

        @Override
        public void test() throws Exception {

          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> {
            return builder.seq(seq -> {
              List<DRes<SReal>> xSecret =
                  x.stream().map(x -> seq.realNumeric().input(x, 1)).collect(Collectors.toList());
              List<DRes<SReal>> bSecret = buckets.stream().map(b -> seq.realNumeric().input(b, 2))
                  .collect(Collectors.toList());
              DRes<List<DRes<SInt>>> h = Statistics.using(seq).histogramReal(bSecret, xSecret);
              return h;
            }).seq((seq, h) -> {
              List<DRes<BigInteger>> out =
                  h.stream().map(seq.numeric()::open).collect(Collectors.toList());
              return () -> out.stream().map(DRes::out).collect(Collectors.toList());
            });
          };

          List<BigInteger> output = runApplication(testApplication);
          for (int i = 0; i < output.size(); i++) {
            assertEquals(expected.get(i).intValue(), output.get(i).intValue());
          }
        }
      };
    }
  }

  public static class TestTwoDimHistogram<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Integer> x = Arrays.asList(1, 3, 5, 6, 7, 8);
        List<Integer> y = Arrays.asList(2, 4, 5, 8, 9, 10);
        List<Integer> bucketsX = Arrays.asList(1, 4, 9);
        List<Integer> bucketsY = Arrays.asList(1, 4, 9);

        @Override
        public void test() throws Exception {

          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> {
            return builder.seq(seq -> {
              Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets = new Pair<>(
                  bucketsX.stream().map(x -> seq.numeric().input(x, 1))
                      .collect(Collectors.toList()),
                  bucketsY.stream().map(x -> seq.numeric().input(x, 1)).collect(Collectors.toList())
              );
              List<Pair<DRes<SInt>, DRes<SInt>>> data = IntStream.range(0, x.size()).mapToObj(
                  i -> new Pair<>(seq.numeric().input(x.get(i), 1),
                      seq.numeric().input(y.get(i), 1))).collect(Collectors.toList());
              x.stream().map(x -> seq.realNumeric().input(x, 1)).collect(Collectors.toList());

              DRes<Matrix<DRes<SInt>>> histogram = Statistics.using(seq)
                  .twoDimensionalHistogramInt(buckets, data);
              return histogram;
            }).seq((seq, histogram) -> {
              Matrix<DRes<BigInteger>> opened = MatrixUtils.map(histogram, seq.numeric()::open);
              return () -> MatrixUtils.map(opened, DRes::out);
            });
          };

          Matrix<BigInteger> output = runApplication(testApplication);
          assertEquals(BigInteger.valueOf(0), output.getRow(0).get(0));
          assertEquals(BigInteger.valueOf(1), output.getRow(1).get(1));
          assertEquals(BigInteger.valueOf(3), output.getRow(2).get(2));
        }
      };
    }
  }

  public static class TestLeakyFrequencyTable<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {
        List<Integer> x = Arrays.asList(1, 3, 2, 1, 3, 1);

        @Override
        public void test() {
          Application<List<Pair<BigInteger, Integer>>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                List<DRes<SInt>> xSecret =
                    x.stream().map(x -> seq.numeric().input(x, 1)).collect(Collectors.toList());
                DRes<List<Pair<DRes<SInt>, Integer>>> frequencyTable = new LeakyFrequencyTable(
                    xSecret).buildComputation(seq);
                return frequencyTable;
              }).seq((seq, ft) -> {
                List<Pair<DRes<BigInteger>, Integer>> out =
                    ft.stream()
                        .map(p -> new Pair<>(seq.numeric().open(p.getFirst()), p.getSecond()))
                        .collect(Collectors.toList());
                return () -> out.stream().map(p -> new Pair<>(p.getFirst().out(), p.getSecond()))
                    .collect(Collectors.toList());
              });

          Map<Integer, Integer> expected = new HashMap<>();
          for (int xi : x) {
            expected.putIfAbsent(xi, 0);
            expected.computeIfPresent(xi, (k, v) -> v + 1);
          }

          List<Pair<BigInteger, Integer>> output = runApplication(testApplication);
          for (int i = 0; i < output.size(); i++) {
            assertEquals(expected.get(output.get(i).getFirst().intValue()), output.get(i).getSecond());
          }
        }
      };
    }
  }


  public static class TestFTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Integer> data1 = List
            .of(200, 215, 225, 229, 230, 232, 241, 253, 256, 264, 268, 288, 288);
        List<Integer> data2 = List
            .of(163, 182, 188, 195, 202, 205, 212, 214, 215, 230, 235, 255, 272);
        List<Integer> data3 = List
            .of(268, 271, 273, 282, 285, 299, 309, 310, 314, 320, 337, 340, 345);
        List<Integer> data4 = List
            .of(201, 216, 241, 257, 259, 267, 269, 282, 283, 291, 291, 312, 326);

        @Override
        public void test() throws Exception {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SReal>> input1 = data1.stream().map(x -> builder.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SReal>> input2 = data2.stream().map(x -> builder.realNumeric().input(x, 2))
                .collect(Collectors.toList());
            List<DRes<SReal>> input3 = data3.stream().map(x -> builder.realNumeric().input(x, 1))
                .collect(Collectors.toList());
            List<DRes<SReal>> input4 = data4.stream().map(x -> builder.realNumeric().input(x, 2))
                .collect(Collectors.toList());

            DRes<SReal> f = new FTest(List.of(input1, input2, input3, input4))
                .buildComputation(builder);
            return builder.realNumeric().open(f);
          };

          BigDecimal output = runApplication(testApplication);

          OneWayAnova oneWayAnova = new OneWayAnova();
          double f = oneWayAnova.anovaFValue(List.of(
              data1.stream().mapToDouble(Double::valueOf).toArray(),
              data2.stream().mapToDouble(Double::valueOf).toArray(),
              data3.stream().mapToDouble(Double::valueOf).toArray(),
              data4.stream().mapToDouble(Double::valueOf).toArray()));

          assertEquals(output.doubleValue(), f, 0.01);
        }
      };
    }
  }

  public static class TestLeakyRanks<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Integer> data1 = List
            .of(200, 215, 225, 229, 230, 232, 241, 253, 256, 264, 268, 288, 288);
        List<Integer> data2 = List
            .of(163, 182, 188, 195, 202, 205, 212, 214, 215, 230, 235, 255, 272);
        List<Integer> data3 = List
            .of(268, 271, 273, 282, 285, 299, 309, 310, 314, 320, 337, 340, 345);
        List<Integer> data4 = List
            .of(201, 216, 241, 257, 259, 267, 269, 282, 283, 291, 291, 312, 326);
        List<Integer> data = new ArrayList<>();

        @Override
        public void test() throws Exception {
          data.addAll(data1);
          data.addAll(data2);
          data.addAll(data3);
          data.addAll(data4);
          data.sort(Integer::compareTo);

          Application<List<Double>, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SInt>> input = data.stream().map(x -> builder.numeric().input(x, 1))
                .collect(Collectors.toList());

            return new LeakyBreakTies(input).buildComputation(builder);
          };

          List<Double> output = runApplication(testApplication);

          RankingAlgorithm ranking = new NaturalRanking();

          double[] rank = ranking.rank(data.stream().mapToDouble(Double::valueOf).toArray());

          assertArrayEquals(rank, output.stream().mapToDouble(x -> x).toArray(), 0.01);
        }
      };
    }
  }

  public static class TestRanks<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Integer> data1 = List
            .of(200, 215, 225, 229, 230, 232, 241, 253, 256, 264, 268, 288, 288);
        List<Integer> data2 = List
            .of(163, 182, 188, 195, 202, 205, 212, 214, 215, 230, 235, 255, 272);
        List<Integer> data3 = List
            .of(268, 271, 273, 282, 285, 299, 309, 310, 314, 320, 337, 340, 345);
        List<Integer> data4 = List
            .of(201, 216, 241, 257, 259, 267, 269, 282, 283, 291, 291, 312, 326);
        List<List<Integer>> data = new ArrayList<>();

        @Override
        public void test() throws Exception {
          data.add(data1);
          data.add(data2);
          data.add(data3);
          data.add(data4);

          Application<Pair<List<BigDecimal>, Double>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<List<DRes<SInt>>> input = data.stream().map(
                    sample -> sample.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList())).collect(Collectors.toList());
                DRes<Pair<List<DRes<SReal>>, Double>> ranks = new Ranks(input, true)
                    .buildComputation(seq);
                return ranks;
              }).seq((seq, ranks) -> {
                List<DRes<BigDecimal>> openList =
                    ranks.getFirst().stream().map(seq.realNumeric()::open)
                        .collect(Collectors.toList());
                return () -> new Pair<>(
                    openList.stream().map(DRes::out).collect(Collectors.toList()),
                    ranks.getSecond());
              });

          Pair<List<BigDecimal>, Double> output = runApplication(testApplication);

          // Data and expected values from example 12.3 in Blæsild & Granfeldt: "Statistics with
          // applications in biology and geology".
          assertArrayEquals(new double[]{282, 147, 549, 400},
              output.getFirst().stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.01);
          assertEquals(1.000282292212767, output.getSecond(), 0.01);
        }
      };
    }
  }

  public static class TestKruskallWallis<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        List<Integer> data1 = List
            .of(200, 215, 225, 229, 230, 232, 241, 253, 256, 264, 268, 288, 288);
        List<Integer> data2 = List
            .of(163, 182, 188, 195, 202, 205, 212, 214, 215, 230, 235, 255, 272);
        List<Integer> data3 = List
            .of(268, 271, 273, 282, 285, 299, 309, 310, 314, 320, 337, 340, 345);
        List<Integer> data4 = List
            .of(201, 216, 241, 257, 259, 267, 269, 282, 283, 291, 291, 312, 326);
        List<List<Integer>> data = new ArrayList<>();

        @Override
        public void test() {
          data.add(data1);
          data.add(data2);
          data.add(data3);
          data.add(data4);

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            List<List<DRes<SInt>>> input = data.stream().map(
                sample -> sample.stream().map(x -> builder.numeric().input(x, 1))
                    .collect(Collectors.toList())).collect(Collectors.toList());
            DRes<SReal> h = new KruskallWallisTest(input, true).buildComputation(builder);
            DRes<BigDecimal> output = builder.realNumeric().open(h);
            return output;
          };

          // Data and expected values from example 12.3 in Blæsild & Granfeldt: "Statistics with
          // applications in biology and geology".
          BigDecimal output = runApplication(testApplication);
          assertEquals(29.4203, output.doubleValue(), 0.01);
        }
      };
    }
  }

}
