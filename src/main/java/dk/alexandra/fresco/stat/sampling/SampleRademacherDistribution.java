package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * This computation samples from a Rademacher distribution which can be -1 or +1 each with
 * propability 1/2.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SampleRademacherDistribution implements Computation<SInt, ProtocolBuilderNumeric> {

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    DRes<SInt> bernoulli = builder.numeric().randomBit();

    return builder.numeric().sub(builder.numeric().mult(BigInteger.valueOf(2), bernoulli),
        BigInteger.ONE);
  }

}
