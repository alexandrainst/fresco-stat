package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.helpers.SPD;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import java.util.List;

public class PearsonsCorrelation implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> x;
  private final List<DRes<SFixed>> y;
  private final DRes<SFixed> meanX;
  private final DRes<SFixed> meanY;

  public PearsonsCorrelation(List<DRes<SFixed>> x, DRes<SFixed> meanX, List<DRes<SFixed>> y,
      DRes<SFixed> meanY) {
    assert (x.size() == y.size());
    this.x = x;
    this.meanX = meanX;
    this.y = y;
    this.meanY = meanY;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(builder -> {

      DRes<SFixed> gammaX = new SSD(x, meanX).buildComputation(builder);
      DRes<SFixed> gammaY = new SSD(y, meanY).buildComputation(builder);
      DRes<SFixed> spd = new SPD(x, meanX, y, meanY).buildComputation(builder);

      return () -> List.of(spd, gammaX, gammaY);

    }).seq((builder, descriptive) -> {
      FixedNumeric numeric = FixedNumeric.using(builder);
      AdvancedFixedNumeric advanced = AdvancedFixedNumeric.using(builder);

      DRes<SFixed> denom = numeric.mult(descriptive.get(1), descriptive.get(2));
      denom = advanced.sqrt(denom);
      denom = advanced.reciprocal(denom);
      return numeric.mult(descriptive.get(0), denom);
    });
  }
}
