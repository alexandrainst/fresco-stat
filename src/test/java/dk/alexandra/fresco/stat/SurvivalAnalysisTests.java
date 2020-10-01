package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertEquals;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.survival.SurvivalInfoContinuous;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.stat.survival.cox.CoxGradientDiscrete;
import dk.alexandra.fresco.stat.survival.cox.CoxRegressionContinuous;
import dk.alexandra.fresco.stat.survival.cox.CoxRegressionDiscrete;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SurvivalAnalysisTests {

  public static List<SurvivalInfoDiscrete> survivalAnalysisDataset(ProtocolBuilderNumeric builder) {
    return survivalAnalysisDataset(builder, false);
  }

  public static List<SurvivalInfoContinuous> survivalAnalysisDatasetContinuous(ProtocolBuilderNumeric builder) {
    List<SurvivalInfoDiscrete> data = survivalAnalysisDataset(builder);

    return data.stream().map(d -> new SurvivalInfoContinuous(d.getCovariates().stream().map(e -> FixedNumeric
        .using(builder).fromSInt(e.get(1))).collect(
        Collectors.toList()), d.getTime(), d.getCensored())).collect(Collectors.toList());
  }

  public static List<SurvivalInfoDiscrete> survivalAnalysisDataset(ProtocolBuilderNumeric builder,
      boolean sorted) {

    // Dataset from https://www.statsdirect.com/help/survival_analysis/cox_regression.htm
    int[] group1 = new int[]{6, 19, 32, 42, 42, 43, 94, 126, 169, 207, 211, 227, 253, 255, 270,
        310, 316, 335, 346};
    int[] group1censor = new int[]{1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0};
    int[] group2 = new int[]{4, 6, 10, 11, 11, 11, 13, 17, 20, 20, 21, 22, 24, 24, 29, 30, 30,
        31, 33, 34, 35, 39, 40, 41, 43, 45, 46, 50, 56, 61, 61, 63, 68, 82, 85, 88, 89, 90,
        93, 104, 110, 134, 137, 160, 169, 171, 173, 175, 184, 201, 222, 235, 247, 260, 284,
        290, 291, 302, 304, 341, 345};
    int[] group2censor = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0};

    int[][] combined = new int[group1.length + group2.length][];
    for (int i = 0; i < group1.length; i++) {
      combined[i] = new int[]{0, group1[i], group1censor[i]};
    }
    for (int i = 0; i < group2.length; i++) {
      combined[i + group1.length] = new int[]{1, group2[i], group2censor[i]};
    }

    if (sorted) {
      // Sort descending
      Arrays.sort(combined, Comparator.comparingInt(a -> -a[1]));
    }

    List<SurvivalInfoDiscrete> data = new ArrayList<>();
    for (int[] row : combined) {
      if (row[0] == 0) {
        data.add(new SurvivalInfoDiscrete(List.of(
            List.of(builder.numeric().known(1), builder.numeric().known(0))),
            builder.numeric().known(row[1]),
            builder.numeric().known(row[2])));
      } else {
        data.add(new SurvivalInfoDiscrete(List.of(
            List.of(builder.numeric().known(0), builder.numeric().known(1))),
            builder.numeric().known(row[1]),
            builder.numeric().known(row[2])));
      }
    }

    return data;
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

                List<SurvivalInfoDiscrete> input = survivalAnalysisDataset(seq, true);
                return new CoxGradientDiscrete(input,
                    List.of(FixedNumeric.using(seq).known(1))).buildComputation(seq);
              }).seq((seq, beta) -> {
                List<DRes<BigDecimal>> openBeta =
                    beta.stream().map(FixedNumeric.using(seq)::open)
                        .collect(Collectors.toList());
                return () ->
                    openBeta.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(testApplication);
          assertEquals(-0.25862161094245983, output.get(0).doubleValue(), 0.001);
        }
      };
    }
  }

  public static class TestCoxRegressionDiscrete<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {

                List<SurvivalInfoDiscrete> input = survivalAnalysisDataset(seq);
                return new CoxRegressionDiscrete(input, 5, 0.1,
                    new double[]{1}).buildComputation(seq);
              }).seq((seq, beta) -> {
                List<DRes<BigDecimal>> openBeta =
                    beta.stream().map(FixedNumeric.using(seq)::open)
                        .collect(Collectors.toList());
                return () ->
                    openBeta.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(testApplication);
          assertEquals(0.9610201322467578, output.get(0).doubleValue(), 0.001);
        }
      };
    }
  }


  public static class TestCoxRegressionContinuous<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {
          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication = builder -> builder
              .seq(seq -> {

                List<SurvivalInfoContinuous> input = survivalAnalysisDatasetContinuous(seq);
                return new CoxRegressionContinuous(input, 5, 0.1,
                    new double[]{1}).buildComputation(seq);
              }).seq((seq, beta) -> {
                List<DRes<BigDecimal>> openBeta =
                    beta.stream().map(FixedNumeric.using(seq)::open)
                        .collect(Collectors.toList());
                return () ->
                    openBeta.stream().map(DRes::out).collect(Collectors.toList());
              });

          List<BigDecimal> output = runApplication(testApplication);
          assertEquals(0.9610201322467578, output.get(0).doubleValue(), 0.001);
        }
      };
    }
  }

}
