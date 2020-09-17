package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.math.Exponential;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.stat.utils.RealUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import dk.alexandra.fresco.stat.utils.sort.FindTiedGroups;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CoxGradient implements Computation<List<DRes<SReal>>, ProtocolBuilderNumeric> {

  private final List<SurvivalInfoDiscrete> data;
  private final List<DRes<SReal>> beta;

  /**
   * Compute the gradient of the score function for a Cox model on the given data with coefficients
   * beta and learning rate alpha. The data is assumed to be sorted on the time parameter.
   *
   * @param data
   * @param beta
   */
  public CoxGradient(List<SurvivalInfoDiscrete> data, List<DRes<SReal>> beta) {
    this.data = data;
    this.beta = beta;
  }

  @Override
  public DRes<List<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      State state = new State();

      // Compute exp(beta_i) for all i's
      for (int i = 0; i < beta.size(); i++) {
        state.expBeta.add(new Exponential(beta.get(i)).buildComputation(par));
      }
      return () -> state;
    }).seq((par, state) -> {
      // Compute exp(k * beta_i) for k = 1...k_i (number of different values for covariate i)
      for (int i = 0; i < beta.size(); i++) {
        state.expPipe.add(new ArrayList<>());
        state.expPipe.get(i).add(par.realNumeric().known(1));
        for (int j = 1; j < data.get(0).getCovariates().get(i).getFirst().size(); j++) {
          state.expPipe.get(i).add(par.realNumeric().mult(state.expPipe.get(i).get(j - 1),
              state.expBeta.get(i)));
        }
      }
      return () -> state;
    }).par((par, state) -> {
      // Compute exp(x_ji beta_i) for all i and j
      for (int j = 0; j < data.size(); j++) {
        state.partialThetas.add(new ArrayList<>());
        for (int i = 0; i < beta.size(); i++) {
          state.partialThetas.get(j).add(
              VectorUtils.innerProductWithBitvector(data.get(j).getCovariates().get(i).getFirst(),
                  state.expPipe.get(i), par));
        }
      }
      return () -> state;
    }).par((par, state) -> {
      // compute theta_j = exp(x_j . beta) for all data points j
      for (int j = 0; j < data.size(); j++) {
        state.thetas.add(RealUtils.product(state.partialThetas.get(j), par));
      }
      return () -> state;
    }).par((par, state) -> {
      // Convert x_ji's to fixed point
      for (int j = 0; j < data.size(); j++) {
        int finalJ = j;
        state.x.add(VectorUtils.listBuilder(beta.size(), i ->
            par.realNumeric().fromSInt(data.get(finalJ).getCovariates().get(i).getSecond())));
      }
      return () -> state;
    }).par((par, state) -> {
      // Compute theta_j * x_j
      for (int j = 0; j < data.size(); j++) {
        state.sum1terms.add(VectorUtils.scale(state.x.get(j), state.thetas.get(j), par));
      }
      return () -> state;
    }).seq((seq, state) -> {

      // Find ties
      DRes<List<BigInteger>> tiedGroups = new FindTiedGroups(
          VectorUtils.listBuilder(data.size(), i -> data.get(i).getTime())).buildComputation(seq);
      return Pair.lazy(tiedGroups, state);

    }).seq((seq, p) -> {

      // Compute accumulated sums of theta_j * x_j's and theta_j's
      State state = p.getSecond();
      state.tiedGroups = p.getFirst().out();

      state.sum1.add(state.sum1terms.get(0));
      for (int j = 1; j < state.sum1terms.size(); j++) {
        state.sum1.add(VectorUtils.add(state.sum1.get(j - 1), state.sum1terms.get(j), seq));
      }

      state.sum2.add(state.thetas.get(0));
      for (int j = 1; j < state.thetas.size(); j++) {
        state.sum2.add(seq.realNumeric().add(state.sum2.get(j - 1), state.thetas.get(j)));
      }

      // Correct for ties
      for (int j = data.size() - 2; j >= 0; j--) {
        if (state.tiedGroups.get(j).equals(state.tiedGroups.get(j+1))) {
          state.sum1.set(j, state.sum1.get(j+1));
          state.sum2.set(j, state.sum2.get(j+1));
        }
      }

      return () -> state;
    }).par((par, state) -> {
      // Compute ratios of sum1 and sum2
      for (int j = 0; j < data.size(); j++) {
        state.ratios.add(VectorUtils.div(state.sum1.get(j), state.sum2.get(j), par));
      }
      return () -> state;
    }).par((par, state) -> {
      // The terms in the final sum are x_j - ratio_j
      List<List<DRes<SReal>>> terms = new ArrayList<>();
      for (int j = 0; j < data.size(); j++) {
        terms.add(VectorUtils.sub(state.x.get(j), state.ratios.get(j), par));
      }
      state.terms = terms;
      return () -> state;
    }).par((par, state) -> {
      // Only include terms with status = 1
      List<DRes<SInt>> censored = VectorUtils
          .listBuilder(data.size(), j -> data.get(j).getCensored());
      List<DRes<SReal>> result = VectorUtils
          .listBuilder(beta.size(), i -> VectorUtils.innerProductWithBitvector(
              censored,
              // The i'th entries in the terms
              VectorUtils.listBuilder(data.size(), j -> state.terms.get(j).get(i)),
              par));
      return () -> result;
    });
  }

  private class State {

    List<DRes<SReal>> expBeta = new ArrayList<>();
    List<List<DRes<SReal>>> expPipe = new ArrayList<>();
    List<List<DRes<SReal>>> partialThetas = new ArrayList<>();
    List<DRes<SReal>> thetas = new ArrayList<>();
    List<List<DRes<SReal>>> x = new ArrayList<>();
    List<List<DRes<SReal>>> sum1terms = new ArrayList<>();
    List<List<DRes<SReal>>> sum1 = new ArrayList<>();
    List<DRes<SReal>> sum2 = new ArrayList<>();
    List<List<DRes<SReal>>> ratios = new ArrayList<>();
    List<List<DRes<SReal>>> terms = new ArrayList<>();
    List<BigInteger> tiedGroups = new ArrayList<>();

  }
}
