package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;

/**
 * Sample a number from a Bernoulli distribution which is 0 with propability <i>p</i> and 1 with
 * propability <i>1-p</i>.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class SampleBernoulliDistribution implements Computation<SInt, ProtocolBuilderNumeric> {

  private DRes<SReal> p;
  private Double knownP;

  public SampleBernoulliDistribution(double p) {
    this.knownP = p;
  }

  public SampleBernoulliDistribution(DRes<SReal> p) {
    this.p = p;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    if (knownP != null) {
      p = builder.realNumeric().known(BigDecimal.valueOf(knownP));
    }

    DRes<SReal> draw = new SampleUniformDistribution().buildComputation(builder);
    return builder.realNumeric().leq(p, draw);
  }

}
