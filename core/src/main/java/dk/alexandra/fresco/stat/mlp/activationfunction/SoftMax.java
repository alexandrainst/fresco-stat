package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SoftMax implements Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final ArrayList<DRes<SFixed>> x;

  public SoftMax(ArrayList<DRes<SFixed>> x) {
    this.x = x;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(
      ProtocolBuilderNumeric root) {
    return root.par(par ->
        DRes.of(x.stream().map(xi -> AdvancedFixedNumeric.using(par).exp(xi))
            .collect(Collectors.toList()))
    ).seq((seq, exps) ->
        Pair.lazy(exps,
            AdvancedFixedNumeric.using(seq).reciprocal(AdvancedFixedNumeric.using(seq).sum(exps)))
    ).par((par, expsAndReciprocalSum) ->
        DRes.of(expsAndReciprocalSum.getFirst().stream()
            .map(xi -> FixedNumeric.using(par).mult(xi, expsAndReciprocalSum.getSecond())).collect(
                Collectors.toCollection(ArrayList::new)))
    );
  }
}
