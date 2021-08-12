package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.mlp.Layer;
import java.util.ArrayList;

/** Apply an affine map to a vector */
public class AffineMap implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> weights;
  private final ArrayList<DRes<SFixed>> bias;
  private final ArrayList<DRes<SFixed>> input;

  public AffineMap(Matrix<DRes<SFixed>> weights, ArrayList<DRes<SFixed>> bias, ArrayList<DRes<SFixed>> input) {
    this.weights = weights;
    this.bias = bias;
    this.input = input;
  }

  public AffineMap(Layer layer, ArrayList<DRes<SFixed>> input) {
    this(layer.getWeights(), layer.getBias(), input);
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(seq -> {
      FixedLinearAlgebra linearAlgebra = FixedLinearAlgebra.using(seq);
      return linearAlgebra.vectorMult(DRes.of(weights), DRes.of(input));
    }).par((par, activated) -> {
      ArrayList<DRes<SFixed>> output = new ArrayList<>();
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      for (int i = 0; i < activated.size(); i++) {
        output.add(fixedNumeric.add(activated.get(i), bias.get(i)));
      }
      return DRes.of(output);
    });
  }

}
