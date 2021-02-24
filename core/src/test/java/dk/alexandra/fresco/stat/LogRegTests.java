package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.regression.logistic.LogisticRegressionGD;
import dk.alexandra.fresco.stat.regression.logistic.LogisticRegressionPrediction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import org.junit.Assert;

public class LogRegTests {

  public static Pair<Matrix<DRes<SFixed>>, ArrayList<DRes<SFixed>>> logisticRegressionDataset(
      ProtocolBuilderNumeric builder) {

    // Data from https://en.wikipedia.org/wiki/Logistic_regression
    double[] hours = new double[]{0.50, 0.75, 1.00, 1.25, 1.50, 1.75, 1.75, 2.00, 2.25, 2.50, 2.75,
        3.00, 3.25, 3.50, 4.00, 4.25, 4.50, 4.75, 5.00, 5.50};
    double[] pass = new double[]{0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1};

    List<List<Double>> data = Arrays.stream(hours).boxed().map(List::of)
        .collect(Collectors.toList());
    List<Double> e = Arrays.stream(pass).boxed().collect(Collectors.toList());

    Matrix<DRes<SFixed>> secretData = new Matrix<>(data.size(), data.get(0).size(),
        i -> data.get(i).stream().map(BigDecimal::valueOf).map(FixedNumeric.using(builder)::known)
            .collect(Collectors.toCollection(ArrayList::new)));

    ArrayList<DRes<SFixed>> secretE =
        e.stream().map(BigDecimal::valueOf).map(FixedNumeric.using(builder)::known)
            .collect(Collectors.toCollection(ArrayList::new));

    return new Pair<>(secretData, secretE);
  }

  public static class TestLogRegPrediction<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          List<Double> row = List.of(4.0);
          List<Double> b = List.of(-4.0777, 1.5046);

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = seq -> {

            List<DRes<SFixed>> secretRow =
                row.stream().map(i -> FixedNumeric.using(seq).known(BigDecimal.valueOf(i)))
                    .collect(Collectors.toList());

            List<DRes<SFixed>> secretB =
                b.stream().map(i -> FixedNumeric.using(seq).known(BigDecimal.valueOf(i)))
                    .collect(Collectors.toList());

            DRes<SFixed> y =
                new LogisticRegressionPrediction(secretRow, secretB).buildComputation(seq);

            return FixedNumeric.using(seq).open(y);
          };
          double expected = 1.0 / (1.0 + Math.exp(-b.get(0) - b.get(1) * row.get(0)));
          BigDecimal output = runApplication(testApplication);
          Assert.assertEquals(expected, output.doubleValue(), 0.001);
        }
      };
    }
  }

  public static class TestLogRegSGDSingleEpoch<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {
                Pair<Matrix<DRes<SFixed>>, ArrayList<DRes<SFixed>>> data = logisticRegressionDataset(
                    seq);

                ArrayList<DRes<SFixed>> initB = new ArrayList<>(
                    List.of(FixedNumeric.using(seq).known(-3.0),
                        FixedNumeric.using(seq).known(2.0)));

                return new LogisticRegressionGD(data.getFirst(), data.getSecond(), 0.1, initB)
                    .buildComputation(seq);
              }).seq((seq, b) -> {

                List<DRes<BigDecimal>> openB =
                    b.stream().map(bi -> FixedNumeric.using(seq).open(bi))
                        .collect(Collectors.toList());

                return () -> openB.stream().map(DRes::out).collect(Collectors.toList());
              });

          double[] hours = new double[]{0.50, 0.75, 1.00, 1.25, 1.50, 1.75, 1.75, 2.00, 2.25, 2.50,
              2.75, 3.00, 3.25, 3.50, 4.00, 4.25, 4.50, 4.75, 5.00, 5.50};
          double[] pass = new double[]{0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1, 1, 1};
          double rate = 0.1;

          List<BigDecimal> output = runApplication(testApplication);

          ToDoubleBiFunction<double[], Double> predict = (b, x) -> 1 / (1 + Math
              .exp(-b[0] - b[1] * x));

          double[] beta = new double[]{-3, 2};
          double[] score = new double[2];
          for (int i = 0; i < hours.length; i++) {
            score[0] += (predict.applyAsDouble(beta, hours[i]) - pass[i]);
            score[1] += (predict.applyAsDouble(beta, hours[i]) - pass[i]) * hours[i];
          }

          Assert.assertEquals(beta[0] - rate * score[0], output.get(0).doubleValue(), 0.001);
          Assert.assertEquals(beta[1] - rate * score[1], output.get(1).doubleValue(), 0.001);
        }
      };
    }
  }

  public static class TestLogisticRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {

                Pair<Matrix<DRes<SFixed>>, ArrayList<DRes<SFixed>>> data = logisticRegressionDataset(
                    seq);

                double[] guess = new double[]{0, 0};

                return Statistics.using(seq)
                    .logisticRegression(data.getFirst(), data.getSecond(), guess,
                        i -> 5.0 / (i + 35.0), 50);
              }).seq((seq, b) -> {

                List<DRes<BigDecimal>> openB =
                    b.stream().map(bi -> FixedNumeric.using(seq).open(bi))
                        .collect(Collectors.toList());

                return () -> openB.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(testApplication);
          assertEquals(-4.0778, output.get(0).doubleValue(), 0.2);
          assertEquals(1.5047, output.get(1).doubleValue(), 0.2);
        }
      };
    }
  }
}
