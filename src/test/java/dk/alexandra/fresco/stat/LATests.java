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
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.GramSchmidt;
import dk.alexandra.fresco.stat.linearalgebra.MatrixUtils;
import dk.alexandra.fresco.stat.linearalgebra.QRDecomposition;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LATests {


  public static class TestGramSchmidt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> a = List.of(1.0,2.0,3.0,0.0);
        final List<Double> b = List.of(1.0,2.0,0.0,0.0);
        final List<Double> c = List.of(1.0,0.0,0.0,1.0);

        @Override
        public void test() throws Exception {

          Application<List<List<BigDecimal>>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
            FixedNumeric numeric = FixedNumeric.using(seq);
            List<DRes<SFixed>> inputA =
                a.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> inputB =
                b.stream().map(numeric::known).collect(Collectors.toList());
            List<DRes<SFixed>> inputC =
                c.stream().map(numeric::known).collect(Collectors.toList());
            return new GramSchmidt(List.of(inputA, inputB, inputC)).buildComputation(seq);
          }).seq((seq, gs) -> {
                FixedNumeric numeric = FixedNumeric.using(seq);
            List<List<DRes<BigDecimal>>> out = gs.stream().map(v -> v.stream().map(numeric::open).collect(Collectors.toList())).collect(
                Collectors.toList());
            return () -> out.stream().map(v -> v.stream().map(DRes::out).collect(Collectors.toList())).collect(
                Collectors.toList());
          });

          List<List<BigDecimal>> out = runApplication(testApplication);

          for (int i = 0; i < out.size(); i++) {
            for (int j = i+1; j < out.size(); j++) {
              assertEquals(0.0, innerProduct(out.get(i), out.get(j)).doubleValue(), 0.01);
            }
          }
        }
      };
    }
  }

  public static class TestQR<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() throws Exception {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(12));
          rowOne.add(BigDecimal.valueOf(-51));
          rowOne.add(BigDecimal.valueOf(4));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(6));
          rowTwo.add(BigDecimal.valueOf(167));
          rowTwo.add(BigDecimal.valueOf(-68));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowThree.add(BigDecimal.valueOf(-4));
          rowThree.add(BigDecimal.valueOf(24));
          rowThree.add(BigDecimal.valueOf(-41));
          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> input = new Matrix<>(3, 3, mat);

          Application<Pair<Matrix<BigDecimal>, Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> FixedLinearAlgebra.using(seq).input(input, 1))
                  .seq((seq, a) -> {
                    return new QRDecomposition(
                        a).buildComputation(seq);
                      }).seq((seq, qr) -> {
                Matrix<DRes<BigDecimal>> qOut = MatrixUtils.map(qr.getFirst(), FixedNumeric.using(seq)::open);
                Matrix<DRes<BigDecimal>> rOut = MatrixUtils.map(qr.getSecond(), FixedNumeric.using(seq)::open);
                return Pair.lazy(qOut, rOut);
          }).seq((seq, qr) -> {
            Matrix<BigDecimal> qOut = MatrixUtils.map(qr.getFirst(), DRes::out);
                Matrix<BigDecimal> rOut = MatrixUtils.map(qr.getSecond(), DRes::out);
                return Pair.lazy(qOut, rOut);
              });

          Pair<Matrix<BigDecimal>, Matrix<BigDecimal>> out = runApplication(testApplication);

          // Assert q is ortonormal
          Matrix<BigDecimal> q = out.getFirst();
          for (int i = 0; i < q.getWidth(); i++) {
            assertEquals(1.0, innerProduct(q.getColumn(i), q.getColumn(i)).doubleValue(), 0.01);
            for (int j = i+1; j < q.getWidth(); j++) {
              assertEquals(0.0, innerProduct(q.getColumn(i), q.getColumn(j)).doubleValue(), 0.01);
            }
          }

          // Assert r is upper-triangular
          Matrix<BigDecimal> r = out.getSecond();
          for (int i = 0; i < r.getWidth(); i++) {
            for (int j = i+1; j < r.getHeight(); j++) {
              assertEquals(0.0, r.getColumn(i).get(j).doubleValue(), 0.01);
            }
          }
        }
      };
    }
  }

  private static BigDecimal innerProduct(List<BigDecimal> a, List<BigDecimal> b) {
    assert(a.size() == b.size());
    BigDecimal out = BigDecimal.ZERO;
    for (int i = 0; i < a.size(); i++) {
      out = out.add(a.get(i).multiply(b.get(i)));
    }
    return out;
  }

  private static BigDecimal norm(List<BigDecimal> a) {
    return innerProduct(a, a).sqrt(MathContext.DECIMAL64);
  }

}
