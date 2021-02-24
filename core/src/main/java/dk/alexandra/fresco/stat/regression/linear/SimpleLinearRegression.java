package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.helpers.SPD;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import java.util.List;

/**
 * This computation returns coefficients a and b based on a simple linear regression of the observed
 * x and y values.
 */
public class SimpleLinearRegression implements
    Computation<Pair<DRes<SFixed>, DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> x;
  private final List<DRes<SFixed>> y;
  private final DRes<SFixed> meanY;
  private final DRes<SFixed> meanX;

  public SimpleLinearRegression(List<DRes<SFixed>> x, DRes<SFixed> meanX, List<DRes<SFixed>> y,
      DRes<SFixed> meanY) {
    this.x = x;
    this.meanX = meanX;
    this.y = y;
    this.meanY = meanY;
  }

  @Override
  public DRes<Pair<DRes<SFixed>, DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(builder -> {

      DRes<SFixed> spd = new SPD(x, meanX, y, meanY).buildComputation(builder);
      DRes<SFixed> ssd = new SSD(x, meanX).buildComputation(builder);

      return Pair.lazy(spd, ssd);

    }).seq((builder, spdAndSsd) -> {

      FixedNumeric numeric = FixedNumeric.using(builder);
      DRes<SFixed> b = numeric
          .mult(spdAndSsd.getFirst(),
              AdvancedFixedNumeric.using(builder).reciprocal(spdAndSsd.getSecond()));
      DRes<SFixed> a = numeric
          .sub(meanY, numeric.mult(b, meanX));

      return Pair.lazy(a, b);

    });
  }

}
