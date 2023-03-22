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
import dk.alexandra.fresco.stat.complex.Open;
import dk.alexandra.fresco.stat.complex.OpenComplex;
import dk.alexandra.fresco.stat.complex.SecretComplex;
import dk.alexandra.fresco.stat.linearalgebra.Convolution;
import dk.alexandra.fresco.stat.linearalgebra.FFT;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
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

  private static Matrix<BigDecimal> multiply(Matrix<BigDecimal> a, Matrix<BigDecimal> b) {
    assert a.getWidth() == b.getHeight();
    ArrayList<ArrayList<BigDecimal>> rows = new ArrayList<>();
    for (int i = 0; i < a.getHeight(); i++) {
      ArrayList<BigDecimal> row = new ArrayList<>();
      for (int j = 0; j < b.getWidth(); j++) {
        BigDecimal aij = BigDecimal.ZERO;
        for (int k = 0; k < a.getWidth(); k++) {
          aij = aij.add(a.getRow(i).get(k).multiply(b.getRow(k).get(j)));
        }
        row.add(aij);
      }
      rows.add(row);
    }
    return new Matrix<>(a.getHeight(), b.getWidth(), rows);
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
                ArrayList<DRes<SFixed>> inputA =
                    a.stream().map(numeric::known).collect(Collectors.toCollection(ArrayList::new));
                ArrayList<DRes<SFixed>> inputB =
                    b.stream().map(numeric::known).collect(Collectors.toCollection(ArrayList::new));
                ArrayList<DRes<SFixed>> inputC =
                    c.stream().map(numeric::known).collect(Collectors.toCollection(ArrayList::new));
                return AdvancedLinearAlgebra.using(seq)
                    .gramSchmidt(List.of(inputA, inputB, inputC));
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

  public static class TestQRDcomposition<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final long seed;
    private final boolean square;

    public TestQRDcomposition(long seed, boolean square) {
      this.seed = seed;
      this.square = square;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<>() {

        final Random random = new Random(seed);
        final int m = random.nextInt(5) + 1;
        final int n = square ? m : random.nextInt(m) + 1;
        final Matrix<BigDecimal> input = MatrixUtils.buildMatrix(m, n, (i,j) -> BigDecimal.valueOf(20 * random.nextDouble() - 10));

        @Override
        public void test() {
          Application<Pair<Matrix<BigDecimal>, Matrix<BigDecimal>>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> FixedLinearAlgebra.using(seq).input(input, 1))
              .seq((seq, a) -> AdvancedLinearAlgebra.using(seq).qrDecomposition(
                  a)).seq((seq, qr) -> {
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

          RealMatrix a = new Array2DRowRealMatrix(m, n);
          for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
              a.setEntry(i, j, input.getRow(i).get(j).doubleValue());
            }
          }
          QRDecomposition qrDecomposition = new QRDecomposition(a);
          RealMatrix q = qrDecomposition.getQ();
          RealMatrix r = qrDecomposition.getR();

          for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
              assertEquals(Math.abs(q.getEntry(i, j)), Math.abs(out.getFirst().getRow(i).get(j).doubleValue()), 0.01);
            }
          }

          for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
              assertEquals(Math.abs(r.getEntry(i, j)), Math.abs(out.getSecond().getRow(i).get(j).doubleValue()), 0.01);
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
                  .seq((seq, a) -> AdvancedLinearAlgebra.using(seq).invertLowerTriangularMatrix(
                      a)).seq((seq, inverse) -> {
                Matrix<DRes<BigDecimal>> open = MatrixUtils
                    .map(inverse, FixedNumeric.using(seq)::open);
                return DRes.of(open);
              }).seq((seq, open) -> {
                Matrix<BigDecimal> out = MatrixUtils.map(open, DRes::out);
                return DRes.of(out);
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
                  .seq((seq, a) -> AdvancedLinearAlgebra.using(seq).iterativeEigenvalues(
                      a, 10)).seq((seq, eigenvalues) -> {
                List<DRes<BigDecimal>> open = eigenvalues.stream()
                    .map(FixedNumeric.using(seq)::open).collect(
                        Collectors.toList());
                return DRes.of(open);
              }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
              });

          List<BigDecimal> out = runApplication(testApplication);

          assertEquals(-7.0, out.get(0).doubleValue(), 0.01);
          assertEquals(6.0, out.get(1).doubleValue(), 0.01);
        }
      };
    }
  }

  public static class TestForwardSubstitution<ResourcePoolT extends ResourcePool>
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
                return DRes.of(inputs);
              }).seq((seq, inputs) -> AdvancedLinearAlgebra.using(seq).forwardSubstitution(
                  inputs.getFirst().out(), inputs.getSecond().out()))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return DRes.of(open);
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
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
                return DRes.of(inputs);
              }).seq((seq, inputs) -> AdvancedLinearAlgebra.using(seq).linearInverseProblem(
                  inputs.getFirst().out(), inputs.getSecond().out()))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return DRes.of(open);
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
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

  public static class TestLinearInverseProblemUnderdetermined<ResourcePoolT extends ResourcePool>
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
          rowTwo.add(BigDecimal.valueOf(1));
          rowTwo.add(BigDecimal.valueOf(1));
          rowTwo.add(BigDecimal.valueOf(2));

          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          Matrix<BigDecimal> inputA = new Matrix<>(2, 3, mat);

          ArrayList<BigDecimal> inputB = new ArrayList<>(
              Arrays.asList(BigDecimal.valueOf(1), BigDecimal.valueOf(3)));

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                Pair<DRes<Matrix<DRes<SFixed>>>, DRes<ArrayList<DRes<SFixed>>>> inputs = new Pair<>(
                    FixedLinearAlgebra.using(seq).input(inputA, 1),
                    FixedLinearAlgebra.using(seq).input(inputB, 1));
                return DRes.of(inputs);
              }).seq((seq, inputs) -> AdvancedLinearAlgebra.using(seq).linearInverseProblem(
                  inputs.getFirst().out(), inputs.getSecond().out()))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return DRes.of(open);
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
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

  public static class TestLinearInverseProblemNoSolution<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(1));
          rowOne.add(BigDecimal.valueOf(1));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(2));
          rowTwo.add(BigDecimal.valueOf(1));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowThree.add(BigDecimal.valueOf(3));
          rowThree.add(BigDecimal.valueOf(2));

          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> inputA = new Matrix<>(3, 2, mat);

          ArrayList<BigDecimal> inputB = new ArrayList<>(
              Arrays.asList(BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(3)));

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                Pair<DRes<Matrix<DRes<SFixed>>>, DRes<ArrayList<DRes<SFixed>>>> inputs = new Pair<>(
                    FixedLinearAlgebra.using(seq).input(inputA, 1),
                    FixedLinearAlgebra.using(seq).input(inputB, 1));
                return DRes.of(inputs);
              }).seq((seq, inputs) -> AdvancedLinearAlgebra.using(seq).linearInverseProblem(
                  inputs.getFirst().out(), inputs.getSecond().out()))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return DRes.of(open);
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
              });

          List<BigDecimal> out = runApplication(testApplication);
          Array2DRowRealMatrix a = new Array2DRowRealMatrix(new double[][]{
              inputA.getRow(0).stream().mapToDouble(BigDecimal::doubleValue).toArray(),
              inputA.getRow(1).stream().mapToDouble(BigDecimal::doubleValue).toArray(),
              inputA.getRow(2).stream().mapToDouble(BigDecimal::doubleValue).toArray(),
          });
          DecompositionSolver solver = new SingularValueDecomposition(a).getSolver();
          RealVector b = solver.solve(
              new ArrayRealVector(inputB.stream().mapToDouble(BigDecimal::doubleValue).toArray()));
          Assert.assertArrayEquals(out.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
              b.toArray(), 0.001);
        }
      };
    }
  }

  public static class TestLinearInverseProblemOverdetermined<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> rowOne = new ArrayList<>();
          rowOne.add(BigDecimal.valueOf(2));
          rowOne.add(BigDecimal.valueOf(-3));
          ArrayList<BigDecimal> rowTwo = new ArrayList<>();
          rowTwo.add(BigDecimal.valueOf(3));
          rowTwo.add(BigDecimal.valueOf(-2));
          ArrayList<BigDecimal> rowThree = new ArrayList<>();
          rowThree.add(BigDecimal.valueOf(1));
          rowThree.add(BigDecimal.valueOf(-1));

          ArrayList<ArrayList<BigDecimal>> mat = new ArrayList<>();
          mat.add(rowOne);
          mat.add(rowTwo);
          mat.add(rowThree);
          Matrix<BigDecimal> inputA = new Matrix<>(3, 2, mat);

          ArrayList<BigDecimal> inputB = new ArrayList<>(
              Arrays.asList(BigDecimal.valueOf(1), BigDecimal.valueOf(4), BigDecimal.valueOf(1)));

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                Pair<DRes<Matrix<DRes<SFixed>>>, DRes<ArrayList<DRes<SFixed>>>> inputs = new Pair<>(
                    FixedLinearAlgebra.using(seq).input(inputA, 1),
                    FixedLinearAlgebra.using(seq).input(inputB, 1));
                return DRes.of(inputs);
              }).seq((seq, inputs) -> AdvancedLinearAlgebra.using(seq).linearInverseProblem(
                  inputs.getFirst().out(), inputs.getSecond().out()))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return DRes.of(open);
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
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

  public static class TestBackSubstitution<ResourcePoolT extends ResourcePool>
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
                return DRes.of(inputs);
              }).seq((seq, inputs) -> AdvancedLinearAlgebra.using(seq).backSubstitution(
                  inputs.getFirst().out(), inputs.getSecond().out()))
                  .seq((seq, x) -> {
                    List<DRes<BigDecimal>> open = x.stream()
                        .map(FixedNumeric.using(seq)::open).collect(
                            Collectors.toList());
                    return DRes.of(open);
                  }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
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

  public static class TestMoorePenrosePseudoInverse<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    private final long seed;
    private final boolean square;

    public TestMoorePenrosePseudoInverse(long seed, boolean square) {
      this.seed = seed;
      this.square = square;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

      return new TestThread<>() {

        final Random random = new Random(seed);
        final int m = random.nextInt(5) + 1;
        final int n = square ? m : random.nextInt(m) + 1;
        final Matrix<BigDecimal> input = MatrixUtils.buildMatrix(m, n, (i,j) -> BigDecimal.valueOf(20 * random.nextDouble() - 10));

        @Override
        public void test() {

          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> FixedLinearAlgebra.using(seq).input(input, 1))
                  .seq((seq, a) -> AdvancedLinearAlgebra.using(seq).moorePenrosePseudoInverse(
                      a)).seq((seq, inverse) -> {
                Matrix<DRes<BigDecimal>> open = MatrixUtils
                    .map(inverse, FixedNumeric.using(seq)::open);
                return DRes.of(open);
              }).seq((seq, open) -> {
                Matrix<BigDecimal> out = MatrixUtils.map(open, DRes::out);
                return DRes.of(out);
              });

          Matrix<BigDecimal> out = runApplication(testApplication);

          RealMatrix a = new Array2DRowRealMatrix(m, n);
          for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
              a.setEntry(i, j, input.getRow(i).get(j).doubleValue());
            }
          }

          RealMatrix b = new SingularValueDecomposition(a).getSolver().getInverse();
          for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
              Assert.assertEquals(b.getEntry(i, j), out.getRow(i).get(j).doubleValue(), 0.02);
            }
          }
        }
      };
    }
  }

  public static class TestConvolution<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =

              root -> root.seq(seq -> {
                ArrayList<BigDecimal> a = List.of(1., 0., -1.).stream().map(BigDecimal::valueOf).collect(Collectors.toCollection(ArrayList::new));
                ArrayList<BigDecimal> b = List.of(0., 1., 2.).stream().map(BigDecimal::valueOf).collect(Collectors.toCollection(ArrayList::new));

                DRes<ArrayList<DRes<SFixed>>> aSecret = FixedLinearAlgebra.using(seq).input(a, 1);
                DRes<ArrayList<DRes<SFixed>>> bSecret = FixedLinearAlgebra.using(seq).input(b, 2);
                return Pair.lazy(aSecret, bSecret);
              }).seq((seq, inputs) -> {
                DRes<ArrayList<DRes<SFixed>>> result = new Convolution(inputs.getFirst().out(), inputs.getSecond().out()).buildComputation(seq);
                return FixedLinearAlgebra.using(seq).openArrayList(result);
              }).seq((seq, result) -> DRes.of(result.stream().map(DRes::out).collect(Collectors.toList())));

          List<BigDecimal> output = runApplication(testApplication);
          Assert.assertArrayEquals(new double[] {0, 1, 2, -1, -2}, output.stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.001);
        }
      };
    }
  }

  public static class TestFFT<ResourcePoolT extends ResourcePool>
          extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          ArrayList<BigDecimal> x = List.of(1.0 ,2.0,3.0,4.0).stream().map(BigDecimal::valueOf).collect(Collectors.toCollection(ArrayList::new));

          Application<List<OpenComplex>, ProtocolBuilderNumeric> testApplication = builder ->
                  builder.par(par -> DRes.of(x.stream().map(xi -> FixedNumeric.using(par).input(xi, 1)).map(xi -> DRes.of(new SecretComplex(xi, xi))).collect(Collectors.toCollection(ArrayList::new)))
                          ).seq((seq, a) -> new FFT(a).buildComputation(seq)).seq((seq, fft) ->
                            DRes.of(fft.stream()
                                    .map(z -> new Open(z.out()).buildComputation(seq)).collect(Collectors.toList()))
                          ).seq((seq, open) ->
                            DRes.of(open.stream().map(DRes::out).collect(Collectors.toList()))
                          );

          List<OpenComplex> out = runApplication(testApplication);
          System.out.println(out);
        }
      };
    }
  }

}
