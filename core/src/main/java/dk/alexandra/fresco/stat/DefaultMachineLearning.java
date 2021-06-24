package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.mlp.MLP;
import dk.alexandra.fresco.stat.mlp.Predict;
import dk.alexandra.fresco.stat.regression.logistic.LogisticRegression;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

public class DefaultMachineLearning implements MachineLearning {

  private final ProtocolBuilderNumeric builder;

  DefaultMachineLearning(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> logisticRegression(Matrix<DRes<SFixed>> data,
      ArrayList<DRes<SFixed>> expected, double[] beta, IntToDoubleFunction rate, int epochs) {
    return new LogisticRegression(data, expected, beta, rate, epochs).buildComputation(builder);
  }

  @Override
  public DRes<MLP> fit(MLP network, List<ArrayList<DRes<SFixed>>> data,
      List<ArrayList<DRes<SFixed>>> labels, int epochs, double learningRate) {
    return network.fit(data, labels, epochs, learningRate).buildComputation(builder);
  }

  @Override
  public DRes<SInt> predict(MLP network, ArrayList<DRes<SFixed>> input) {
    return new Predict(network, input).buildComputation(builder);
  }

}
