package dk.alexandra.fresco.stat.mlp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.AffineMap;
import dk.alexandra.fresco.stat.mlp.activationfunction.ActivationFunction;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.TransposedMatrixAction;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Instances of this class represents fully connected layers in a neural network. Instances are
 * immutable.
 */
public class Layer implements DRes<Layer> {

  private final Matrix<DRes<SFixed>> weights;
  private final ArrayList<DRes<SFixed>> bias;
  private final ActivationFunction activationFunction;

  /** Create a new fully connected layer with the given secret weights, biases and activation function. */
  public Layer(Matrix<DRes<SFixed>> weights, ArrayList<DRes<SFixed>> bias,
      ActivationFunction activationFunction) {
    this.weights = weights;
    this.bias = bias;
    this.activationFunction = activationFunction;
  }

  /** Create a new fully connected layer with the given open weights, biases and activation function. */
  public Layer(double[][] weights, double[] bias, ProtocolBuilderNumeric inputBuilder,
      ActivationFunction activationFunction) {
    this(MatrixUtils.buildMatrix(weights.length, weights[0].length,
        (i, j) -> FixedNumeric.using(inputBuilder).known(weights[i][j])),
        VectorUtils
            .listBuilder(weights.length, i -> FixedNumeric.using(inputBuilder).known(bias[i])),
        activationFunction);
  }

  /** Create a new fully connected layer with the given weights and biases and using a sigmoid activation function. */
  public Layer(double[][] weights, double[] bias, ProtocolBuilderNumeric inputBuilder) {
    this(weights, bias, inputBuilder, ActivationFunction.SIGMOID);
  }

  /**
   * Create a new layer with random weights (bias is zero and weights are distributed as a standard
   * Gaussian distribution divided by the number of neurons.
   */
  public Layer(int in, int out, Random prng, ProtocolBuilderNumeric inputBuilder,
      ActivationFunction activationFunction) {
    this(MatrixUtils.buildMatrix(out, in,
        (i, j) -> FixedNumeric.using(inputBuilder).known(prng.nextGaussian() / out)),
        VectorUtils
            .listBuilder(out,
                i -> FixedNumeric.using(inputBuilder).known(0.0)),
        activationFunction);
  }

  public Layer(int in, int out, Random prng, ProtocolBuilderNumeric inputBuilder) {
    this(in, out, prng, inputBuilder, ActivationFunction.SIGMOID);
  }

  public Matrix<DRes<SFixed>> getWeights() {
    return weights;
  }

  public ArrayList<DRes<SFixed>> getBias() {
    return bias;
  }

  /**
   * Apply this layer on an input and return the output.
   */
  Computation<ForwardPropagationOutput, ProtocolBuilderNumeric> forwardPropagation(
      ArrayList<DRes<SFixed>> input) {
    return root -> root.seq(new AffineMap(this, input)).par(
        (par, beforeActivation) -> new ForwardPropagationOutput(DRes.of(beforeActivation),
            ActivationFunction.get(beforeActivation, activationFunction).buildComputation(par)));
  }

  /**
   * Given the output of a previous forward propagation and the output of the backpropagation of the
   * previous layer, this method computes the {@link BackPropagationOutput} to be passed on to the
   * next layers. The {@link BackPropagationOutput} computed by this method is used in to update weights
   * after the backpropagation has finished (see {@link Layer#update(ArrayList, ArrayList, double)}.
   */
  Computation<BackPropagationOutput, ProtocolBuilderNumeric> backPropagation(
      ForwardPropagationOutput output, BackPropagationOutput previous) {
    return root -> root.par(par -> ActivationFunction
        .getDerivative(output.getBeforeActivation().out(), output.getAfterActivation().out(), activationFunction)
        .buildComputation(par))
        .par((par, xPrime) -> DRes
            .of(IntStream.range(0, xPrime.size()).mapToObj(i -> FixedNumeric.using(par).mult(
                previous.getError().out().get(i), xPrime.get(i)))
                .collect(Collectors.toCollection(ArrayList::new)))
        ).seq((seq, delta) -> {
          DRes<ArrayList<DRes<SFixed>>> newError = new TransposedMatrixAction(weights, delta)
              .buildComputation(seq);
          return new BackPropagationOutput(newError, DRes.of(delta));
        });
  }

  /**
   * Compute a new layer after backpropagation. The delta parameter are retrieved from when this
   * layer computed the {@link #backPropagation(ForwardPropagationOutput, BackPropagationOutput)}
   * and the input is the input used for the corresponding forward propagation.
   */
  Computation<Layer, ProtocolBuilderNumeric> update(ArrayList<DRes<SFixed>> delta,
      ArrayList<DRes<SFixed>> input, double learningRate) {
    return root -> root.par(par -> {
      Matrix<DRes<SFixed>> newWeights = MatrixUtils
          .buildMatrix(weights.getHeight(), weights.getWidth(), (i, j) -> par.seq(seq -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(seq);
            return fixedNumeric
                .add(weights.getRow(i).get(j), fixedNumeric.mult(delta.get(i), input.get(j)));
          }));
      ArrayList<DRes<SFixed>> newBias = VectorUtils.listBuilder(bias.size(), i -> par.seq(seq -> {
        FixedNumeric fixedNumeric = FixedNumeric.using(seq);
        return fixedNumeric
            .add(bias.get(i), fixedNumeric.mult(learningRate, delta.get(i)));
      }));
      return new Layer(newWeights, newBias, activationFunction);
    });
  }

  @Override
  public Layer out() {
    return this;
  }

}
