package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.FindMinima;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.Assert;

public class Optimisation {

  public static class TestMinimum<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Function<List<DRes<SFixed>>,
              Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric>> gradient = l -> (b -> {

                // Gradient for (x-1)^2 + (y-2)^2
            return DRes.of(new ArrayList<>(
                List.of(b.seq(seq -> {
                  FixedNumeric num = FixedNumeric.using(seq);
                  return num.sub(num.mult(2.0, l.get(0)), 2);
                  }), b.seq(seq -> {
                  FixedNumeric num = FixedNumeric.using(seq);
                  return num.sub(num.mult(2.0, l.get(1)), 4);
                }))));

          });
          Function<List<DRes<SFixed>>,
              Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric>> hessian = l -> (b -> {
            FixedNumeric num = FixedNumeric.using(b);
            return DRes.of(MatrixUtils.buildMatrix(2, 2, (i,j) -> {
              if (i == 0 && j == 0 || i == 1 && j == 1) {
                return num.known(2);
              } else {
                return num.known(0);
              }
            }));
          });


          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder ->
                  builder.seq(seq -> new FindMinima(List.of(
                      FixedNumeric.using(seq).known(0),
                      FixedNumeric.using(seq).known(0)),
                  gradient, hessian, 5, 1.0).buildComputation(seq)
              ).seq((seq, result) -> VectorUtils.openToAll(result, seq));

          List<BigDecimal> output = runApplication(testApplication);

          Assert.assertEquals(1.0, output.get(0).doubleValue(), 0.001);
          Assert.assertEquals(2.0, output.get(1).doubleValue(), 0.001);
        }
      };
    }
  }

  public static class TestMaximum<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Function<List<DRes<SFixed>>,
              Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric>> gradient = l -> (b -> {

            // Gradient for (-x+1)^2 + (-y+2)^2
            return DRes.of(new ArrayList<>(
                List.of(b.seq(seq -> {
                  FixedNumeric num = FixedNumeric.using(seq);
                  return num.add(2, num.mult(-2.0, l.get(0)));
                }), b.seq(seq -> {
                  FixedNumeric num = FixedNumeric.using(seq);
                  return num.add(4, num.mult(-2.0, l.get(1)));
                }))));

          });
          Function<List<DRes<SFixed>>,
              Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric>> hessian = l -> (b -> {
            FixedNumeric num = FixedNumeric.using(b);
            return DRes.of(MatrixUtils.buildMatrix(2, 2, (i,j) -> {
              if (i == 0 && j == 0 || i == 1 && j == 1) {
                return num.known(-2);
              } else {
                return num.known(0);
              }
            }));
          });


          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              builder ->
                  builder.seq(seq -> new FindMinima(List.of(
                      FixedNumeric.using(seq).known(0),
                      FixedNumeric.using(seq).known(0)),
                      gradient, hessian, 5, 1.0).buildComputation(seq)
                  ).seq((seq, result) -> VectorUtils.openToAll(result, seq));

          List<BigDecimal> output = runApplication(testApplication);

          Assert.assertEquals(1.0, output.get(0).doubleValue(), 0.001);
          Assert.assertEquals(2.0, output.get(1).doubleValue(), 0.001);
        }
      };
    }
  }

}
