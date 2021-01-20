package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample a number from an Irwin-Hall distribution which is the sum of <i>n</i> iid U(0,1)
 * distributions.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SampleIrwinHallDistribution implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final int n;

  public SampleIrwinHallDistribution(int n) {
    assert (n > 0);
    this.n = n;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SFixed>> uniforms = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        uniforms.add(new SampleUniformDistribution().buildComputation(par));
      }
      return () -> uniforms;
    }).seq((seq, uniforms) -> AdvancedFixedNumeric.using(seq).sum(uniforms));
  }

}

