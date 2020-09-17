package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.ArrayList;
import java.util.List;

public class RealUtils {

  /**
   * Compute the product of all elements in the list.
   *
   * @param input   The list of factors.
   * @param builder The builder to use.
   * @return
   */
  public static DRes<SReal> product(List<DRes<SReal>> input, ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> () -> input)
        .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
          List<DRes<SReal>> out = new ArrayList<>();
          DRes<SReal> left = null;
          for (DRes<SReal> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(parallel.realNumeric().mult(left, input1));
              left = null;
            }
          }
          if (left != null) {
            out.add(left);
          }
          return () -> out;
        })).seq((r3, currentInput) -> currentInput.get(0));
  }

}
