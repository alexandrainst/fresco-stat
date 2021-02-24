package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertTrue;
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
import dk.alexandra.fresco.stat.regression.linear.LinearRegression;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

          Application<Pair<BigDecimal, BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder -> {
                List<DRes<SFixed>> xSecret = x.stream().map(x -> FixedNumeric
                    .using(builder).input(x, 1))
                    .collect(Collectors.toList());
                List<DRes<SFixed>> ySecret = y.stream()
                    .map(y -> FixedNumeric.using(builder).input(y, 2))
                    .collect(Collectors.toList());
                DRes<Pair<DRes<SFixed>, DRes<SFixed>>> f = Statistics.using(builder)
                    .simpleLinearRegression(xSecret, ySecret);
                return builder.par(par -> {
                  Pair<DRes<BigDecimal>, DRes<BigDecimal>> result =
                      new Pair<>(FixedNumeric.using(par).open(f.out().getFirst()),
                          FixedNumeric.using(par).open(f.out().getSecond()));
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

  public static class TestLinearRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<BigDecimal> x1 = Stream.of(18, 24, 12, 30, 30, 22).map(BigDecimal::valueOf)
            .collect(Collectors.toList());
        final List<BigDecimal> x2 = Stream.of(52, 40, 40, 48, 32, 16).map(BigDecimal::valueOf)
            .collect(Collectors.toList());
        final List<ArrayList<BigDecimal>> X = IntStream.range(0, x1.size())
            .mapToObj(i -> List.of(BigDecimal.ONE, x1.get(i), x2.get(i))).map(
                ArrayList::new).collect(Collectors.toList());

        final List<Double> y = Arrays.asList(.4, .2, -2.4, -8.4, -5.6, -5.2);

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
                DRes<ArrayList<DRes<SFixed>>> beta = statistics.linearRegression(
                    inputs.getFirst().stream().map(DRes::out).collect(
                        Collectors.toList()), inputs.getSecond());

                return FixedLinearAlgebra.using(seq).openArrayList(beta);
              }).seq((seq, beta) -> DRes
                  .of(beta.stream().map(DRes::out).collect(Collectors.toList())));

          List<BigDecimal> output = runApplication(testApplication);

          OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
          double[] yArray = y.stream().mapToDouble(Double::valueOf).toArray();
          double[][] xArray = new double[yArray.length][];
          for (int i = 0; i < yArray.length; i++) {
            xArray[i] = new double[]{x1.get(i).doubleValue(), x2.get(i).doubleValue()};
          }
          regression.newSampleData(yArray, xArray);

          double[] betaExpected = regression.estimateRegressionParameters();
          Assert.assertArrayEquals(betaExpected,
              output.stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.01);
        }
      };
    }
  }

}
