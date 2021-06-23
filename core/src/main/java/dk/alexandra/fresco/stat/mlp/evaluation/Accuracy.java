package dk.alexandra.fresco.stat.mlp.evaluation;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.mlp.MLP;
import dk.alexandra.fresco.stat.mlp.Predict;
import java.util.ArrayList;
import java.util.List;

/**
 * Return the number of correct predictions in the given data set. The output of the given neural
 * network is computed as the entry in the output vector with the highest value.
 */
public class Accuracy implements Computation<SInt, ProtocolBuilderNumeric> {

  private final MLP neuralNetwork;
  private final List<ArrayList<DRes<SFixed>>> data;
  private final List<DRes<SInt>> labels;

  public Accuracy(MLP neuralNetwork, List<ArrayList<DRes<SFixed>>> data,
      ArrayList<DRes<SInt>> labels) {
    this.neuralNetwork = neuralNetwork;
    this.data = data;
    this.labels = labels;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      ArrayList<DRes<SInt>> outputs = new ArrayList<>();
      for (ArrayList<DRes<SFixed>> entry : data) {
        outputs.add(new Predict(neuralNetwork, entry).buildComputation(par));
      }
      return DRes.of(outputs);
    }).par((par, outputs) -> {
      List<DRes<SInt>> indicators = new ArrayList<>();
      Comparison comparison = Comparison.using(par);
      for (int i = 0; i < data.size(); i++) {
        indicators.add(comparison.equals(outputs.get(i), labels.get(i)));
      }
      return DRes.of(indicators);
    }).seq((seq, indicators) -> AdvancedNumeric.using(seq).sum(indicators));
  }
}
