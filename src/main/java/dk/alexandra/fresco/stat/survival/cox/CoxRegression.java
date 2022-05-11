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
import dk.alexandra.fresco.stat.survival.cox.CoxHessian.CoxHessianInternal.State;
import dk.alexandra.fresco.stat.utils.Triple;
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

      DRes<State> hessianState = seq
          .seq(new CoxHessian.CoxHessianInternal(sortedData, tiedGroups, beta));
      return Pair.lazy(beta, hessianState);

    }).par((par, betaAndHessianState) -> {

      DRes<Matrix<DRes<SFixed>>> hInverse = AdvancedLinearAlgebra.using(par)
          .moorePenrosePseudoInverse(betaAndHessianState.getSecond().out().hessian.out());

      DRes<SFixed> G = new CoxPartialLikelihoodRatioTest(sortedData, tiedGroups,
          betaAndHessianState.getFirst(), betaAndHessianState.getSecond().out())
          .buildComputation(par);

      return DRes.of(new Triple<>(betaAndHessianState.getFirst(), hInverse, G));

    }).seq((seq, betaAndHInverseAndG) -> {

      List<DRes<SFixed>> errors = IntStream.range(0, beta.length)
          .mapToObj(i -> betaAndHInverseAndG.getSecond().out().getRow(i).get(i)).map(xii ->
              seq.seq(b -> AdvancedFixedNumeric.using(b).sqrt(FixedNumeric.using(b).sub(0, xii)))
          ).collect(Collectors.toList());

      return new CoxRegressionResult(betaAndHInverseAndG.getFirst(), betaAndHInverseAndG.getThird(),
          errors);

    });

  }

  public static class CoxRegressionResult implements DRes<CoxRegressionResult> {

    private final DRes<SFixed> G;
    private final List<DRes<SFixed>> model;
    private final List<DRes<SFixed>> standardErrors;

    CoxRegressionResult(List<DRes<SFixed>> model, DRes<SFixed> G,
        List<DRes<SFixed>> standardErrors) {
      this.model = model;
      this.G = G;
      this.standardErrors = standardErrors;
    }

    public List<DRes<SFixed>> getModel() {
      return model;
    }

    public List<DRes<SFixed>> getStandardErrors() {
      return standardErrors;
    }

    public DRes<SFixed> getPartialLikelihoodRatioTest() {
      return G;
    }

    @Override
    public CoxRegressionResult out() {
      return this;
    }
  }
}
