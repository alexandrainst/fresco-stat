package dk.alexandra.fresco.stat.descriptive.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SSE implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> actual;
  private final List<DRes<SFixed>> expected;

  public SSE(List<DRes<SFixed>> expected, List<DRes<SFixed>> actual) {
    if (expected.size() != actual.size()) {
      throw new IllegalArgumentException("Lists must have same size");
    }
    this.expected = expected;
    this.actual = actual;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SFixed>> terms = new ArrayList<>();
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      for (int i = 0; i < actual.size(); i++) {
        terms.add(fixedNumeric.sub(actual.get(i), expected.get(i)));
      }
      return DRes.of(terms);
    }).seq((seq, differences) -> seq.seq(new USS(differences)));
  }
}
