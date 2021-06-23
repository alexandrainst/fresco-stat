package dk.alexandra.fresco.stat.mlp.activationfunction;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ActivationFunction {
  SIGMOID, RELU;

  public static Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> get(
      ArrayList<DRes<SFixed>> x, ActivationFunction type) {
    if (type == SIGMOID) {
      return new CoordinateWiseEvaluation(Sigmoid::new, x);
    } else if (type == RELU) {
      return new CoordinateWiseEvaluation(Relu::new, x);
    } else {
      throw new IllegalArgumentException("Unknown activation function");
    }
  }

  /**
   * Compute <i>f'(x)</i> for the given activation function <i>f: R<sup>n</sup> &#8594;
   * R<sup>n</sup></i>. The function value <i>y = f(x)</i> may be used in the computation if it
   * makes the computation easier for the given function.
   */
  public static Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> getDerivative(
      ArrayList<DRes<SFixed>> x, ArrayList<DRes<SFixed>> y, ActivationFunction type) {
    if (type == SIGMOID) {
      return new CoordinateWiseEvaluation(SigmoidDerivative::new, y);
    } else if (type == RELU) {
      return new CoordinateWiseEvaluation(ReluDerivative::new, x);
    } else {
      throw new IllegalArgumentException("Unknown activation function");
    }
  }

  /**
   * Extend a function <i>f: R &#8594; R</i> to <i>g: R<sup>n</sup> &#8594; R<sup>n</sup></i> by
   * <i>g(x)<sub>i</sub> = f(x<sub>i</sub>)</i>.
   */
  private static class CoordinateWiseEvaluation implements
      Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

    private final Function<DRes<SFixed>, Computation<SFixed, ProtocolBuilderNumeric>> function;
    private final List<DRes<SFixed>> vector;

    public CoordinateWiseEvaluation(
        Function<DRes<SFixed>, Computation<SFixed, ProtocolBuilderNumeric>> function,
        ArrayList<DRes<SFixed>> vector) {
      this.function = function;
      this.vector = vector;
    }

    @Override
    public DRes<ArrayList<DRes<SFixed>>> buildComputation(
        ProtocolBuilderNumeric root) {
      return root.par(par -> DRes
          .of(vector.stream().map(xi -> function.apply(xi).buildComputation(par)).collect(
              Collectors.toCollection(ArrayList::new))));
    }
  }
}
