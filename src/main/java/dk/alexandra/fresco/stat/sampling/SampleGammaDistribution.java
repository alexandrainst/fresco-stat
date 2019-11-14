package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SampleGammaDistribution implements Computation<SReal, ProtocolBuilderNumeric> {

  private final int shape;
  private final double scale;

  public SampleGammaDistribution(int shape, double scale) {
    this.shape = shape;
    this.scale = scale;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(par -> {
      List<DRes<SReal>> exps = Stream.generate(() -> new SampleExponentialDistribution(scale).buildComputation(par)).limit(shape).collect(
          Collectors.toList());
      return () -> exps;
    }).seq((seq, exps) -> seq.realAdvanced().sum(exps));


  }

}
