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
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.BackwardSubstitution;
import dk.alexandra.fresco.stat.linearalgebra.ForwardSubstitution;
import dk.alexandra.fresco.stat.linearalgebra.GramSchmidt;
import dk.alexandra.fresco.stat.linearalgebra.InvertTriangularMatrix;
import dk.alexandra.fresco.stat.linearalgebra.LinearInverseProblem;
import dk.alexandra.fresco.stat.linearalgebra.MatrixUtils;
import dk.alexandra.fresco.stat.linearalgebra.QRAlgorithm;
import dk.alexandra.fresco.stat.linearalgebra.QRDecomposition;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class LATests {


  private static BigDecimal innerProduct(List<BigDecimal> a, List<BigDecimal> b) {
    assert (a.size() == b.size());
    BigDecimal out = BigDecimal.ZERO;
    for (int i = 0; i < a.size(); i++) {
      out = out.add(a.get(i).multiply(b.get(i)));
    }
    return out;
  }

  private static List<BigDecimal> multiply(Matrix<BigDecimal> a, List<BigDecimal> b) {
    List<BigDecimal> result = new ArrayList<>();
    for (int i = 0; i < a.getHeight(); i++) {
      BigDecimal res = BigDecimal.ZERO;
      for (int j = 0; j < a.getWidth(); j++) {
        res = res.add(a.getRow(i).get(j).multiply(b.get(j)));
      }
      result.add(res);
    }
    return result;
  }

  public static class TestGramSchmidt<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> a = List.of(1.0, 2.0, 3.0, 0.0);
        final List<Double> b = List.of(1.0, 2.0, 0.0, 0.0);
        final List<Double> c = List.of(1.0, 0.0, 0.0, 1.0);

        @Override
        public void test() {

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
                List<List<DRes<BigDecimal>>> out = gs.stream()
                    .map(v -> v.stream().map(numeric::open).collect(Collectors.toList())).collect(
                        Collectors.toList());
                return () -> out.stream()
                    .map(v -> v.stream().map(DRes::out).collect(Collectors.toList())).collect(
                        Collectors.toList());
              });

          List<List<BigDecimal>> out = runApplication(testApplication);

          for (int i = 0; i < out.size(); i++) {
            for (int j = i + 1; j < out.size(); j++) {
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
        public void test() {

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
                  .seq((seq, a) -> new QRDecomposition(
                      a).buildComputation(seq)).seq((seq, qr) -> {
                Matrix<DRes<BigDecimal>> qOut = MatrixUtils
                    .map(qr.getFirst(), FixedNumeric.using(seq)::open);
                Matrix<DRes<BigDecimal>> rOut = MatrixUtils
                    .map(qr.getSecond(), FixedNumeric.using(seq)::open);
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
            for (int j = i + 1; j < q.getWidth(); j++) {
              assertEquals(0.0, innerProduct(q.getColumn(i), q.getColumn(j)).doubleValue(), 0.01);
            }
          }

          // Assert r is upper-triangular
          Matrix<BigDecimal> r = out.getSecond();
          for (int i = 0; i < r.getWidth(); i++) {
            for (int j = i + 1; j < r.getHeight(); j++) {
              assertEquals(0.0, r.getColumn(i).get(j).doubleValue(), 0.01);
            }
          }
        }
      };
    }
  }

  public static class TestTriangularInverse<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(3));
          rowOne.add(BigDecimal.valueOf(0));
          rowOne.add(BigDecimal.valueOf(0));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(1));
          rowTwo.add(BigDecimal.valueOf(5));
          rowTwo.add(BigDecimal.valueOf(0));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowThree.add(BigDecimal.valueOf(2));
          rowThree.add(BigDecimal.valueOf(-3));
          rowThree.add(BigDecimal.valueOf(7));
          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> input = new Matrix<>(3, 3, mat);

          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> FixedLinearAlgebra.using(seq).input(input, 1))
                  .seq((seq, a) -> new InvertTriangularMatrix(
                      a).buildComputation(seq)).seq((seq, inverse) -> {
                Matrix<DRes<BigDecimal>> open = MatrixUtils
                    .map(inverse, FixedNumeric.using(seq)::open);
                return () -> open;
              }).seq((seq, open) -> {
                Matrix<BigDecimal> out = MatrixUtils.map(open, DRes::out);
                return () -> out;
              });

          Matrix<BigDecimal> out = runApplication(testApplication);

          for (int i = 0; i < out.getHeight(); i++) {
            for (int j = i + 1; j < out.getWidth(); j++) {
              assertEquals(i == j ? 1.0 : 0.0,
                  innerProduct(input.getRow(i), out.getColumn(j)).doubleValue(), 0.01);
            }
          }
        }
      };
    }
  }

  public static class TestEigenvalues<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(-6));
          rowOne.add(BigDecimal.valueOf(3));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(4));
          rowTwo.add(BigDecimal.valueOf(5));

          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          Matrix<BigDecimal> input = new Matrix<>(2, 2, mat);

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> FixedLinearAlgebra.using(seq).input(input, 1))
                  .seq((seq, a) -> new QRAlgorithm(
                      a, 10).buildComputation(seq)).seq((seq, eigenvalues) -> {
                List<DRes<BigDecimal>> open = eigenvalues.stream()
                    .map(FixedNumeric.using(seq)::open).collect(
                        Collectors.toList());
                return () -> open;
              }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return () -> out;
              });

          List<BigDecimal> out = runApplication(testApplication);

          assertEquals(-7.0, out.get(0).doubleValue(), 0.01);
          assertEquals(6.0, out.get(1).doubleValue(), 0.01);
        }
      };
    }
  }

  public static class TestForwardsubstitution<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(1));
          rowOne.add(BigDecimal.valueOf(0));
          rowOne.add(BigDecimal.valueOf(0));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(-4));
          rowTwo.add(BigDecimal.valueOf(2));
          rowTwo.add(BigDecimal.valueOf(0));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowThree.add(BigDecimal.valueOf(3));
          rowThree.add(BigDecimal.valueOf(-5));
          rowThree.add(BigDecimal.valueOf(1));

          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> inputA = new Matrix<>(3, 3, mat);

          ArrayList<BigDecimal> inputB = new ArrayList<>(
              Arrays.asList(BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(1)));

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                Pair<DRes<Matrix<DRes<SFixed>>>, DRes<ArrayList<DRes<SFixed>>>> inputs = new Pair<>(
                    FixedLinearAlgebra.using(seq).input(inputA, 1),
                    FixedLinearAlgebra.using(seq).input(inputB, 1));
                return () -> inputs;
              }).seq((seq, inputs) -> new ForwardSubstitution(
                  inputs.getFirst().out(), inputs.getSecond().out()).buildComputation(seq))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return () -> open;
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return () -> out;
              });

          List<BigDecimal> out = runApplication(testApplication);
          double[] product = multiply(inputA, out).stream().mapToDouble(BigDecimal::doubleValue)
              .toArray();
          Assert.assertArrayEquals(inputB.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
              product, 0.001);
        }
      };
    }
  }

  public static class TestLinearInverseProblem<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(1));
          rowOne.add(BigDecimal.valueOf(1));
          rowOne.add(BigDecimal.valueOf(1));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(0));
          rowTwo.add(BigDecimal.valueOf(2));
          rowTwo.add(BigDecimal.valueOf(5));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowThree.add(BigDecimal.valueOf(2));
          rowThree.add(BigDecimal.valueOf(5));
          rowThree.add(BigDecimal.valueOf(-1));

          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> inputA = new Matrix<>(3, 3, mat);

          ArrayList<BigDecimal> inputB = new ArrayList<>(
              Arrays.asList(BigDecimal.valueOf(6), BigDecimal.valueOf(-4), BigDecimal.valueOf(27)));

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                Pair<DRes<Matrix<DRes<SFixed>>>, DRes<ArrayList<DRes<SFixed>>>> inputs = new Pair<>(
                    FixedLinearAlgebra.using(seq).input(inputA, 1),
                    FixedLinearAlgebra.using(seq).input(inputB, 1));
                return () -> inputs;
              }).seq((seq, inputs) -> new LinearInverseProblem(
                  inputs.getFirst().out(), inputs.getSecond().out()).buildComputation(seq))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return () -> open;
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return () -> out;
              });

          List<BigDecimal> out = runApplication(testApplication);
          double[] product = multiply(inputA, out).stream().mapToDouble(BigDecimal::doubleValue)
              .toArray();
          Assert.assertArrayEquals(inputB.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
              product, 0.001);
        }
      };
    }
  }


  public static class TestBackwardSubstitution<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(1));
          rowOne.add(BigDecimal.valueOf(-2));
          rowOne.add(BigDecimal.valueOf(1));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(0));
          rowTwo.add(BigDecimal.valueOf(1));
          rowTwo.add(BigDecimal.valueOf(6));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowThree.add(BigDecimal.valueOf(0));
          rowThree.add(BigDecimal.valueOf(0));
          rowThree.add(BigDecimal.valueOf(1));

          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> inputA = new Matrix<>(3, 3, mat);

          ArrayList<BigDecimal> inputB = new ArrayList<>(
              Arrays.asList(BigDecimal.valueOf(4), BigDecimal.valueOf(-1), BigDecimal.valueOf(2)));

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                Pair<DRes<Matrix<DRes<SFixed>>>, DRes<ArrayList<DRes<SFixed>>>> inputs = new Pair<>(
                    FixedLinearAlgebra.using(seq).input(inputA, 1),
                    FixedLinearAlgebra.using(seq).input(inputB, 1));
                return () -> inputs;
              }).seq((seq, inputs) -> new BackwardSubstitution(
                  inputs.getFirst().out(), inputs.getSecond().out()).buildComputation(seq))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return () -> open;
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return () -> out;
              });

          List<BigDecimal> out = runApplication(testApplication);
          System.out.println(out);
          double[] product = multiply(inputA, out).stream().mapToDouble(BigDecimal::doubleValue)
              .toArray();
          Assert.assertArrayEquals(inputB.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
              product, 0.001);
        }
      };
    }
  }

}
