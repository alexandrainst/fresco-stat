package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.ArrayList;
import java.util.List;

/**
 * Compute the sum of products of deviations of two samples.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SPD implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> x;
  private DRes<SReal> meanX;
  private List<DRes<SReal>> y;
  private DRes<SReal> meanY;

  public SPD(List<DRes<SReal>> x, DRes<SReal> meanX, List<DRes<SReal>> y, DRes<SReal> meanY) {
    assert (x.size() == y.size());
    this.x = x;
    this.meanX = meanX;
    this.y = y;
    this.meanY = meanY;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(builder -> {
      List<DRes<SReal>> terms = new ArrayList<>(x.size());
      for (int i = 0; i < x.size(); i++) {
        terms.add(new ComputePD(x.get(i), y.get(i)).buildComputation(builder));
      }
      return () -> terms;
    }).seq((builder, terms) -> {
      return builder.realAdvanced().sum(terms);
    });
  }

  private class ComputePD implements Computation<SReal, ProtocolBuilderNumeric> {

    private DRes<SReal> yi;
    private DRes<SReal> xi;

    private ComputePD(DRes<SReal> xi, DRes<SReal> yi) {
      this.xi = xi;
      this.yi = yi;
    }

    @Override
    public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.seq(seq -> {
        return seq.realNumeric().mult(seq.realNumeric().sub(xi, meanX),
            seq.realNumeric().sub(yi, meanY));
      });
    }

  }

}
