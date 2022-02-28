package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

/**
 * Compute <i>x, x<sup>2</sup>, x<sup>3</sup>, ..., x<sup>n</sup></i> for a secret value <i>x</i>.
 */
public class PowerList implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<SInt> x;
  private final int n;
  private final int iterations;

  /**
   * Create a new PowerList computation.
   *
   * @param x The value
   * @param n The number of powers to compute.
   */
  public PowerList(DRes<SInt> x, int n) {
    this.x = x;
    this.n = n;
    this.iterations = Integer.highestOneBit(n) + 1;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> new State(List.of(x), 1)).whileLoop(state -> state.i < iterations,
        (seq, state) -> seq.par(par -> {
          DRes<SInt> top = state.powers.get(state.powers.size() - 1);
          List<DRes<SInt>> newPowers = new ArrayList<>(state.powers);
          for (DRes<SInt> power : state.powers) {
            if (newPowers.size() == n) {
              continue;
            }
            newPowers.add(par.numeric().mult(power, top));
          }
          return new State(newPowers, state.i + 1);
        })).seq((seq, state) -> DRes.of(state.powers));
  }

  static class State implements DRes<State> {

    final List<DRes<SInt>> powers;
    final int i;

    State(List<DRes<SInt>> powers, int i) {
      this.powers = powers;
      this.i = i;
    }

    @Override
    public State out() {
      return this;
    }
  }
}
