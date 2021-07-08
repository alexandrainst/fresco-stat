package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.Sampler;
import java.util.Objects;

/**
 * This computation samples from a Laplace distribution with scale <i>b</i> and location 0.
 */
public class SampleLaplaceDistribution implements Computation<SFixed, ProtocolBuilderNumeric> {

  private DRes<SFixed> b;
  private double bKnown;

  public SampleLaplaceDistribution(DRes<SFixed> b) {
    this.b = b;
  }

  public SampleLaplaceDistribution(double b) {
    this.bKnown = b;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {

      Sampler sampler = Sampler.using(par);
      DRes<SFixed> exponential;
      if (Objects.nonNull(b)) {
        exponential = new SampleExponentialDistribution(b).buildComputation(par);
      } else {
        exponential = sampler.sampleExponentialDistribution(bKnown);
      }
      DRes<SInt> rademacher = sampler.sampleRademacherDistribution();
      return Pair.lazy(exponential, rademacher);
    }).seq((seq, p) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      return fixedNumeric.mult(p.getFirst(), fixedNumeric.fromSInt(p.getSecond()));
    });
  }

}
