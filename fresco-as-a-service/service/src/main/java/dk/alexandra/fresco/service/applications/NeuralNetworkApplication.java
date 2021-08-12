package dk.alexandra.fresco.service.applications;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.service.messages.Common;
import dk.alexandra.fresco.service.messages.Common.Vector;
import dk.alexandra.fresco.service.messages.MPCInputOuterClass.MPCVector;
import dk.alexandra.fresco.service.messages.NeuralNetwork.NeuralNetworkInput;
import dk.alexandra.fresco.service.messages.NeuralNetwork.NeuralNetworkParameters;
import dk.alexandra.fresco.stat.mlp.Layer;
import dk.alexandra.fresco.stat.mlp.MLP;
import dk.alexandra.fresco.stat.mlp.activationfunction.ActivationFunction;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NeuralNetworkApplication implements
    Application<List<Pair<dk.alexandra.fresco.service.messages.Common.Matrix, Vector>>, ProtocolBuilderNumeric> {

  private final List<MPCVector> data;
  private final List<MPCVector> labels;
  private final NeuralNetworkParameters mlp;
  private final int epochs;
  private final double learningRate;

  public NeuralNetworkApplication(NeuralNetworkInput neuralNetworkInput) {
    this(neuralNetworkInput.getData().getXList(), neuralNetworkInput.getData().getYList(),
        neuralNetworkInput.getNetwork(),
        neuralNetworkInput.getEpochs(), neuralNetworkInput.getLearningrate());
  }

  private NeuralNetworkApplication(List<MPCVector> data,
      List<MPCVector> labels, NeuralNetworkParameters mlp, int epochs,
      double learningRate) {
    this.data = data;
    this.labels = labels;
    this.mlp = mlp;
    this.epochs = epochs;
    this.learningRate = learningRate;
  }

  @Override
  public DRes<List<Pair<Common.Matrix, Common.Vector>>> buildComputation(
      ProtocolBuilderNumeric builder) {

    return builder
        .par(par -> {

          List<Pair<Matrix<DRes<SFixed>>, ArrayList<DRes<SFixed>>>> layers = new ArrayList<>();
          for (int i = 0; i < mlp.getWeightsCount(); i++) {
            layers.add(new Pair<>(MatrixUtils.transpose(Utils.inputMatrix(mlp.getWeights(i), par)),
                Utils.inputList(mlp.getBiases(i), par)));
          }

          // Dataset may be distributed
          List<ArrayList<DRes<SFixed>>> x = data.stream().map(
              row -> row.getValuesList().stream()
                  .map(entry -> Utils.input(entry, par))
                  .collect(Collectors.toCollection(ArrayList::new)))
              .collect(Collectors.toList());

          List<ArrayList<DRes<SFixed>>> y = labels.stream().map(yi -> Utils.inputList(yi, par))
              .collect(
                  Collectors.toList());

          return Pair.lazy(layers, new Pair<>(x, y));
        }).seq((seq, layersAndData) -> {

          // TODO: Allow other activation functions
          List<Layer> layers = layersAndData.getFirst().stream()
              .map(weightAndBias -> new Layer(weightAndBias.getFirst(),
                  weightAndBias.getSecond(),
                  ActivationFunction.SIGMOID)).collect(
                  Collectors.toList());
          MLP nn = new MLP(layers);

          return nn.fit(layersAndData.getSecond().getFirst(), layersAndData.getSecond().getSecond(),
              epochs, learningRate).buildComputation(seq);
        }).par((par, nn) -> {
          FixedLinearAlgebra linearAlgebra = FixedLinearAlgebra.using(par);

          List<Pair<DRes<Matrix<DRes<BigDecimal>>>, DRes<ArrayList<DRes<BigDecimal>>>>> layers = new ArrayList<>();

          for (int l = 0; l < mlp.getWeightsCount(); l++) {
            layers.add(new Pair<>(linearAlgebra.openMatrix(DRes.of(nn.getLayer(l).getWeights())),
                linearAlgebra.openArrayList(DRes.of(nn.getLayer(l).getBias()))));
          }

          return DRes.of(layers);
        }).seq((seq, openLayers) -> {
          List<Pair<Common.Matrix, Vector>> layers = openLayers.stream().map(pair -> {
            Common.Matrix weight = Utils.convert(MatrixUtils.map(pair.getFirst().out(), DRes::out));
            ArrayList<BigDecimal> bias = pair.getSecond().out().stream().map(DRes::out)
                .collect(Collectors.toCollection(ArrayList::new));
            Common.Vector biasVector = Utils.convert(bias);
            return new Pair<>(weight, biasVector);
          }).collect(Collectors.toList());
          return DRes.of(layers);
        });
  }
}
