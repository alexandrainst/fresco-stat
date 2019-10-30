package dk.alexandra.fresco.stat.descriptive;

import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.helpers.SPD;
import dk.alexandra.fresco.stat.descriptive.helpers.SSD;

public class PearsonsCorrelation implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> x, y;
  private DRes<SReal> meanX, meanY;

  public PearsonsCorrelation(List<DRes<SReal>> x, DRes<SReal> meanX, List<DRes<SReal>> y, DRes<SReal> meanY) {
    assert(x.size() == y.size());
    this.x = x;
    this.meanX = meanX;
    this.y = y;
    this.meanY = meanY;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(builder -> {
      
      DRes<SReal> gammaX = new SSD(x, meanX).buildComputation(builder);
      DRes<SReal> gammaY = new SSD(y, meanY).buildComputation(builder);
      DRes<SReal> spd = new SPD(x, meanX, y, meanY).buildComputation(builder);
            
      return () -> List.of(spd, gammaX, gammaY);
      
    }).seq((builder, descriptive) -> {
      DRes<SReal> denom = builder.realNumeric().mult(descriptive.get(1), descriptive.get(2));     
      denom = builder.realAdvanced().sqrt(denom);
      denom = builder.realAdvanced().reciprocal(denom);
      DRes<SReal> result = builder.realNumeric().mult(descriptive.get(0), denom);
      return result;
    });
  }
}
