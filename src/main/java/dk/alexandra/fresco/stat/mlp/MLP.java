package dk.alexandra.fresco.stat.mlp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.Triple;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class represents layered neural networks. Computing a networks output on a sample is done
 * using the {@link #apply(ArrayList)} method. Prediction is done using {@link Predict}. Training a
 * network is done using the {@link #fit(List, List, int, double)} method. Instances of this class
 * are immutable.
 */
public class MLP {

  private final List<Layer> layers;

  public MLP(List<Layer> layers) {
    this.layers = layers;
  }

  public Layer getLayer(int i) {
    return layers.get(i);
  }

  /**
   * Given an input sample, this method computes the outputs of all layers and returns them as a
   * list. The first output is equal to the input sample and the last is the output of the network.
   */
  Computation<List<ForwardPropagationOutput>, ProtocolBuilderNumeric> forwardPropagation(
      ArrayList<DRes<SFixed>> input) {
    return builder -> builder.seq(seq -> {
      LinkedList<DRes<ForwardPropagationOutput>> outputs = new LinkedList<>();
      outputs.add(new ForwardPropagationOutput(null, DRes.of(input)));
      return Pair.lazy(0, outputs);
    }).whileLoop(state -> state.getFirst() < layers.size(), (seq, state) -> {
      LinkedList<DRes<ForwardPropagationOutput>> outputs = new LinkedList<>(state.getSecond());
      outputs.add(seq.seq(layers.get(state.getFirst())
          .forwardPropagation(state.getSecond().getLast().out().getAfterActivation().out())));
      return Pair.lazy(state.getFirst() + 1, outputs);
    }).seq((seq, state) -> DRes
        .of(state.getSecond().stream().map(DRes::out).collect(Collectors.toList())));
  }

  /**
   * Compute a neural network with new weights based on this network and the output of a
   * backpropagation. The deltas are the outputs of {@link Layer#backPropagation(ForwardPropagationOutput,
   * BackPropagationOutput)} on all layers.
   */
  private Computation<MLP, ProtocolBuilderNumeric> update(
      List<ArrayList<DRes<SFixed>>> deltas, List<ForwardPropagationOutput> outputs,
      double learningRate) {
    return root -> root.par(par -> {
      List<DRes<Layer>> newLayers = new ArrayList<>();
      for (int i = 0; i < layers.size(); i++) {
        Layer layer = layers.get(i);
        DRes<Layer> newLayer = layer
            .update(deltas.get(i), outputs.get(i).getAfterActivation().out(), learningRate)
            .buildComputation(par);
        newLayers.add(newLayer);
      }
      return DRes.of(newLayers);
    }).seq((seq, newLayers) -> DRes
        .of(new MLP(newLayers.stream().map(DRes::out).collect(Collectors.toList()))));
  }

  /**
   * Train this neural network using single step training (batch size = 1) and return a new neural
   * network with the updated weights.
   *
   * @param data         The training data.
   * @param labels       The expected outputs.
   * @param epochs       The number of epochs to run the training. One epoch is an iteration over
   *                     all training data.
   * @param learningRate The learning rate.
   * @return A new neural network with weights updated from this network.
   */
  public Computation<MLP, ProtocolBuilderNumeric> fit(
      List<ArrayList<DRes<SFixed>>> data, List<ArrayList<DRes<SFixed>>> labels, int epochs,
      double learningRate) {

    int outputSize = labels.get(0).size();

    return builder -> builder.seq(seq -> Pair.lazy(0, DRes.of(this)))
        .whileLoop(state -> state.getFirst() < epochs,
            (seq, state) -> {
              DRes<MLP> nn = seq.seq(sub -> Pair.lazy(0, state.getSecond()))
                  .whileLoop(substate -> substate.getFirst() < data.size(),
                      (sub, epochLoopState) -> {

                        // Log the progress
                        int percentage = 100 * state.getFirst() / epochs + 100 * epochLoopState.getFirst() / (epochs * data.size());
                        Logger.getLogger("dk.alexandra.fresco.stat").info("Fitting model: " + percentage + "% (epoch " + state.getFirst()
                            + "/" + epochs + ", sample " + epochLoopState.getFirst() + "/" + data.size() + ")");

                        // Loop over all samples
                        return sub.seq(epochLoopState.getSecond().out()
                            .forwardPropagation(data.get(epochLoopState.getFirst())))
                            .par((subBuilder, outputs) -> {
                              FixedNumeric fixedNumeric = FixedNumeric.using(subBuilder);

                              // Compute the error, which in the case is expectedOutput - actualOutput.
                              ArrayList<DRes<SFixed>> error = VectorUtils
                                  .listBuilder(outputSize, i ->
                                      fixedNumeric.sub(labels.get(epochLoopState.getFirst()).get(i),
                                          outputs.get(outputs.size() - 1).getAfterActivation().out()
                                              .get(i))
                                  );
                              return Pair.lazy(outputs, error);
                            }).seq((subBuilder, outputsError) -> Pair.lazy(outputsError.getFirst(),

                                // Back propagation
                                subBuilder.seq(epochLoopState.getSecond().out()
                                    .backPropagation(outputsError.getFirst(),
                                        outputsError.getSecond())))
                            )

                            // Update weights and return new network with these weights
                            .seq((subBuilder, outputsDeltas) -> Pair
                                .lazy(epochLoopState.getFirst() + 1,
                                    epochLoopState.getSecond().out()
                                        .update(outputsDeltas.getSecond().out(),
                                            outputsDeltas.getFirst(), learningRate)
                                        .buildComputation(subBuilder)));

                      }).seq((subBuilder, substate) -> substate.getSecond());

              // Iterate epoch count and continue to next epoch
              return Pair.lazy(state.getFirst() + 1, nn);

            }).seq((seq, state) -> state.getSecond());
  }

  /**
   * Given a list of results from a forward propagation and the error (difference between the expected
   * output and the actual output), this computes the delta values for each layer using the backpropagation
   * algorithm.
   */
  Computation<List<ArrayList<DRes<SFixed>>>, ProtocolBuilderNumeric> backPropagation(
      List<ForwardPropagationOutput> outputs, ArrayList<DRes<SFixed>> error) {
    return builder -> builder.seq(seq -> {
      LinkedList<DRes<ArrayList<DRes<SFixed>>>> deltas = new LinkedList<>();
      BackPropagationOutput output = new BackPropagationOutput(DRes.of(error), null);
      return DRes.of(new Triple<>(layers.size() - 1, deltas, output));
    }).whileLoop(state -> state.getFirst() >= 0, (root, state) -> root.seq(seq ->
        layers.get(state.getFirst())
            .backPropagation(outputs.get(state.getFirst() + 1), state.getThird())
            .buildComputation(seq))
        .seq((seq, backPropagationOutput) -> {
          LinkedList<DRes<ArrayList<DRes<SFixed>>>> deltas = new LinkedList<>(state.getSecond());
          deltas.addFirst(backPropagationOutput.getDelta());
          return DRes.of(new Triple<>(state.getFirst() - 1, deltas, backPropagationOutput));
        })
    ).seq((seq, state) -> DRes
        .of(state.getSecond().stream().map(DRes::out).collect(Collectors.toList())));
  }

  /** Apply this neural network in an input vector. */
  public Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> apply(
      ArrayList<DRes<SFixed>> input) {
    return builder -> builder.seq(forwardPropagation(input))
        .seq((seq, outputs) -> outputs.get(outputs.size() - 1).getAfterActivation());
  }

}
