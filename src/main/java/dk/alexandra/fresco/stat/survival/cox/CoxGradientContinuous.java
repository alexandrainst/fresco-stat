package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.math.Exponential;
import dk.alexandra.fresco.stat.descriptive.sort.FindTiedGroups;
import dk.alexandra.fresco.stat.survival.SurvivalInfoContinuous;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CoxGradientContinuous implements
    Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<SurvivalInfoContinuous> data;
  private final List<DRes<SFixed>> beta;

  /**
   * Compute the gradient of the score function for a Cox model on the given data with coefficients
   * beta and learning rate alpha. The data is assumed to be sorted on the time parameter.
   *
   * @param data
   * @param beta
   */
  public CoxGradientContinuous(List<SurvivalInfoContinuous> data, List<DRes<SFixed>> beta) {
    this.data = data;
    this.beta = beta;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      State state = new State();

      for (int j = 0; j < data.size(); j++) {
        List<DRes<SFixed>> row = new ArrayList<>();
        for (int i = 0; i < beta.size(); i++) {
          row.add(FixedNumeric.using(par).mult(beta.get(i), data.get(j).getCovariates()
              .get(i)));
        }
        state.innerProductTerms.add(row);
      }

      return DRes.of(state);
    }).par((par, state) -> {
      for (int j = 0; j < data.size(); j++) {
        state.innerProducts
            .add(AdvancedFixedNumeric.using(par).sum(state.innerProductTerms.get(j)));
      }
      return DRes.of(state);
    }).par((par, state) -> {
      // compute theta_j = exp(x_j . beta) for all data points j
      for (int j = 0; j < data.size(); j++) {
        state.thetas.add(new Exponential(state.innerProducts.get(j)).buildComputation(par));
      }
      return DRes.of(state);
    }).par((par, state) -> {
      // Compute theta_j * x_j
      for (int j = 0; j < data.size(); j++) {
        state.sum1terms
            .add(VectorUtils.scale(data.get(j).getCovariates(), state.thetas.get(j), par));
      }
      return DRes.of(state);
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
        state.sum2.add(FixedNumeric.using(seq).add(state.sum2.get(j - 1), state.thetas.get(j)));
      }

      // Correct for ties
      for (int j = data.size() - 2; j >= 0; j--) {
        if (state.tiedGroups.get(j).equals(state.tiedGroups.get(j + 1))) {
          state.sum1.set(j, state.sum1.get(j + 1));
          state.sum2.set(j, state.sum2.get(j + 1));
        }
      }

      return DRes.of(state);
    }).par((par, state) -> {
      // Compute ratios of sum1 and sum2
      for (int j = 0; j < data.size(); j++) {
        state.ratios.add(VectorUtils.div(state.sum1.get(j), state.sum2.get(j), par));
      }
      return DRes.of(state);
    }).par((par, state) -> {
      // The terms in the final sum are x_j - ratio_j
      List<List<DRes<SFixed>>> terms = new ArrayList<>();
      for (int j = 0; j < data.size(); j++) {
        terms.add(VectorUtils.sub(data.get(j).getCovariates(), state.ratios.get(j), par));
      }
      state.terms = terms;
      return DRes.of(state);
    }).par((par, state) -> {
      // Only include terms with status = 1
      List<DRes<SInt>> censored = VectorUtils
          .listBuilder(data.size(), j -> data.get(j).getCensored());
      List<DRes<SFixed>> result = VectorUtils
          .listBuilder(beta.size(), i -> VectorUtils.innerProductWithBitvector(
              censored,
              // The i'th entries in the terms
              VectorUtils.listBuilder(data.size(), j -> state.terms.get(j).get(i)),
              par));
      return DRes.of(result);
    });
  }

  private class State {

    List<List<DRes<SFixed>>> innerProductTerms = new ArrayList<>();
    List<DRes<SFixed>> innerProducts = new ArrayList<>();
    List<DRes<SFixed>> thetas = new ArrayList<>();
    List<List<DRes<SFixed>>> sum1terms = new ArrayList<>();
    List<List<DRes<SFixed>>> sum1 = new ArrayList<>();
    List<DRes<SFixed>> sum2 = new ArrayList<>();
    List<List<DRes<SFixed>>> ratios = new ArrayList<>();
    List<List<DRes<SFixed>>> terms = new ArrayList<>();
    List<BigInteger> tiedGroups = new ArrayList<>();

  }
}
