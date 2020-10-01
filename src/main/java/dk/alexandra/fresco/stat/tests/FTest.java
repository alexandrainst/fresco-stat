package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.helpers.USS;
import java.util.ArrayList;
import java.util.List;

/**
 * Compute the F-test for equal mean (one-way-anova) for the given data sets.
 */
public class FTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<List<DRes<SFixed>>> observed;

  public FTest(List<List<DRes<SFixed>>> observed) {
    this.observed = observed;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    int n = observed.stream().map(List::size).mapToInt(Integer::intValue).sum();
    int f1 = n - observed.size(); // degrees of freedom

    return builder.par(par -> {

      // Keep helper values throughout the computation
      State state = new State();

      for (List<DRes<SFixed>> sample : observed) {
        state.sums.add(AdvancedFixedNumeric.using(par).sum(sample));
        state.uss.add(new USS(sample).buildComputation(par));
      }

      return () -> state;
    }).par((par, state) -> {

      for (int i = 0; i < observed.size(); i++) {
        int finalI = i;
        par.seq(seq -> {

          FixedNumeric numeric = FixedNumeric.using(seq);
          DRes<SFixed> ratio = numeric
              .mult(state.sums.get(finalI), state.sums.get(finalI));
          ratio = numeric.div(ratio, observed.get(finalI).size());
          DRes<SFixed> ssd = numeric.sub(state.uss.get(finalI), ratio);

          state.ssds.add(ssd);
          state.ratios.add(ratio);

          return null;
        });
      }

      return () -> state;
    }).par((par, state) -> {
      // Some values are no longer needed
      state.uss = null;

      state.ssd1 = AdvancedFixedNumeric.using(par).sum(state.ssds);
      state.sumOfRatios = AdvancedFixedNumeric.using(par).sum(state.ratios);
      state.sum = AdvancedFixedNumeric.using(par).sum(state.sums);

      return () -> state;
    }).par((par, state) -> {
      // Some values are no longer needed
      state.ssds = null;
      state.sums = null;
      state.ratios = null;

      state.s2 = par.seq(seq -> {
        DRes<SFixed> ssd2 = FixedNumeric.using(seq).mult(state.sum, state.sum);
        ssd2 = FixedNumeric.using(seq).div(ssd2, n);
        ssd2 = FixedNumeric.using(seq).sub(state.sumOfRatios, ssd2);
        return FixedNumeric.using(seq).div(ssd2, observed.size() - 1);
      });
      state.s1 = par.seq(subSeq -> FixedNumeric.using(subSeq).div(state.ssd1, f1));

      return () -> state;
    }).seq((seq, state) -> FixedNumeric.using(seq).div(state.s2, state.s1));
  }

  /**
   * Data class to keep values used through out the computations
   */
  private static class State {

    private List<DRes<SFixed>> sums, uss, ssds, ratios;
    private DRes<SFixed> ssd1, sumOfRatios, sum, s1, s2;

    public State() {
      sums = new ArrayList<>();
      uss = new ArrayList<>();
      ssds = new ArrayList<>();
      ratios = new ArrayList<>();
    }
  }
}
