package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

public class DurbinWatsonTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> residuals;
  private final DRes<SFixed> sse;

  public DurbinWatsonTest(List<DRes<SFixed>> residuals, DRes<SFixed> sse) {
    this.residuals = residuals;
    this.sse = sse;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(sub -> {
      List<DRes<SFixed>> numeratorTerms = new ArrayList<>();
      for (int i = 1; i < residuals.size(); i++) {
        int finalI = i;
        numeratorTerms.add(sub.seq(seq -> {
          DRes<SFixed> difference = FixedNumeric.using(seq).sub(residuals.get(finalI),
              residuals.get(finalI - 1));
          return FixedNumeric.using(seq).mult(difference, difference);
        }));
      }
      return DRes.of(numeratorTerms);
    }).seq((seq, numeratorTerms) -> {
      DRes<SFixed> numerator = AdvancedFixedNumeric.using(seq).sum(numeratorTerms);
      return FixedNumeric.using(seq).div(numerator, sse);
    });
  }
}
