package dk.alexandra.fresco.stat.mlp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MaxList;
import java.util.ArrayList;

public class Predict implements Computation<SInt, ProtocolBuilderNumeric> {

  private final MLP neuralNetwork;
  private final ArrayList<DRes<SFixed>> input;

  public Predict(MLP neuralNetwork, ArrayList<DRes<SFixed>> input) {
    this.neuralNetwork = neuralNetwork;
    this.input = input;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(neuralNetwork.apply(input)).seq((seq, output) ->
        seq.seq(MaxList.withSFixed(output))
    ).seq((seq, max) -> max.getSecond());
  }
}
