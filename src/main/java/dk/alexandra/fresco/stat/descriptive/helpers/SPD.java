package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Compute the sum of products of deviations of two samples.
 */
public class SPD implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> x;
  private final DRes<SFixed> meanX;
  private final List<DRes<SFixed>> y;
  private final DRes<SFixed> meanY;

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
      FixedNumeric fixedNumeric = FixedNumeric.using(builder);
      List<DRes<SFixed>> termsX = x.stream().map(xi -> fixedNumeric.sub(xi, meanX)).collect(
          Collectors.toList());
      List<DRes<SFixed>> termsY = y.stream().map(yi -> fixedNumeric.sub(yi, meanY)).collect(
          Collectors.toList());
      return Pair.lazy(termsX, termsY);
    }).seq((seq, terms) -> seq.seq(new SP(terms.getFirst(), terms.getSecond())));
  }

}
