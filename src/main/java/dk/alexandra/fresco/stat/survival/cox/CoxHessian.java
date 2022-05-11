package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.OuterProductWithItself;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.survival.cox.CoxGradient.CoxGradientInternal;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoxHessian implements Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<SurvivalEntry> data;
  private final List<BigInteger> tiedGroups;
  private final List<DRes<SFixed>> beta;

  public CoxHessian(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
      List<DRes<SFixed>> beta) {
    this.data = data;
    this.tiedGroups = tiedGroups;
    this.beta = beta;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(new CoxHessianInternal(data, tiedGroups, beta))
        .seq((seq, state) -> state.out().hessian);
  }

  static class CoxHessianInternal implements
      Computation<CoxHessianInternal.State, ProtocolBuilderNumeric> {

    private final List<SurvivalEntry> data;
    private final List<DRes<SFixed>> beta;
    private final List<BigInteger> tiedGroups;
    private final CoxGradientInternal.State fromGradient;

    CoxHessianInternal(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
        List<DRes<SFixed>> beta) {
      this(data, tiedGroups, beta, null);
    }

    CoxHessianInternal(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
        List<DRes<SFixed>> beta, CoxGradientInternal.State fromGradient) {
      this.data = data;
      this.tiedGroups = tiedGroups;
      this.beta = beta;

      // If the gradient was computed before this, we reuse some precomputed values
      this.fromGradient = fromGradient;
    }

    @Override
    public DRes<State> buildComputation(ProtocolBuilderNumeric builder) {

      // Number of data entries
      int n = data.size();

      return builder.par(par -> {
        State state = new State();

        // Is used in Cox Prtial Likelihood Ratio Test
        if (fromGradient != null) {
          state.innerProducts = fromGradient.innerProducts;
        } else {
          state.innerProducts = data.stream().map(datum -> par.seq(seq -> {
            AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(seq);
            return advancedFixedNumeric.innerProduct(beta, datum.getCovariates());
          })).collect(Collectors.toList());
        }

        return state;
      }).par((par, state) -> {

        state.thetas = Objects.nonNull(fromGradient) ? fromGradient.thetas
            : state.innerProducts.stream().map(ip -> par.seq(seq -> {
              AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(seq);
              return advancedFixedNumeric
                  .exp(ip);
            })).collect(Collectors.toList());

        return state;

      }).par((par, state) -> {

        // Compute theta_j * x_j
        // Only used to compute sum1, so in case we have fromGradient, we get sum1 from there
        state.sum1terms = IntStream.range(0, n)
            .mapToObj(
                j -> VectorUtils.scale(data.get(j).getCovariates(), state.thetas.get(j), par))
            .collect(
                Collectors.toList());

        return state;

      }).par((par, state) -> {

        // Compute x_j * x_j^T
        state.observationsSquared = data.stream().map(SurvivalEntry::getCovariates)
            .map(OuterProductWithItself::new)
            .map(outerProduct -> outerProduct.buildComputation(par))
            .collect(
                Collectors.toCollection(ArrayList::new));
        return state;

      }).par((par, state) -> {

        // Compute theta_j x_j * x_j^T
        state.sum3terms = IntStream.range(0, n)
            .mapToObj(j -> FixedLinearAlgebra.using(par).scale(state.thetas.get(j),
                state.observationsSquared.get(j))).collect(Collectors.toList());
        return DRes.of(state);

      }).par((par, state) -> {

        // Compute accumulated sums of theta_j * x_j's, theta_j x_j * x_j and theta_j's
        if (Objects.isNull(fromGradient)) {

          par.seq(seq -> {
            state.sum1.add(state.sum1terms.get(0));
            for (int j = 1; j < state.sum1terms.size(); j++) {
              state.sum1.add(VectorUtils.add(state.sum1.get(j - 1), state.sum1terms.get(j), seq));
            }
            return state;
          });

          par.seq(seq -> {
            state.sum2.add(state.thetas.get(0));
            for (int j = 1; j < state.thetas.size(); j++) {
              state.sum2
                  .add(FixedNumeric.using(seq).add(state.sum2.get(j - 1), state.thetas.get(j)));
            }
            return state;
          });

        } else {

          state.sum1 = fromGradient.sum1;
          state.sum2 = fromGradient.sum2;

        }

        par.seq(seq -> {
          state.sum3.add(state.sum3terms.get(0));
          for (int j = 1; j < state.sum3terms.size(); j++) {
            state.sum3.add(
                FixedLinearAlgebra.using(seq).add(state.sum3.get(j - 1), state.sum3terms.get(j)));
          }
          return state;
        });

        return state;
      }).par((par, state) -> {

        // Correct for ties -- done in the clear
        for (int j = n - 2; j >= 0; j--) {
          if (tiedGroups.get(j).equals(tiedGroups.get(j + 1))) {
            if (Objects.isNull(fromGradient)) {
              state.sum1.set(j, state.sum1.get(j + 1));
              state.sum2.set(j, state.sum2.get(j + 1));
            }
            state.sum3.set(j, state.sum3.get(j + 1));
          }
        }

        // The numerator of the second term is the outer product of sum1 with if self
        state.sum1squared = state.sum1.stream().map(OuterProductWithItself::new)
            .map(outerProductWithItself -> outerProductWithItself.buildComputation(par)).collect(
                Collectors.toCollection(ArrayList::new));

        // Sum2 is only used for division so we compute the reciprocal
        state.sum2recip = Objects.nonNull(fromGradient) ? fromGradient.sum2recip :
            state.sum2.stream().map(AdvancedFixedNumeric.using(par)::reciprocal)
                .collect(
                    Collectors.toList());

        return state;
      }).par((par, state) -> {

        // Compute the rations of sum1 and sum2
        state.ratios = IntStream.range(0, n).mapToObj(j ->
            FixedLinearAlgebra.using(par).scale(state.sum2recip.get(j), state.sum1squared.get(j)))
            .collect(
                Collectors.toList());
        return state;

      }).par((par, state) -> {

        // Compute sum3 - ratios
        state.difference = IntStream.range(0, n)
            .mapToObj(
                j -> FixedLinearAlgebra.using(par).sub(state.sum3.get(j), state.ratios.get(j)))
            .collect(
                Collectors.toList());
        return state;

      }).par((par, state) -> {

        // The final terms of the Hessian are (sum3 - rations) / sum2
        state.terms = IntStream.range(0, n)
            .mapToObj(j -> FixedLinearAlgebra.using(par).scale(state.sum2recip.get(j),
                state.difference.get(j))).collect(Collectors.toList());
        return state;

      }).par((par, state) -> {

        // Only include terms with status = 1
        List<DRes<SInt>> status = VectorUtils
            .listBuilder(n, j -> data.get(j).getCensored());

        state.hessian = par.par(sub -> {
          Matrix<DRes<SFixed>> result = MatrixUtils
              .buildMatrix(beta.size(), beta.size(),
                  (i, j) -> VectorUtils.innerProductWithBitvector(
                      status,
                      // The i'th entries in the terms
                      VectorUtils.listBuilder(n, k -> state.terms.get(k).out().getRow(i).get(j)),
                      sub));
          return DRes.of(result);
        }).par((sub, result) -> DRes
            .of(MatrixUtils.map(result, x -> FixedNumeric.using(sub).sub(0, x))));

        return state;

      });
    }

    static class State implements DRes<State> {

      public List<DRes<SFixed>> innerProducts;
      List<DRes<Matrix<DRes<SFixed>>>> difference;
      List<DRes<SFixed>> sum2recip;
      List<DRes<Matrix<DRes<SFixed>>>> sum3 = new ArrayList<>();
      List<DRes<Matrix<DRes<SFixed>>>> sum3terms;
      ArrayList<DRes<Matrix<DRes<SFixed>>>> observationsSquared;
      ArrayList<DRes<Matrix<DRes<SFixed>>>> sum1squared;
      List<DRes<SFixed>> thetas;
      List<List<DRes<SFixed>>> sum1terms;
      List<List<DRes<SFixed>>> sum1 = new ArrayList<>();
      List<DRes<SFixed>> sum2 = new ArrayList<>();
      List<DRes<Matrix<DRes<SFixed>>>> ratios;
      List<DRes<Matrix<DRes<SFixed>>>> terms;
      DRes<Matrix<DRes<SFixed>>> hessian;

      @Override
      public State out() {
        return this;
      }
    }
  }
}
