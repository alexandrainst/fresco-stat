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
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.inference.TTest;
import org.junit.Assert;

public class FilteredStatTests {

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

  public static class TestFilteredMean<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final Random random = new Random(1234);
        final int n = 100;
        final List<Double> x = IntStream.range(0, n)
            .mapToDouble(i -> random.nextDouble() * 10.0 - 5.0).boxed().collect(
                Collectors.toList());
        final List<Integer> filter = IntStream.generate(() -> random.nextInt(2)).limit(n).boxed()
            .collect(
                Collectors.toList());

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            FixedNumeric numeric = FixedNumeric.using(builder);
            List<DRes<SFixed>> xSecret =
                x.stream().map(x -> numeric.input(x, 1)).collect(Collectors.toList());

            List<DRes<SInt>> filterSecret = filter.stream().map(builder.numeric()::known).collect(
                Collectors.toList());

            DRes<SFixed> r = FilteredStatistics.using(builder).sampleMean(xSecret, filterSecret);
            return numeric.open(r);
          };

          List<Double> xActual = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            if (filter.get(i) == 1) {
              xActual.add(x.get(i));
            }
          }
          double expected = new Mean().evaluate(xActual.stream().mapToDouble(i -> i).toArray());

          BigDecimal output = runApplication(testApplication);
          assertTrue(Math.abs(expected - output.doubleValue()) < 0.01);
        }
      };
    }
  }

  public static class TestFilteredVariance<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final Random random = new Random(1234);
        final int n = 100;
        final List<Double> x = IntStream.range(0, n)
            .mapToDouble(i -> random.nextDouble() * 10.0 - 5.0).boxed().collect(
                Collectors.toList());

        final List<Integer> filter = IntStream.generate(() -> random.nextInt(2)).limit(n).boxed()
            .collect(
                Collectors.toList());

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder -> {
            FixedNumeric numeric = FixedNumeric.using(builder);
            List<DRes<SFixed>> xSecret =
                x.stream().map(x -> numeric.input(x, 1)).collect(Collectors.toList());

            List<DRes<SInt>> filterSecret = filter.stream().map(builder.numeric()::known).collect(
                Collectors.toList());

            DRes<SFixed> r = FilteredStatistics.using(builder)
                .sampleVariance(xSecret, filterSecret);
            return numeric.open(r);
          };

          List<Double> xActual = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            if (filter.get(i) == 1) {
              xActual.add(x.get(i));
            }
          }
          double expected = new Variance().evaluate(xActual.stream().mapToDouble(i -> i).toArray());

          BigDecimal output = runApplication(testApplication);
          assertTrue(Math.abs(expected - output.doubleValue()) < 0.01);
        }
      };
    }
  }

  public static class TestFilteredHistogram<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        // Generate random data and filter
        final int n = 100;
        final Random random = new Random(1234);
        final List<Integer> x = IntStream.range(0, n).map(i -> random.nextInt(50)).boxed().collect(
            Collectors.toList());
        final List<Integer> filter = IntStream.range(0, n).map(i -> random.nextInt(2)).boxed()
            .collect(
                Collectors.toList());
        final List<Integer> buckets = Arrays.asList(10, 20, 30, 40);

        @Override
        public void test() {

          Application<List<BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                List<DRes<SInt>> xSecret =
                    x.stream().map(x -> seq.numeric().input(x, 1)).collect(Collectors.toList());
                List<DRes<SInt>> bSecret =
                    buckets.stream().map(b -> seq.numeric().input(b, 2))
                        .collect(Collectors.toList());
                List<DRes<SInt>> filterSecret =
                    filter.stream().map(b -> seq.numeric().input(b, 1))
                        .collect(Collectors.toList());
                return FilteredStatistics.using(seq).histogram(bSecret, xSecret, filterSecret);
              }).seq((seq, h) -> {
                List<DRes<BigInteger>> out =
                    h.stream().map(seq.numeric()::open).collect(Collectors.toList());
                return () -> out.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigInteger> output = runApplication(testApplication);

          List<Integer> xActual = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            if (filter.get(i) == 1) {
              xActual.add(x.get(i));
            }
          }

          // Sanity test -- are there as many points in histogram as in the filtered dataset?
          Assert.assertEquals(xActual.size(), output.stream().mapToInt(BigInteger::intValue).sum());

          // Test histogram output
          Assert.assertEquals(xActual.stream().filter(x -> x <= buckets.get(0)).count(),
              output.get(0).intValue());
          for (int i = 1; i < buckets.size(); i++) {
            int finalI = i;
            Assert.assertEquals(xActual.stream()
                    .filter(x -> x > buckets.get(finalI - 1) && x <= buckets.get(finalI)).count(),
                output.get(i).intValue());
          }
          Assert.assertEquals(
              xActual.stream().filter(x -> x > buckets.get(buckets.size() - 1)).count(),
              output.get(buckets.size()).intValue());
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

  public static class TestFilteredKAnonymity<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {
        final Random random = new Random();
        final int n = 100;
        final int k = 3;
        final List<Integer> x = IntStream.generate(() -> random.nextInt(10)).limit(n).boxed()
            .collect(Collectors.toList());
        final List<Integer> y = IntStream.generate(() -> random.nextInt(10)).limit(n).boxed()
            .collect(Collectors.toList());
        final List<Integer> z = IntStream.generate(() -> random.nextInt(10)).limit(n).boxed()
            .collect(Collectors.toList());
        final List<List<Integer>> D = List.of(x, y, z);

        // For easy test, we let the i'th rows sensitive attribute be equal to i+1
        final List<Integer> s = IntStream.range(0, n).map(i -> i + 1).boxed()
            .collect(Collectors.toList());
        final List<Integer> bucketsX = java.util.Collections.singletonList(2);
        final List<Integer> bucketsY = java.util.Collections.singletonList(2);
        final List<Integer> bucketsZ = java.util.Collections.singletonList(3);
        final List<List<Integer>> B = List.of(bucketsX, bucketsY, bucketsZ);
        final List<Integer> filter = IntStream.generate(() -> random.nextInt(2)).limit(n).boxed()
            .collect(Collectors.toList());

        @Override
        public void test() {

          Application<MultiDimensionalArray<List<BigInteger>>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                    List<List<DRes<SInt>>> buckets = B.stream().map(row -> row
                        .stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList())).collect(Collectors.toList());
                    Matrix<DRes<SInt>> data = MatrixUtils
                        .buildMatrix(n, 3, (i, j) -> seq.numeric().known(D.get(j).get(i)));
                    List<DRes<SInt>> sensitive = s.stream().map(x -> seq.numeric().input(x, 1))
                        .collect(Collectors.toList());
                    List<DRes<SInt>> filterSecret = filter.stream().map(fi -> seq.numeric().known(fi))
                        .collect(
                            Collectors.toList());
                    return FilteredStatistics.using(seq)
                        .kAnonymizeAndOpen(data, sensitive, buckets, k, filterSecret);
                  });

          MultiDimensionalArray<List<BigInteger>> output = runApplication(testApplication);

          output.forEachWithIndices((entries, index) -> {
            Assert.assertTrue(entries.size() == 0 || entries.size() >= k);

            for (BigInteger entry : entries) {
              int actualIndex = entry.intValue() - 1;
              Assert.assertEquals(1, filter.get(actualIndex).intValue());

              for (int j = 0; j < 3; j++) {
                if (index.get(j) > 0) {
                  Assert.assertTrue(D.get(j).get(actualIndex) > B.get(j).get(0));
                } else {
                  Assert.assertTrue(D.get(j).get(actualIndex) <= B.get(j).get(0));
                }
              }
            }
          });
        }
      };
    }
  }

  public static class TestFilteredTTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final Random random = new Random(1234);
        final int n = 100;
        final List<Double> x = IntStream.range(0, n)
            .mapToDouble(i -> random.nextDouble() * 10.0 - 5.0).boxed().collect(
                Collectors.toList());
        final List<Integer> filter = IntStream.generate(() -> random.nextInt(2)).limit(n).boxed()
            .collect(
                Collectors.toList());

        @Override
        public void test() {

          Application<Pair<BigDecimal, BigInteger>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {
                FixedNumeric numeric = FixedNumeric.using(seq);
                List<DRes<SFixed>> input =
                    x.stream().map(numeric::known).collect(Collectors.toList());
                List<DRes<SInt>> filterSecret = filter.stream().map(seq.numeric()::known).collect(
                    Collectors.toList());
                return FilteredStatistics.using(seq)
                    .ttest(input, numeric.known(0.0), filterSecret);
              }).seq((seq, t) ->
                  Pair.lazy(FixedNumeric.using(seq).open(t.getResult()),
                      seq.numeric().open(t.getN()))
              ).seq((seq, t) ->
                  Pair.lazy(t.getFirst().out(), t.getSecond().out()));

          Pair<BigDecimal, BigInteger> t = runApplication(testApplication);

          List<Double> xActual = new ArrayList<>();
          for (int i = 0; i < n; i++) {
            if (filter.get(i) == 1) {
              xActual.add(x.get(i));
            }
          }
          double clearT = new TTest().t(0.0,
              xActual.stream().mapToDouble(x -> x).toArray());
          assertEquals(t.getFirst().doubleValue(), clearT, 0.01);
          assertEquals(t.getSecond().intValue(), xActual.size());
        }
      };
    }
  }

}
