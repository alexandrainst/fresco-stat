package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

public class RealUtils {

  /**
   * Compute the product of all elements in the list.
   *
   * @param input   The list of factors.
   * @param builder The builder to use.
   * @return The product of all elements in the input list.
   */
  public static DRes<SFixed> product(List<DRes<SFixed>> input, ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> DRes.of(input))
        .whileLoop((inputs) -> inputs.size() > 1, (seq, inputs) -> seq.par(parallel -> {
          List<DRes<SFixed>> out = new ArrayList<>();
          DRes<SFixed> left = null;
          for (DRes<SFixed> input1 : inputs) {
            if (left == null) {
              left = input1;
            } else {
              out.add(FixedNumeric.using(parallel).mult(left, input1));
              left = null;
            }
          }
          if (left != null) {
            out.add(left);
          }
          return DRes.of(out);
        })).seq((r3, currentInput) -> currentInput.get(0));
  }

}
