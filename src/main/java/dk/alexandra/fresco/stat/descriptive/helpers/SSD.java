package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute the sum of squared deviations
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class SSD implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final DRes<SFixed> mean;

  public SSD(List<DRes<SFixed>> data, DRes<SFixed> mean) {
    this.data = data;
    this.mean = mean;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.par(par -> {
      List<DRes<SFixed>> terms = data.stream().map(x -> FixedNumeric.using(par).sub(x, mean))
          .collect(Collectors.toList());
      return () -> terms;
    }).par((par, terms) -> {
      List<DRes<SFixed>> squaredTerms =
          terms.stream().map(x -> FixedNumeric.using(par).mult(x, x)).collect(Collectors.toList());
      return () -> squaredTerms;
    }).seq((seq, terms) -> {
      return AdvancedFixedNumeric.using(seq).sum(terms);
    });
  }

}
