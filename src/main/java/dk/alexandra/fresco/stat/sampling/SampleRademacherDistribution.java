package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * This computation samples from a Rademacher distribution which can be -1 or +1 each with
 * probability 1/2.
 */
public class SampleRademacherDistribution implements Computation<SInt, ProtocolBuilderNumeric> {

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {
      DRes<SInt> bernoulli = builder.numeric().randomBit();
      return builder.numeric().sub(builder.numeric().mult(2, bernoulli),
          1);
    });
  }

}
