package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.anonymisation.NoisyStats;
import dk.alexandra.fresco.stat.regression.linear.LinearRegression;
import dk.alexandra.fresco.stat.regression.linear.SimpleLinearRegression.SimpleLinearRegressionResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.Assert;
import org.junit.Test;

public class LinRegTests {

  @Test(expected = IllegalArgumentException.class)
  public void linearRegressionObservationsWithDifferentSizes() {
    ArrayList<DRes<SFixed>> obs1 = mock(ArrayList.class);
    when(obs1.size()).thenReturn(2);
    ArrayList<DRes<SFixed>> obs2 = mock(ArrayList.class);
    when(obs2.size()).thenReturn(3);
    new LinearRegression(List.of(obs1, obs2), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void linearRegressionObservationsAndDependantVarsDifferInSize() {
    ArrayList<DRes<SFixed>> obs1 = mock(ArrayList.class);
    when(obs1.size()).thenReturn(3);
    List<ArrayList<DRes<SFixed>>> obs = List.of(obs1);
    ArrayList<DRes<SFixed>> y = mock(ArrayList.class);
    when(y.size()).thenReturn(2);
    new LinearRegression(obs, y);
  }

  public static class TestSimpleLinearRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        final List<Double> y = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder -> {
                List<DRes<SFixed>> xSecret = x.stream().map(x -> FixedNumeric
                    .using(builder).input(x, 1))
                    .collect(Collectors.toList());
                List<DRes<SFixed>> ySecret = y.stream()
                    .map(y -> FixedNumeric.using(builder).input(y, 2))
                    .collect(Collectors.toList());

                DRes<SimpleLinearRegressionResult> f = Statistics.using(builder)
                    .simpleLinearRegression(xSecret, ySecret);
                return builder.par(par -> {
                  List<DRes<BigDecimal>> result =
                      List.of(FixedNumeric.using(par).open(f.out().getAlpha()),
                          FixedNumeric.using(par).open(f.out().getBeta()),
                          FixedNumeric.using(par).open(f.out().getErrorAlphaSquared()),
                          FixedNumeric.using(par).open(f.out().getErrorBetaSquared()),
                          FixedNumeric.using(par).open(f.out().getRSquared()));
                  return () -> result.stream().map(DRes::out).collect(Collectors.toList());
                });
              };

          SimpleRegression simpleRegression = new SimpleRegression();
          for (int i = 0; i < x.size(); i++) {
            simpleRegression.addData(x.get(i), y.get(i));
          }
          RegressionResults result = simpleRegression.regress();
          List<BigDecimal> output = runApplication(testApplication);

          double delta = 0.001;
          assertEquals(output.get(0).doubleValue(), result.getParameterEstimate(0), delta);
          assertEquals(output.get(1).doubleValue(), result.getParameterEstimate(1), delta);
          assertEquals(output.get(2).doubleValue(), Math.pow(result.getStdErrorOfEstimate(0), 2),
              delta);
          assertEquals(output.get(3).doubleValue(), Math.pow(result.getStdErrorOfEstimate(1), 2),
              delta);
          assertEquals(output.get(4).doubleValue(), result.getRSquared(), delta);
        }
      };
    }
  }


  public static class TestNoisySimpleLinearRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = IntStream.range(0, 100).mapToDouble(i -> i * 1.0 / 100.0).boxed()
            .collect(
                Collectors.toList());
        final Random random = new Random(1234);
        final List<Double> y = x.stream().map(xi -> 0.7 + 0.2 * xi + random.nextGaussian() / 10)
            .collect(
                Collectors.toList());

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder -> {
                List<DRes<SFixed>> xSecret = x.stream().map(x -> FixedNumeric
                    .using(builder).input(x, 1))
                    .collect(Collectors.toList());
                List<DRes<SFixed>> ySecret = y.stream()
                    .map(y -> FixedNumeric.using(builder).input(y, 2))
                    .collect(Collectors.toList());
                DRes<List<DRes<SFixed>>> f = new NoisyStats(xSecret, ySecret, 0.5)
                    .buildComputation(builder);
                return builder.par(par -> {
                  DRes<BigDecimal> a = FixedNumeric.using(par).open(f.out().get(0));
                  DRes<BigDecimal> b = FixedNumeric.using(par).open(f.out().get(1));
                  List<DRes<BigDecimal>> result = List.of(a, b);
                  return () -> result.stream().map(DRes::out).collect(Collectors.toList());
                });
              };

          SimpleRegression simpleRegression = new SimpleRegression();
          for (int i = 0; i < x.size(); i++) {
            simpleRegression.addData(x.get(i), y.get(i));
          }
          RegressionResults expected = simpleRegression.regress();
          List<BigDecimal> output = runApplication(testApplication);

          assertArrayEquals(expected.getParameterEstimates(),
              output.stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.1);

        }
      };
    }
  }

  public static class TestLinearRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<BigDecimal> x1 = Stream.of(1.47,	1.50,	1.52,	1.55,	1.57,	1.60,	1.63,	1.65,	1.68,	1.70,	1.73,	1.75,	1.78,	1.8, 1.83).map(BigDecimal::valueOf)
            .collect(Collectors.toList());
        final List<BigDecimal> x2 = x1.stream().map(x -> x.multiply(x)).collect(Collectors.toList());
        final List<ArrayList<BigDecimal>> X = IntStream.range(0, x1.size())
            .mapToObj(i -> List.of(BigDecimal.ONE, x1.get(i), x2.get(i))).map(
                ArrayList::new).collect(Collectors.toList());

        final List<Double> y = Stream
            .of(52.21, 53.12,	54.48,	55.84,	57.20,	58.57,	59.93,	61.29,	63.11,	64.47,	66.28,	68.10,	69.92,	72.19,	74.46)
            .map(x -> x/100.0).collect(
            Collectors.toList());

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder -> builder.seq(seq -> {
                ArrayList<DRes<ArrayList<DRes<SFixed>>>> x1secret = X.stream()
                    .map(x -> FixedLinearAlgebra
                        .using(seq).input(x, 1))
                    .collect(Collectors.toCollection(ArrayList::new));
                ArrayList<DRes<SFixed>> ySecret = y.stream()
                    .map(y -> FixedNumeric.using(seq).input(y, 1))
                    .collect(Collectors.toCollection(ArrayList::new));
                return Pair.lazy(x1secret, ySecret);
              }).seq((seq, inputs) -> {
                Statistics statistics = Statistics.using(seq);

                return statistics.linearRegression(
                    inputs.getFirst().stream().map(DRes::out).collect(
                        Collectors.toList()), inputs.getSecond());
              }).seq((seq, result) -> {
                ArrayList<DRes<SFixed>> toOutput = new ArrayList<>(result.getBeta());
                toOutput.add(result.getErrorVariance());
                toOutput.addAll(result.getStdErrors());
                toOutput.add(result.getRSquared());
                toOutput.add(result.getAdjustedRSquared());
                toOutput.add(result.getFTestStatistics());
                toOutput.addAll(result.getTTestStatistics());
                return FixedLinearAlgebra.using(seq).openArrayList(DRes.of(toOutput));
              }).seq((seq, output) -> DRes
                  .of(output.stream().map(DRes::out).collect(Collectors.toList())));

          List<BigDecimal> output = runApplication(testApplication);

          System.out.println(output);

          OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
          double[] yArray = y.stream().mapToDouble(Double::valueOf).toArray();
          double[][] xArray = new double[yArray.length][];
          for (int i = 0; i < yArray.length; i++) {
            xArray[i] = new double[]{x1.get(i).doubleValue(), x2.get(i).doubleValue()};
          }
          regression.newSampleData(yArray, xArray);

          assertArrayEquals(regression.estimateRegressionParameters(),
              output.subList(0, 3).stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.001);

          Assert
              .assertEquals(output.get(3).doubleValue(), regression.estimateErrorVariance(), 0.001);

          assertArrayEquals(regression.estimateRegressionParametersStandardErrors(),
              output.subList(4, 7).stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.001);

          Assert.assertEquals(output.get(7).doubleValue(), regression.calculateRSquared(), 0.001);

          Assert.assertEquals(output.get(8).doubleValue(), regression.calculateAdjustedRSquared(), 0.001);


        }
      };
    }
  }

}
