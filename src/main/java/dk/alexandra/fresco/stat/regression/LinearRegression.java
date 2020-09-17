package dk.alexandra.fresco.stat.regression;

import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.helpers.SPD;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;
import dk.alexandra.fresco.stat.regression.LinearRegression.LinearFunction;

/**
 * This computation returns coefficients a and b based on a simple linear regression of the observed
 * x and y values.
 */
public class LinearRegression implements Computation<LinearFunction, ProtocolBuilderNumeric> {

  public class LinearFunction {

    private DRes<SReal> b;
    private DRes<SReal> a;

    private LinearFunction(DRes<SReal> a, DRes<SReal> b) {
      this.a = a;
      this.b = b;
    }

    public DRes<SReal> getA() {
      return a;
    }

    public DRes<SReal> getB() {
      return b;
    }
  }

  private List<DRes<SReal>> x;
  private List<DRes<SReal>> y;
  private DRes<SReal> meanY;
  private DRes<SReal> meanX;

  public LinearRegression(List<DRes<SReal>> x, DRes<SReal> meanX, List<DRes<SReal>> y,
      DRes<SReal> meanY) {
    this.x = x;
    this.meanX = meanX;
    this.y = y;
    this.meanY = meanY;
  }

  @Override
  public DRes<LinearFunction> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(builder -> {

      DRes<SReal> spd = new SPD(x, meanX, y, meanY).buildComputation(builder);
      DRes<SReal> ssd = new SSD(x, meanX).buildComputation(builder);

      return () -> new Pair<>(spd, ssd);

    }).seq((builder, spdAndSsd) -> {

      DRes<SReal> b = builder.realNumeric().mult(spdAndSsd.getFirst(), builder.realAdvanced().reciprocal(spdAndSsd.getSecond()));
      //DRes<SReal> b = builder.realNumeric().div(spdAndSsd.getFirst(), spdAndSsd.getSecond());
      DRes<SReal> a = builder.realNumeric().sub(meanY, builder.realNumeric().mult(b, meanX));

      return () -> new LinearFunction(a, b);

    });
  }

}
