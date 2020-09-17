package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import java.math.BigDecimal;
import java.util.Objects;

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
      if (Objects.nonNull(bKnown)) {
        exponential = new SampleExponentialDistribution(bKnown).buildComputation(par);
      } else {
        exponential = new SampleExponentialDistribution(b).buildComputation(par);
        //DRes<SReal> bInverse = seq.realAdvanced().reciprocal(b);
//          return new SampleExponentialDistribution(bInverse).buildComputation(par);
//        });
      }

      DRes<SInt> rademacher = new SampleRademacherDistribution().buildComputation(par);

      return () -> new Pair<>(exponential, rademacher);
    }).seq((seq, p) -> {
      SFixed exp = (SFixed) p.getFirst().out();
      DRes<SInt> product = seq.numeric().mult(exp.getSInt(), p.getSecond());
      return new SFixed(product);
    });
  }

}
