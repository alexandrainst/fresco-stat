package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

/**
 * Compute the sum of products of deviations of two samples.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SPD implements Computation<SFixed, ProtocolBuilderNumeric> {

  private List<DRes<SFixed>> x;
  private DRes<SFixed> meanX;
  private List<DRes<SFixed>> y;
  private DRes<SFixed> meanY;

  public SPD(List<DRes<SFixed>> x, DRes<SFixed> meanX, List<DRes<SFixed>> y, DRes<SFixed> meanY) {
    assert (x.size() == y.size());
    this.x = x;
    this.meanX = meanX;
    this.y = y;
    this.meanY = meanY;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(builder -> {
      List<DRes<SFixed>> terms = new ArrayList<>(x.size());
      for (int i = 0; i < x.size(); i++) {
        terms.add(new ComputePD(x.get(i), y.get(i)).buildComputation(builder));
      }
      return () -> terms;
    }).seq((builder, terms) -> AdvancedFixedNumeric.using(builder).sum(terms));
  }

  private class ComputePD implements Computation<SFixed, ProtocolBuilderNumeric> {

    private DRes<SFixed> yi;
    private DRes<SFixed> xi;

    private ComputePD(DRes<SFixed> xi, DRes<SFixed> yi) {
      this.xi = xi;
      this.yi = yi;
    }

    @Override
    public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.seq(seq -> FixedNumeric.using(seq).mult(FixedNumeric.using(seq).sub(xi, meanX),
          FixedNumeric.using(seq).sub(yi, meanY)));
    }

  }

}
