package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.helpers.USS;
import java.util.ArrayList;
import java.util.List;

/**
 * Compute the F-test for equal mean (one-way-anova) for the given data sets.
 */
public class FTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private final List<List<DRes<SReal>>> observed;

  public FTest(List<List<DRes<SReal>>> observed) {
    this.observed = observed;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    int n = observed.stream().map(List::size).mapToInt(Integer::intValue).sum();
    int f1 = n - observed.size(); // degrees of freedom

    return builder.par(par -> {

      // Keep helper values throughout the computation
      State state = new State();

      for (List<DRes<SReal>> sample : observed) {
        state.sums.add(par.realAdvanced().sum(sample));
        state.uss.add(new USS(sample).buildComputation(par));
      }

      return () -> state;
    }).par((par, state) -> {

      for (int i = 0; i < observed.size(); i++) {
        int finalI = i;
        par.seq(seq -> {

          DRes<SReal> ratio = seq.realNumeric()
              .mult(state.sums.get(finalI), state.sums.get(finalI));
          ratio = seq.realNumeric().div(ratio, observed.get(finalI).size());
          DRes<SReal> ssd = seq.realNumeric().sub(state.uss.get(finalI), ratio);

          state.ssds.add(ssd);
          state.ratios.add(ratio);

          return null;
        });
      }

      return () -> state;
    }).par((par, state) -> {
      // Some values are no longer needed
      state.uss = null;

      state.ssd1 = par.realAdvanced().sum(state.ssds);
      state.sumOfRatios = par.realAdvanced().sum(state.ratios);
      state.sum = par.realAdvanced().sum(state.sums);

      return () -> state;
    }).par((par, state) -> {
      // Some values are no longer needed
      state.ssds = null;
      state.sums = null;
      state.ratios = null;

      state.s2 = par.seq(seq -> {
        DRes<SReal> ssd2 = seq.realNumeric().mult(state.sum, state.sum);
        ssd2 = seq.realNumeric().div(ssd2, n);
        ssd2 = seq.realNumeric().sub(state.sumOfRatios, ssd2);
        return seq.realNumeric().div(ssd2, observed.size() - 1);
      });
      state.s1 = par.seq(subSeq -> subSeq.realNumeric().div(state.ssd1, f1));

      return () -> state;
    }).seq((seq, state) -> seq.realNumeric().div(state.s2, state.s1));
  }

  /**
   * Data class to keep values used through out the computations
   */
  private class State {

    private List<DRes<SReal>> sums, uss, ssds, ratios;
    private DRes<SReal> ssd1, sumOfRatios, sum, s1, s2;

    public State() {
      sums = new ArrayList<>();
      uss = new ArrayList<>();
      ssds = new ArrayList<>();
      ratios = new ArrayList<>();
    }
  }
}
