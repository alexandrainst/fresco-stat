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
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.survival.cox.CoxGradient;
import dk.alexandra.fresco.stat.survival.cox.CoxHessian;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SurvivalAnalysisTests {

  public static List<SurvivalEntry> survivalAnalysisDataset(ProtocolBuilderNumeric builder) {
    return survivalAnalysisDataset(builder, false).getFirst();
  }

  public static Pair<List<SurvivalEntry>, List<BigInteger>> survivalAnalysisDataset(
      ProtocolBuilderNumeric builder,
      boolean sorted) {

    // The following data represent the survival in days since entry to the trial of patients with diffuse histiocytic lymphoma. Two different groups of patients, those with stage III and those with stage IV disease, are compared.
    // Dataset from https://www.statsdirect.com/help/survival_analysis/cox_regression.htm

    int[] time = new int[]{6, 19, 32, 42, 42, 43, 94, 126, 169, 207, 211, 227, 253, 255, 270,
        310, 316, 335, 346, 4, 6, 10, 11, 11, 11, 13, 17, 20, 20, 21, 22, 24, 24, 29, 30, 30,
        31, 33, 34, 35, 39, 40, 41, 43, 45, 46, 50, 56, 61, 61, 63, 68, 82, 85, 88, 89, 90,
        93, 104, 110, 134, 137, 160, 169, 171, 173, 175, 184, 201, 222, 235, 247, 260, 284,
        290, 291, 302, 304, 341, 345};
    int[] censor = new int[]{1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int[] group = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    int[][] combined = new int[time.length][];
    for (int i = 0; i < group.length; i++) {
      combined[i] = new int[]{group[i], time[i], censor[i]};
    }

    if (sorted) {
      // Sort descending
      Arrays.sort(combined, Comparator.comparingInt(a -> -a[1]));
    }

    List<SurvivalEntry> data = new ArrayList<>();
    for (int[] row : combined) {
      data.add(new SurvivalEntry(
          List.of(FixedNumeric.using(builder).known(row[0])),
          builder.numeric().known(row[1]),
          builder.numeric().known(row[2])));
    }

    // Simulate hash function (x -> x) to identify ties. In the actual regression, this is done using MiMC encryption
    List<BigInteger> tiedGroups = IntStream.range(0, data.size())
        .mapToObj(i -> BigInteger.valueOf(combined[i][1])).collect(
            Collectors.toList());

    return new Pair<>(data, tiedGroups);
  }

  public static class TestCoxGradient<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {

                Pair<List<SurvivalEntry>, List<BigInteger>> input = survivalAnalysisDataset(seq,
                    true);
                return new CoxGradient(input.getFirst(), input.getSecond(),
                    List.of(FixedNumeric.using(seq).known(1))).buildComputation(seq);
              }).seq((seq, beta) -> {
                List<DRes<BigDecimal>> openBeta =
                    beta.stream().map(FixedNumeric.using(seq)::open)
                        .collect(Collectors.toList());
                return () ->
                    openBeta.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(testApplication);
          assertEquals(0.29766761, output.get(0).doubleValue(), 0.001);
        }
      };
    }
  }

  public static class TestCoxHessian<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<Matrix<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {

                Pair<List<SurvivalEntry>, List<BigInteger>> input = survivalAnalysisDataset(seq,
                    true);
                return new CoxHessian(input.getFirst(), input.getSecond(),
                    List.of(FixedNumeric.using(seq).known(1))).buildComputation(seq);
              }).seq((seq, l) -> {
                return DRes.of(MatrixUtils.map(l, FixedNumeric.using(seq)::open));
              }).seq((seq, l) -> {
                return DRes.of(MatrixUtils.map(l, DRes::out));
              });

          Matrix<BigDecimal> output = runApplication(testApplication);
          assertEquals(-6.96113949, output.getRow(0).get(0).doubleValue(), 0.001);
        }
      };
    }
  }

  public static class TestCoxRegression<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {

                List<SurvivalEntry> input = survivalAnalysisDataset(seq, false).getFirst();
                return Statistics.using(seq).coxRegressionContinuous(input, 2, 1,
                    new double[]{1});

              }).seq((seq, result) -> {
                List<DRes<SFixed>> combined = new ArrayList<>(result.getModel());
                combined.addAll(result.getStandardErrors());

                List<DRes<BigDecimal>> open =
                    combined.stream().map(FixedNumeric.using(seq)::open)
                        .collect(Collectors.toList());

                return () ->
                    open.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(testApplication);
          assertEquals(1.0434, output.get(0).doubleValue(), 0.001);
          assertEquals(0.3847, output.get(1).doubleValue(), 0.001);
        }
      };
    }
  }

}
