package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perform the Gram-Schmidt process on a list of linearly independent vectors. This results in a
 * list of mutually orthogonal vectors spanning the same subspace as the given vectors.
 */
public class GramSchmidt implements
    Computation<List<ArrayList<DRes<SFixed>>>, ProtocolBuilderNumeric> {

  private final List<ArrayList<DRes<SFixed>>> vectors;

  public GramSchmidt(List<ArrayList<DRes<SFixed>>> vectors) {
    this.vectors = vectors;
  }

  @Override
  public DRes<List<ArrayList<DRes<SFixed>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.seq(
        seq -> DRes.of(new State(new ArrayList<>(vectors),
            1)))
        .whileLoop(state -> state.round < vectors.size(),
            (seq, state) -> seq.par(par -> {
              AdvancedLinearAlgebra linearAlgebra = AdvancedLinearAlgebra.using(par);
              List<DRes<ArrayList<DRes<SFixed>>>> projections =
                  vectors.stream().skip(state.round)
                      .map(v -> linearAlgebra.projection(v, state.result.get(state.round - 1)))
                      .collect(Collectors.toList());
              return DRes.of(projections);
            }).par((par, projections) -> {
              for (int i = state.round; i < vectors.size(); i++) {
                state.result
                    .set(i, VectorUtils
                        .sub(state.result.get(i), projections.get(i - state.round).out(), par));
              }
              return DRes.of(new State(state.result, state.round + 1));
            })).seq((seq, state) -> DRes.of(state.result));
  }

  private static class State {

    private final List<ArrayList<DRes<SFixed>>> result;
    private final int round;

    State(List<ArrayList<DRes<SFixed>>> result, int round) {
      this.result = result;
      this.round = round;
    }
  }
}
