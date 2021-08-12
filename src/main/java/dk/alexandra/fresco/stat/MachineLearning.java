package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.mlp.MLP;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntToDoubleFunction;

/** This computation library contains various functions for machine learning. */
public interface MachineLearning {

  static MachineLearning using(ProtocolBuilderNumeric builder) {
    return new DefaultMachineLearning(builder);
  }

  /**
   * Estimate the parameters of a logistic model using gradient descent.
   *
   * @param data     The data represented as a matrix with entry as rows.
   * @param expected The expected outcome for each entry represented as a list. Each entry should be
   *                 either 0 or 1.
   * @param beta     The initial guess for the parameters of the model with the first being the
   *                 constant term.
   * @param rate     The learning rate used by the gradient descent algorithm as a function of the
   *                 iteration number.
   * @param epochs   The number of iterations.
   * @return An approximation of the parameters of a logistic model fitting the given data.
   */
  DRes<ArrayList<DRes<SFixed>>> logisticRegression(Matrix<DRes<SFixed>> data,
      ArrayList<DRes<SFixed>> expected, double[] beta,
      IntToDoubleFunction rate, int epochs);

  /**
   * Fit the given multilayer perceptron to a dataset using back propagation.
   *
   * @param network The mlp to fit.
   * @param data The dataset to use as input.
   * @param labels The expected outputs.
   * @param epochs The number of epochs, ie. iterations through the entire dataset.
   * @param learningRate The learning rate.
   * @return A new MLP with updated weights.
   */
  DRes<MLP> fit(MLP network, List<ArrayList<DRes<SFixed>>> data, List<ArrayList<DRes<SFixed>>> labels, int epochs, double learningRate);

  /**
   * Assuming that the given MLP has <i>n</i> output neurons, this function applies the
   * network to the given input and finds the index of the output <i>i</i> with <i>0 &leq; i &lt; n</i>
   * containing the largest number.
   *
   * @param network The multi-layer perceptron to use for the prediction.
   * @param input The input.
   * @return The index of the largest element in the output vector.
   */
  DRes<SInt> predict(MLP network, ArrayList<DRes<SFixed>> input);


}
