package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GramSchmidt implements Computation<List<List<DRes<SFixed>>>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SFixed>>> vectors;

  public GramSchmidt(List<List<DRes<SFixed>>> vectors) {
    this.vectors = vectors;
  }

  @Override
  public DRes<List<List<DRes<SFixed>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.seq(
        seq -> () -> new State(new ArrayList<>(vectors),
            1))
        .whileLoop(state -> state.round < vectors.size(),
            (seq, state) -> seq.par(par -> {
              List<DRes<List<DRes<SFixed>>>> projections =
                  vectors.stream().skip(state.round)
                      .map(v -> new Projection(v, state.result.get(state.round - 1))
                          .buildComputation(par)).collect(Collectors.toList());
              return () -> projections;
            }).par((par, projections) -> {
              for (int i = state.round; i < vectors.size(); i++) {
                state.result
                    .set(i, VectorUtils
                        .sub(state.result.get(i), projections.get(i - state.round).out(), par));
              }
              return () -> new State(state.result, state.round + 1);
            })).seq((seq, state) -> () -> state.result);
  }

  private static class State {

    private final ArrayList<List<DRes<SFixed>>> result;
    private final int round;

    State(ArrayList<List<DRes<SFixed>>> result, int round) {
      this.result = result;
      this.round = round;
    }
  }
}
