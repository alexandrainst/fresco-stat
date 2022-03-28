package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.survival.SurvivalEntrySorter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Estimate the coefficients of a Cox proportional hazards model on the given data using gradient
 * descent.
 */
public class CoxRegression implements
    Computation<CoxRegression.CoxRegressionResult, ProtocolBuilderNumeric> {

  private final int iterations;
  private final double alpha;
  private final double[] beta;
  private final SurvivalEntrySorter sorter;
  private List<BigInteger> tiedGroups;
  private List<SurvivalEntry> sortedData;

  /**
   * Estimate the coefficients of a Cox model on the given data using gradient descent.
   *
   * @param data       The data.
   * @param iterations The number of iterations.
   * @param alpha      The learning rate.
   */
  public CoxRegression(List<SurvivalEntry> data, int iterations, double alpha,
      double[] beta) {
    this.iterations = iterations;
    this.alpha = alpha;
    this.beta = beta;
    this.sorter = new SurvivalEntrySorter(data);
  }

  @Override
  public DRes<CoxRegressionResult> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.seq(sorter).par((par, sorted) -> {

      this.tiedGroups = sorted.getSecond();
      this.sortedData = sorted.getFirst();

      FixedNumeric numeric = FixedNumeric.using(par);
      List<DRes<SFixed>> initialBeta = Arrays.stream(beta).mapToObj(numeric::known)
          .collect(
              Collectors.toList());
      return DRes.of(initialBeta);

    }).seq((seq, init) ->

        seq.seq(new CoxOptimiser(sortedData, tiedGroups, init, iterations, alpha))

    ).seq((seq, beta) -> {

      DRes<Matrix<DRes<SFixed>>> h = seq.seq(new CoxHessian(sortedData, tiedGroups, beta));
      return Pair.lazy(beta, h);

    }).seq((seq, betaAndH) -> {

      DRes<Matrix<DRes<SFixed>>> hInverse = AdvancedLinearAlgebra.using(seq)
          .moorePenrosePseudoInverse(betaAndH.getSecond().out());
      return Pair.lazy(betaAndH.getFirst(), hInverse);

    }).seq((seq, betaAndHInverse) -> {

      List<DRes<SFixed>> errors = IntStream.range(0, beta.length)
          .mapToObj(i -> betaAndHInverse.getSecond().out().getRow(i).get(i)).map(xii ->
              seq.seq(b -> AdvancedFixedNumeric.using(b).sqrt(FixedNumeric.using(b).sub(0, xii)))
          ).collect(Collectors.toList());

      return new CoxRegressionResult(betaAndHInverse.getFirst(), errors);

    });

  }

  public static class CoxRegressionResult implements DRes<CoxRegressionResult> {

    private final List<DRes<SFixed>> model;
    private final List<DRes<SFixed>> standardErrors;

    CoxRegressionResult(List<DRes<SFixed>> model, List<DRes<SFixed>> standardErrors) {
      this.model = model;
      this.standardErrors = standardErrors;
    }

    public List<DRes<SFixed>> getModel() {
      return model;
    }

    public List<DRes<SFixed>> getStandardErrors() {
      return standardErrors;
    }

    @Override
    public CoxRegressionResult out() {
      return this;
    }
  }
}
