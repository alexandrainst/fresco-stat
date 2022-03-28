package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.OuterProductWithItself;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CoxHessianInternal implements
    Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<SurvivalEntry> data;
  private final List<DRes<SFixed>> beta;
  private final List<BigInteger> tiedGroups;
  private final CoxGradient.CoxGradientInternal.State gradientState;

  CoxHessianInternal(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
      List<DRes<SFixed>> beta, CoxGradient.CoxGradientInternal.State gradientState) {
    this.data = data;
    this.tiedGroups = tiedGroups;
    this.beta = beta;
    this.gradientState = gradientState;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {

    // Number of data entries
    int n = data.size();

    return builder.par(par -> {
      State state = new State();

      // Compute x_j * x_j^T
      state.observationsSquared = data.stream().map(SurvivalEntry::getCovariates)
          .map(OuterProductWithItself::new).map(outerProduct -> outerProduct.buildComputation(par))
          .collect(
              Collectors.toCollection(ArrayList::new));
      return state;

    }).par((par, state) -> {

      // Compute theta_j x_j * x_j^T
      state.sum3terms = IntStream.range(0, n)
          .mapToObj(j -> FixedLinearAlgebra.using(par).scale(gradientState.thetas.get(j),
              state.observationsSquared.get(j))).collect(Collectors.toList());
      return DRes.of(state);

    }).seq((seq, state) -> {

      state.sum3.add(state.sum3terms.get(0));
      for (int j = 1; j < state.sum3terms.size(); j++) {
        state.sum3.add(
            FixedLinearAlgebra.using(seq).add(state.sum3.get(j - 1), state.sum3terms.get(j)));
      }
      return state;

    }).par((par, state) -> {

      // Correct for ties -- done in the clear
      for (int j = n - 2; j >= 0; j--) {
        if (tiedGroups.get(j).equals(tiedGroups.get(j + 1))) {
          state.sum3.set(j, state.sum3.get(j + 1));
        }
      }

      // The numerator of the second term is the outer product of sum1 with if self
      state.sum1squared = gradientState.sum1.stream().map(OuterProductWithItself::new)
          .map(outerProductWithItself -> outerProductWithItself.buildComputation(par)).collect(
              Collectors.toCollection(ArrayList::new));
      return state;

    }).par((par, state) -> {
      // Compute the rations of sum1 and sum2
      state.ratios = IntStream.range(0, n).mapToObj(j ->
          FixedLinearAlgebra.using(par)
              .scale(gradientState.sum2recip.get(j), state.sum1squared.get(j)))
          .collect(
              Collectors.toList());
      return state;

    }).par((par, state) -> {

      // Compute sum3 - ratios
      state.difference = IntStream.range(0, n)
          .mapToObj(j -> FixedLinearAlgebra.using(par).sub(state.sum3.get(j),
              state.ratios.get(j)))
          .collect(
              Collectors.toList());
      return state;

    }).par((par, state) -> {

      // The final terms of the Hessian are (sum3 - rations) / sum2
      state.terms = IntStream.range(0, n)
          .mapToObj(j -> FixedLinearAlgebra.using(par).scale(gradientState.sum2recip.get(j),
              state.difference.get(j))).collect(Collectors.toList());
      return state;

    }).par((par, state) -> {

      // Only include terms with status = 1
      List<DRes<SInt>> status = VectorUtils
          .listBuilder(n, j -> data.get(j).getCensored());

      Matrix<DRes<SFixed>> result = MatrixUtils
          .buildMatrix(beta.size(), beta.size(), (i, j) -> VectorUtils.innerProductWithBitvector(
              status,
              // The i'th entries in the terms
              VectorUtils.listBuilder(n, k -> state.terms.get(k).out().getRow(i).get(j)),
              par));

      return DRes.of(result);
    }).par(
        (par, result) -> DRes.of(MatrixUtils.map(result, x -> FixedNumeric.using(par).sub(0, x))));

  }

  private static class State implements DRes<State> {

    private List<DRes<Matrix<DRes<SFixed>>>> ratios;
    private List<DRes<Matrix<DRes<SFixed>>>> difference;
    private final List<DRes<Matrix<DRes<SFixed>>>> sum3 = new ArrayList<>();
    private List<DRes<Matrix<DRes<SFixed>>>> sum3terms;
    private ArrayList<DRes<Matrix<DRes<SFixed>>>> observationsSquared;
    private ArrayList<DRes<Matrix<DRes<SFixed>>>> sum1squared;
    private List<DRes<Matrix<DRes<SFixed>>>> terms;

    @Override
    public State out() {
      return this;
    }
  }
}
