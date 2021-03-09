package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.anonymisation.KAnonymity;
import dk.alexandra.fresco.stat.descriptive.LeakyBreakTies;
import dk.alexandra.fresco.stat.descriptive.MultiDimensionalHistogram;
import dk.alexandra.fresco.stat.descriptive.Ranks;
import dk.alexandra.fresco.stat.descriptive.sort.FindTiedGroups;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.RankingAlgorithm;
import org.junit.Assert;

public class DescriptiveStatTests {

  public static List<List<Integer>> ranksDataset() {

    // Data from Blæsild & Granfeldt
    List<Integer> data1 = List
        .of(200, 215, 225, 229, 230, 232, 241, 253, 256, 264, 268, 288, 288);
    List<Integer> data2 = List
        .of(163, 182, 188, 195, 202, 205, 212, 214, 215, 230, 235, 255, 272);
    List<Integer> data3 = List
        .of(268, 271, 273, 282, 285, 299, 309, 310, 314, 320, 337, 340, 345);
    List<Integer> data4 = List
        .of(201, 216, 241, 257, 259, 267, 269, 282, 283, 291, 291, 312, 326);
    return List.of(data1, data2, data3, data4);
  }

  public static class TestCorrelation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        final List<Double> y = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            FixedNumeric numeric = FixedNumeric.using(builder);
            List<DRes<SFixed>> xSecret =
                x.stream().map(x -> numeric.input(x, 1)).collect(Collectors.toList());
            List<DRes<SFixed>> ySecret =
                y.stream().map(y -> numeric.input(y, 2)).collect(Collectors.toList());
            DRes<SFixed> r = Statistics.using(builder).correlation(xSecret, ySecret);
            return numeric.open(r);
          };

          double[] xArray = x.stream().mapToDouble(i -> i).toArray();
          double[] yArray = y.stream().mapToDouble(i -> i).toArray();

          PearsonsCorrelation correlation = new PearsonsCorrelation();
          double expected = correlation.correlation(xArray, yArray);

          BigDecimal output = runApplication(testApplication);
          assertTrue(Math.abs(expected - output.doubleValue()) < 0.01);
        }
      };
    }
  }

  public static class TestMean<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            FixedNumeric numeric = FixedNumeric.using(builder);
            List<DRes<SFixed>> xSecret =
                x.stream().map(x -> numeric.input(x, 1)).collect(Collectors.toList());
            DRes<SFixed> r = Statistics.using(builder).sampleMean(xSecret);
            return numeric.open(r);
          };

          double[] xArray = x.stream().mapToDouble(i -> i).toArray();

          Mean mean = new Mean();
          double expected = mean.evaluate(xArray);

          BigDecimal output = runApplication(testApplication);
          assertTrue(Math.abs(expected - output.doubleValue()) < 0.01);
        }
      };
    }
  }

  public static class TestVariance<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            FixedNumeric numeric = FixedNumeric.using(builder);
            List<DRes<SFixed>> xSecret =
                x.stream().map(x -> numeric.input(x, 1)).collect(Collectors.toList());
            DRes<SFixed> r = Statistics.using(builder).sampleVariance(xSecret);
            return numeric.open(r);
          };

          double[] xArray = x.stream().mapToDouble(i -> i).toArray();

          Variance mean = new Variance();
          double expected = mean.evaluate(xArray);

          BigDecimal output = runApplication(testApplication);
          assertTrue(Math.abs(expected - output.doubleValue()) < 0.01);
        }
      };
    }
  }

  public static class TestStandardDeviation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            FixedNumeric numeric = FixedNumeric.using(builder);
            List<DRes<SFixed>> xSecret =
                x.stream().map(x -> numeric.input(x, 1)).collect(Collectors.toList());
            DRes<SFixed> r = Statistics.using(builder).sampleStandardDeviation(xSecret);
            return numeric.open(r);
          };

          double[] xArray = x.stream().mapToDouble(i -> i).toArray();

          StandardDeviation standardDeviation = new StandardDeviation();
          double expected = standardDeviation.evaluate(xArray);

          BigDecimal output = runApplication(testApplication);
          assertTrue(Math.abs(expected - output.doubleValue()) < 0.01);
        }
      };
    }
  }

  public static class TestLeakyFrequencyTable<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {
        final List<Integer> x = Arrays.asList(1, 3, 2, 1, 3, 1);

        @Override
        public void test() {
          Application<List<Pair<BigInteger, Integer>>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                List<DRes<SInt>> xSecret =
                    x.stream().map(x -> seq.numeric().input(x, 1)).collect(Collectors.toList());
                return Statistics.using(seq).leakyFrequencyTable(xSecret);
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
          for (Pair<BigInteger, Integer> outputs : output) {
            assertEquals(expected.get(outputs.getFirst().intValue()),
                outputs.getSecond());
          }
        }
      };
    }
  }

  public static class TestFrequencyTable<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {
        final List<Integer> x = Arrays.asList(1, 3, 2, 1, 3, 1, 0, 0, 4);

        @Override
        public void test() {
          Application<List<Pair<BigInteger, Integer>>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                List<DRes<SInt>> xSecret =
                    x.stream().map(x -> seq.numeric().input(x, 1)).collect(Collectors.toList());
                return Statistics.using(seq).frequencyTable(xSecret);
              });

          Map<Integer, Integer> expected = new HashMap<>();
          for (int xi : x) {
            expected.putIfAbsent(xi, 0);
            expected.computeIfPresent(xi, (k, v) -> v + 1);
          }

          List<Pair<BigInteger, Integer>> output = runApplication(testApplication);
          for (Pair<BigInteger, Integer> outputs : output) {
            assertEquals(expected.get(outputs.getFirst().intValue()),
                outputs.getSecond());
          }
        }
      };
    }
  }


  public static class TestLeakyRanks<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Integer> data = ranksDataset().stream().flatMap(List::stream)
            .collect(Collectors.toList());

        @Override
        public void test() {
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

        final List<List<Integer>> data = ranksDataset();

        @Override
        public void test() {

          Application<Pair<List<BigDecimal>, Double>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<List<DRes<SInt>>> input = data.stream().map(
                    sample -> sample.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList())).collect(Collectors.toList());
                return new Ranks(input)
                    .buildComputation(seq);
              }).seq((seq, ranks) -> {
                List<DRes<BigDecimal>> openList =
                    ranks.getFirst().stream().map(FixedNumeric.using(seq)::open)
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

  public static class TestTiedGroups<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Integer> data = List.of(2, 5, 3, 6, 1, 3, 7, 6, 3, 9, 8, 7, 5, 5);

        @Override
        public void test() {
          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> {
            List<DRes<SInt>> input = data.stream().map(x -> builder.numeric().input(x, 1))
                .collect(Collectors.toList());

            return new FindTiedGroups(input).buildComputation(builder);
          };

          List<BigInteger> output = runApplication(testApplication);

          assertNotEquals(output.get(1), output.get(7));
          assertNotEquals(output.get(1), output.get(11));
          assertEquals(output.get(1), output.get(12));
          assertEquals(output.get(1), output.get(13));

          assertNotEquals(output.get(2), output.get(4));
          assertEquals(output.get(2), output.get(5));
          assertEquals(output.get(2), output.get(8));
        }
      };
    }
  }

  public static class TestHistogramDiscrete<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Integer> x = Arrays.asList(1, 5, 7, 3, 9, 5, 34, 5, -1, -3);
        final List<Integer> buckets = Arrays.asList(0, 5, 10);
        final List<Integer> expected = Arrays.asList(2, 5, 2, 1);

        @Override
        public void test() {

          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<DRes<SInt>> xSecret =
                    x.stream().map(x -> seq.numeric().input(x, 1)).collect(Collectors.toList());
                List<DRes<SInt>> bSecret =
                    buckets.stream().map(b -> seq.numeric().input(b, 2))
                        .collect(Collectors.toList());
                return Statistics.using(seq).histogramDiscrete(bSecret, xSecret);
              }).seq((seq, h) -> {
                List<DRes<BigInteger>> out =
                    h.stream().map(seq.numeric()::open).collect(Collectors.toList());
                return () -> out.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigInteger> output = runApplication(testApplication);
          for (int i = 0; i < output.size(); i++) {
            assertEquals(expected.get(i).intValue(), output.get(i).intValue());
          }
        }
      };
    }
  }

  public static class TestHistogramContinuous<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = Arrays.asList(.1, .5, .7, .3, .9, .5, 3.4, .5, -.1, -.3);
        final List<Double> buckets = Arrays.asList(.0, .5, 1.0);
        final List<Integer> expected = Arrays.asList(2, 5, 2, 1);

        @Override
        public void test() {

          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<DRes<SFixed>> xSecret =
                    x.stream().map(x -> FixedNumeric.using(seq).input(x, 1))
                        .collect(Collectors.toList());
                List<DRes<SFixed>> bSecret = buckets.stream()
                    .map(b -> FixedNumeric.using(seq).input(b, 2))
                    .collect(Collectors.toList());
                return Statistics.using(seq).histogramContinuous(bSecret, xSecret);
              }).seq((seq, h) -> {
                List<DRes<BigInteger>> out =
                    h.stream().map(seq.numeric()::open).collect(Collectors.toList());
                return () -> out.stream().map(DRes::out).collect(Collectors.toList());
              });

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

        final List<Integer> x = Arrays.asList(1, 3, 5, 6, 7, 8);
        final List<Integer> y = Arrays.asList(2, 4, 5, 8, 9, 10);
        final List<Integer> bucketsX = Arrays.asList(1, 4, 9);
        final List<Integer> bucketsY = Arrays.asList(1, 4, 9);

        @Override
        public void test() {

          Application<Matrix<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets = new Pair<>(
                    bucketsX.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList()),
                    bucketsY.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList())
                );
                List<Pair<DRes<SInt>, DRes<SInt>>> data = IntStream.range(0, x.size()).mapToObj(
                    i -> new Pair<>(seq.numeric().input(x.get(i), 1),
                        seq.numeric().input(y.get(i), 1))).collect(Collectors.toList());

                return Statistics.using(seq)
                    .twoDimensionalHistogramDiscrete(buckets, data);
              }).seq((seq, histogram) -> {
                Matrix<DRes<BigInteger>> opened = MatrixUtils.map(histogram, seq.numeric()::open);
                return () -> MatrixUtils.map(opened, DRes::out);
              });

          Matrix<BigInteger> output = runApplication(testApplication);
          assertEquals(BigInteger.valueOf(0), output.getRow(0).get(0));
          assertEquals(BigInteger.valueOf(1), output.getRow(1).get(1));
          assertEquals(BigInteger.valueOf(3), output.getRow(2).get(2));
        }
      };
    }
  }

  public static class TestMultiDimHistogram<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Integer> x = Arrays.asList(0, 2, 5, 1, 3, 5, 6, 7, 8, 10);
        final List<Integer> y = Arrays.asList(9, 5, 4, 2, 4, 5, 8, 9, 10, 11);
        final List<Integer> z = Arrays.asList(7, 5, 4, 1, 7, 2, 3, 4, 2, 11);
        final List<Integer> bucketsX = Arrays.asList(1, 4, 9);
        final List<Integer> bucketsY = Arrays.asList(1, 4, 5);
        final List<Integer> bucketsZ = Arrays.asList(0, 3, 7, 9);

        @Override
        public void test() {

          Application<MultiDimensionalArray<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<List<DRes<SInt>>> buckets = List.of(
                    bucketsX.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList()),
                    bucketsY.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList()),
                    bucketsZ.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList())
                );
                Matrix<DRes<SInt>> data = MatrixUtils.buildMatrix(7, 3, (i, j) -> {
                  if (j == 0) {
                    return seq.numeric().input(x.get(i), 1);
                  } else if (j == 1) {
                    return seq.numeric().input(y.get(i), 2);
                  } else {
                    return seq.numeric().input(z.get(i), 1);
                  }
                });

                return new MultiDimensionalHistogram(buckets, data).buildComputation(seq);
              }).seq((seq, histogram) -> {
                MultiDimensionalArray<DRes<BigInteger>> opened = histogram.map(seq.numeric()::open);
                return () -> opened.map(DRes::out);
              });

          MultiDimensionalArray<BigInteger> output = runApplication(testApplication);

          System.out.println(output.getDimension());
          System.out.println(output);

          for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
              for (int k = 0; k < 5; k++) {

                long actual = output.get(i, j, k).longValue();

                int finalI = i;
                int finalJ = j;
                int finalK = k;
                long expected = IntStream.range(0, 7)
                    .filter(n -> finalI == 3 || x.get(n) <= bucketsX.get(finalI))
                    .filter(n -> finalI == 0 || x.get(n) > bucketsX.get(finalI - 1))
                    .filter(n -> finalJ == 3 || y.get(n) <= bucketsY.get(finalJ))
                    .filter(n -> finalJ == 0 || y.get(n) > bucketsY.get(finalJ - 1))
                    .filter(n -> finalK == 4 || z.get(n) <= bucketsZ.get(finalK))
                    .filter(n -> finalK == 0 || z.get(n) > bucketsZ.get(finalK - 1)).count();

                Assert.assertEquals(expected, actual);

              }
            }
          }
        }
      };
    }
  }

  public static class TestKAnonymity<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Integer> x = Arrays.asList(0, 0, 1, 1, 3, 4, 5, 1, 3, 5, 6, 7, 8, 10);
        final List<Integer> y = Arrays.asList(0, 2, 1, 1, 1, 1, 0, 2, 4, 5, 8, 9, 10, 11);
        final List<Integer> z = Arrays.asList(0, 0, 0, 1, 5, 3, 4, 1, 7, 2, 3, 4, 2, 11);
        final List<Integer> s = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
        final List<Integer> bucketsX = java.util.Collections.singletonList(2);
        final List<Integer> bucketsY = java.util.Collections.singletonList(2);
        final List<Integer> bucketsZ = java.util.Collections.singletonList(3);

        @Override
        public void test() {

          Application<MultiDimensionalArray<List<BigInteger>>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<List<DRes<SInt>>> buckets = List.of(
                    bucketsX.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList()),
                    bucketsY.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList()),
                    bucketsZ.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList())

                );
                Matrix<DRes<SInt>> data = MatrixUtils.buildMatrix(x.size(), 3, (i, j) -> {
                  if (j == 0) {
                    return seq.numeric().input(x.get(i), 1);
                  } else if (j == 1) {
                    return seq.numeric().input(y.get(i), 2);
                  } else {
                    return seq.numeric().input(z.get(i), 1);
                  }
                });
                List<DRes<SInt>> sensitive = s.stream().map(x -> seq.numeric().input(x, 1))
                    .collect(Collectors.toList());

                return new KAnonymity(data, sensitive, buckets, 3).buildComputation(seq);
              }).seq((seq, histogram) -> {
                Collections collections = Collections.using(seq);
                MultiDimensionalArray<DRes<List<DRes<BigInteger>>>> opened = histogram
                    .map(l -> collections.openList(DRes.of(l)));
                return () -> opened
                    .map(a -> a.out().stream().map(DRes::out).collect(Collectors.toList()));
              });

          MultiDimensionalArray<List<BigInteger>> output = runApplication(testApplication);

          for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
              for (int k = 0; k < 2; k++) {
                for (int n = 0; n < s.size(); n++) {
                  if (output.get(i, j, k).get(n).intValue() > 0) {
                    Assert.assertEquals(s.get(n).intValue(), output.get(i, j, k).get(n).intValue());

                    boolean a = x.get(n) <= bucketsX.get(0);
                    boolean b = y.get(n) <= bucketsY.get(0);
                    boolean c = z.get(n) <= bucketsZ.get(0);

                    if (i == 0) {
                      Assert.assertTrue(a);
                    }

                    if (j == 0) {
                      Assert.assertTrue(b);
                    }

                    if (k == 0) {
                      Assert.assertTrue(c);
                    }
                  }
                }
              }
            }
          }
        }
      };
    }
  }

//  public static class TestKAnonymityLarge<ResourcePoolT extends ResourcePool>
//      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {
//
//    @Override
//    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
//      return new TestThread<>() {
//
//
//        @Override
//        public void test() {
//
//          Application<MultiDimensionalArray<List<BigInteger>>, ProtocolBuilderNumeric> testApplication = builder -> builder.par(seq -> {
//            List<DRes<List<DRes<SInt>>>> rows = finalX
//                .stream().map(array -> Collections.using(seq).closeList(
//                    Arrays.stream(array).mapToObj(BigInteger::valueOf).collect(Collectors.toList()),
//                    1))
//                .collect(Collectors.toList());
//            List<DRes<List<DRes<SInt>>>> buckets = B.stream().map(b -> Collections.using(seq).closeList(Arrays.stream(b).mapToObj(BigInteger::valueOf).collect(Collectors.toList()),
//                    1)).collect(Collectors.toList());
//            DRes<List<DRes<SInt>>> sensitive = Collections.using(seq).closeList(finalS.stream().map(BigInteger::valueOf).collect(
//                    Collectors.toList()), 1);
//            return () -> Triple.of(rows, buckets, sensitive);
//          }).seq((seq, inputs) -> {
//            ArrayList<ArrayList<DRes<SInt>>> matrixEntries = inputs.getLeft().stream().map(DRes::out).map(
//                ArrayList::new).collect(Collectors.toCollection(ArrayList::new));
//            List<List<DRes<SInt>>> buckets = inputs.getMiddle().stream().map(DRes::out).collect(Collectors.toList());
//            List<DRes<SInt>> sensitive = inputs.getRight().out();
//
//            return new KAnonymity(new Matrix<>(100, 3, matrixEntries), sensitive, buckets,3).buildComputation(seq);
//          }).seq((seq, histogram) -> {
//            Collections collections = Collections.using(seq);
//            MultiDimensionalArray<DRes<List<DRes<BigInteger>>>> opened = histogram.map(l -> collections.openList(DRes.of(l)));
//            return () -> opened.map(a -> a.out().stream().map(DRes::out).collect(Collectors.toList()));
//          });
//
//          MultiDimensionalArray<List<BigInteger>> output = runApplication(testApplication);
//          System.out.println(output);
//        }
//      };
//    }
//  }

}
