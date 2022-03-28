package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Compute the gradient of the score function for a Cox model on the given data with coefficients
 * beta and learning rate alpha. The data is assumed to be sorted on the time parameter.
 */
public class CoxGradient implements Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final CoxGradientInternal internal;

  public CoxGradient(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
      List<DRes<SFixed>> beta) {
    this.internal = new CoxGradientInternal(data, tiedGroups, beta);
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(internal).seq((seq, result) -> DRes.of(result.getResult()));
  }

  static class CoxGradientInternal implements
      Computation<CoxGradientInternal.State, ProtocolBuilderNumeric> {

    private final List<SurvivalEntry> data;
    private final List<DRes<SFixed>> beta;
    private final List<BigInteger> tiedGroups;

    /**
     * Compute the gradient of the score function for a Cox model on the given data with
     * coefficients beta and learning rate alpha. The data is assumed to be sorted on the time
     * parameter.
     */
    public CoxGradientInternal(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
        List<DRes<SFixed>> beta) {
      this.data = data;
      this.tiedGroups = tiedGroups;
      this.beta = beta;
    }

    @Override
    public DRes<State> buildComputation(ProtocolBuilderNumeric builder) {

      int n = data.size();

      return builder.par(par -> {
        State state = new State();

        // compute theta_j = exp(x_j . beta) for all data entries j
        state.thetas = data.stream().map(datum -> par.seq(seq -> {
          AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(seq);
          return advancedFixedNumeric
              .exp(advancedFixedNumeric.innerProduct(beta, datum.getCovariates()));
        })).collect(Collectors.toList());
        return state;

      }).par((par, state) -> {

        // Compute theta_j * x_j
        state.sum1terms = IntStream.range(0, n).mapToObj(
            j -> VectorUtils.scale(data.get(j).getCovariates(), state.thetas.get(j), par)
        ).collect(Collectors.toList());
        return state;

      }).pairInPar((seq, state) -> {

        // Compute accumulated sums of theta_j * x_j's and theta_j's
        state.sum1.add(state.sum1terms.get(0));
        for (int j = 1; j < state.sum1terms.size(); j++) {
          state.sum1.add(VectorUtils.add(state.sum1.get(j - 1), state.sum1terms.get(j), seq));
        }
        return state;

      }, (seq, state) -> {

        state.sum2.add(state.thetas.get(0));
        for (int j = 1; j < state.thetas.size(); j++) {
          state.sum2.add(FixedNumeric.using(seq).add(state.sum2.get(j - 1), state.thetas.get(j)));
        }
        return state;

      }).par((par, statePair) -> {

        // The parallel scopes above are changing the same object
        State state = statePair.getFirst();

        // Correct for ties -- does not require MPC computation
        for (int j = n - 2; j >= 0; j--) {
          if (tiedGroups.get(j).equals(tiedGroups.get(j + 1))) {
            state.sum1.set(j, state.sum1.get(j + 1));
            state.sum2.set(j, state.sum2.get(j + 1));
          }
        }

        // Sum2 is only used for division so we compute the reciprocal
        state.sum2recip = state.sum2.stream().map(AdvancedFixedNumeric.using(par)::reciprocal)
            .collect(
                Collectors.toList());
        return state;

      }).par((par, state) -> {

        // Compute ratios of sum1 and sum2
        state.ratios = IntStream.range(0, n)
            .mapToObj(j -> VectorUtils.scale(state.sum1.get(j), state.sum2recip.get(j), par))
            .collect(
                Collectors
                    .toList());
        return state;
      }).par((par, state) -> {

        // The terms in the final sum are x_j - ratio_j
        state.terms = IntStream.range(0, n)
            .mapToObj(j -> VectorUtils.sub(data.get(j).getCovariates(), state.ratios.get(j), par))
            .collect(
                Collectors.toList());
        return state;

      }).par((par, state) -> {

        // Only include terms with status = 1
        List<DRes<SInt>> status = VectorUtils
            .listBuilder(n, j -> data.get(j).getCensored());
        state.result = VectorUtils
            .listBuilder(beta.size(), i -> VectorUtils.innerProductWithBitvector(
                status,
                // The i'th entries in the terms
                VectorUtils.listBuilder(n, j -> state.terms.get(j).get(i)),
                par));
        return state;

      });
    }

    // Reused in CoxHessianInternal
    static class State implements DRes<State> {

      List<DRes<SFixed>> sum2recip;
      ArrayList<DRes<SFixed>> result;
      List<DRes<SFixed>> thetas;
      List<List<DRes<SFixed>>> sum1terms;
      List<List<DRes<SFixed>>> sum1 = new ArrayList<>();
      List<DRes<SFixed>> sum2 = new ArrayList<>();
      List<List<DRes<SFixed>>> ratios;
      List<List<DRes<SFixed>>> terms;

      @Override
      public State out() {
        return this;
      }

      ArrayList<DRes<SFixed>> getResult() {
        return result;
      }
    }
  }
}
