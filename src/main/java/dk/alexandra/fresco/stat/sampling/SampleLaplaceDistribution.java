package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;

/**
 * This computation samples from a Laplace distribution with scale <i>b</i> and location 0.
 */
public class SampleLaplaceDistribution implements Computation<SReal, ProtocolBuilderNumeric> {

  private DRes<SReal> b;
  private BigDecimal bKnown;

  public SampleLaplaceDistribution(DRes<SReal> b) {
    this.b = b;
  }

  public SampleLaplaceDistribution(BigDecimal b) {
    this.bKnown = b;
  }

  public SampleLaplaceDistribution(double b) {
    this(BigDecimal.valueOf(b));
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {

      DRes<SReal> exponential;
      if (bKnown != null) {
        exponential = new SampleExponentialDistribution(bKnown).buildComputation(par);
      } else {
        exponential = new SampleExponentialDistribution(b).buildComputation(par);
      }

      DRes<SInt> rademacher = new SampleRademacherDistribution().buildComputation(par);

      return () -> new Pair<>(exponential, rademacher);
    }).seq((seq, p) -> {
      return seq.realNumeric().mult(p.getFirst(), seq.realNumeric().fromSInt(p.getSecond()));
    });
  }

}
