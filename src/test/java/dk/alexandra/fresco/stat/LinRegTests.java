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
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
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

        final List<BigDecimal> x1 = Stream
            .of(1.47, 1.50, 1.52, 1.55, 1.57, 1.60, 1.63, 1.65, 1.68, 1.70, 1.73, 1.75, 1.78, 1.8,
                1.83).map(BigDecimal::valueOf)
            .collect(Collectors.toList());
        final List<BigDecimal> x2 = x1.stream().map(x -> x.multiply(x))
            .collect(Collectors.toList());
        final List<ArrayList<BigDecimal>> X = IntStream.range(0, x1.size())
            .mapToObj(i -> List.of(BigDecimal.ONE, x1.get(i), x2.get(i))).map(
                ArrayList::new).collect(Collectors.toList());

        final List<Double> y = Stream
            .of(52.21, 53.12, 54.48, 55.84, 57.20, 58.57, 59.93, 61.29, 63.11, 64.47, 66.28, 68.10,
                69.92, 72.19, 74.46)
            .map(x -> x / 100.0).collect(
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
                toOutput.add(result.getDurbinWatsonTestStatistics());
                toOutput.add(result.getBreuschPaganTestStatistics());
                return FixedLinearAlgebra.using(seq).openArrayList(DRes.of(toOutput));
              }).seq((seq, output) -> DRes
                  .of(output.stream().map(DRes::out).collect(Collectors.toList())));

          List<BigDecimal> output = runApplication(testApplication);

          System.out.println(output);

          // Compare with results from Apache Commons Math:

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

          Assert.assertEquals(output.get(8).doubleValue(), regression.calculateAdjustedRSquared(),
              0.001);

          // Values below are taken from an analysis in Python

          // F-test
          Assert.assertEquals(5471, output.get(9).doubleValue(), 100);

          // t-test
          Assert.assertEquals(7.899, output.get(10).doubleValue(), 0.1);
          Assert.assertEquals(-7.218, output.get(11).doubleValue(), 0.1);
          Assert.assertEquals(10.312, output.get(12).doubleValue(), 0.1);

          // Durbin-Watson
          Assert
              .assertEquals(2.101, output.get(13).doubleValue(), 0.1);

          // Test that the Breusch-Pagan test statistics does not indicate hetereoskedacity
          //Assert.assertEquals(0.28922243456439567, output.get(14).doubleValue(), 0.1);
        }
      };
    }
  }

  public static class TestLinearRegressionLarge<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<BigDecimal> x1 = Stream
            .of(37,51,13,46,69,27,67,18,59,34,31,38,52,31,36,39,13,12,60,16,23,18,73,42,51,54,15,40,31,38,40,45,25,17,27,29,73,28,27,29,21,24,42,24,31,27,23,43,63,72,19,68,74,14,57,20,45,54,45,49,19,47,53,31,62,71,45,59,32,30,71,43,54,56,41,44,20,25,23,37,28,25,13,62,47,49)
            .mapToDouble(x -> x / 100.)
            .mapToObj(BigDecimal::valueOf)
            .collect(Collectors.toList());
        final List<BigDecimal> x2 = Stream.of(5.846525476501357,6.240275845170769,5.697965589334636,5.049214776063731,4.860587297852597,5.831090374796852,5.668569718900456,5.5338636845792895,5.506793880818843,5.598903324604841,5.883489505442683,5.884630724443972,6.203951518233663,5.555243795829815,5.893107229491114,6.0986359221697715,5.545411792018033,5.68639891904532,5.929269943514864,6.395044546180748,5.58116276110829,6.179498921308624,5.581765488467756,5.702314731381167,6.050322904114336,5.630566412324942,6.262254751889545,5.878799641450515,6.058796039268267,5.743515876874802,6.31757976304607,5.847305451408376,6.304540205922799,5.502441184024992,5.693799476602799,6.310390893798908,5.744604469176457,5.640132447579745,5.462771921513567,5.9692700615982135,5.677027737347381,6.152924165734059,5.721229388259921,5.648375467384462,5.84900772680898,4.9441393028078915,6.148190479552476,6.382289678476631,5.820320290904153,5.520780686557383,5.865305908812872,6.029651071826555,5.751270203723556,6.0719379312578585,6.0330862217988015,5.643749344540722,6.8976443352314085,5.985773383112691,6.091038352004074,6.484971057103506,6.351077670367247,6.060057338573594,5.451167200642247,5.056564225984656,6.291957952849524,6.050346474844204,6.074034827008621,5.8257345854069476,6.261434430773692,6.125492691330997,6.840664169427638,6.542010759068905,5.780403951974222,6.105194937435004,5.696590004074697,6.298397624151875,5.810661836283297,5.491042946640714,5.760477818332746,5.47692369709451,5.800182968942516,5.644492378630635,5.652945216619142,5.986426879340643,5.865022253055595,5.275099915371852)
            .mapToDouble(x -> x / 100.0)
            .mapToObj(BigDecimal::valueOf).collect(
            Collectors.toList());
        final List<ArrayList<BigDecimal>> X = IntStream.range(0, x1.size())
            .mapToObj(i -> List.of(BigDecimal.ONE, x1.get(i), x2.get(i))).map(
                ArrayList::new).collect(Collectors.toList());

        final List<BigDecimal> y = Stream
            .of(41,38,66,80,79,70,31,75,28,50,81,3,13,82,60,35,44,84,33,72,85,77,18,54,47,48,36,20,25,74,4,19,37,69,15,23,39,73,46,42,62,12,17,78,52,86,24,59,22,56,61,21,58,32,16,63,7,43,57,27,53,34,76,11,5,10,2,65,45,49,1,9,29,6,71,30,67,64,26,8,68,40,55,14,51,83)
            .mapToDouble(x -> x / 100.0)
            .mapToObj(BigDecimal::valueOf)
            .collect(
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
                toOutput.add(result.getDurbinWatsonTestStatistics());
                toOutput.add(result.getBreuschPaganTestStatistics());
                toOutput.add(result.getSkew());
                toOutput.add(result.getKurtosis());
                toOutput.add(result.getJarqueBeraTestStatistics());
                return FixedLinearAlgebra.using(seq).openArrayList(DRes.of(toOutput));
              }).seq((seq, output) -> DRes
                  .of(output.stream().map(DRes::out).collect(Collectors.toList())));

          List<BigDecimal> output = runApplication(testApplication);

          System.out.println(output);

          // Compare with results from Apache Commons Math:

          OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
          double[] yArray = y.stream().mapToDouble(BigDecimal::doubleValue).toArray();
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

          Assert.assertEquals(output.get(8).doubleValue(), regression.calculateAdjustedRSquared(),
              0.001);

          // Values below are taken from an analysis in Python

//          // F-test
//          Assert.assertEquals(5471, output.get(9).doubleValue(), 100);
//
//          // t-test
//          Assert.assertEquals(7.899, output.get(10).doubleValue(), 0.1);
//          Assert.assertEquals(-7.218, output.get(11).doubleValue(), 0.1);
//          Assert.assertEquals(10.312, output.get(12).doubleValue(), 0.1);
//
//          // Durbin-Watson
//          Assert
//              .assertEquals(2.101, output.get(13).doubleValue(), 0.1);
//
//          // Test that the Breusch-Pagan test statistics does not indicate hetereoskedacity
//          //Assert.assertEquals(0.28922243456439567, output.get(14).doubleValue(), 0.1);
        }
      };
    }
  }

}
