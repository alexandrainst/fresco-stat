package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.regression.LinearRegression.LinearFunction;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class LinRegTests {

  public static class TestLinearRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> x = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
        final List<Double> y = Arrays.asList(1.0, 2.0, 1.3, 3.75, 2.25);

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
}
