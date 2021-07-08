package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compute the &Chi;<sup>2</sup>-test for goodness of fit of the given observatinos.
 */
public class ChiSquareTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final double[] expectedKnown;
  private final List<DRes<SInt>> observed;
  private final List<DRes<SFixed>> expected;

  public ChiSquareTest(List<DRes<SInt>> observed, List<DRes<SFixed>> expected) {
    this.observed = observed;
    this.expected = expected;
    this.expectedKnown = null;
  }

  public ChiSquareTest(List<DRes<SInt>> observed, double[] expected) {
    this.observed = observed;
    this.expectedKnown = expected;
    this.expected = null;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SFixed>> terms = new ArrayList<>();
      for (int i = 0; i < observed.size(); i++) {
        if (Objects.nonNull(expectedKnown)) {
          terms.add(par.seq(calculateTerm(observed.get(i), expectedKnown[i])));
        } else {
          terms.add(par.seq(calculateTerm(observed.get(i), expected.get(i))));
        }
      }
      return DRes.of(terms);
    }).seq((seq, terms) -> AdvancedFixedNumeric.using(seq).sum(terms));
  }

  private Computation<SFixed, ProtocolBuilderNumeric> calculateTerm(DRes<SInt> o, DRes<SFixed> e) {
    return builder -> {
      FixedNumeric numeric = FixedNumeric.using(builder);
      DRes<SFixed> t = numeric.sub(numeric.fromSInt(o), e);
      t = numeric.mult(t, t);
      return numeric.div(t, e);
    };
  }

  private Computation<SFixed, ProtocolBuilderNumeric> calculateTerm(DRes<SInt> o, double e) {
    return builder -> {
      FixedNumeric numeric = FixedNumeric.using(builder);
      DRes<SFixed> t = numeric.sub(numeric.fromSInt(o), e);
      t = numeric.mult(t, t);
      return numeric.div(t, e);
    };
  }
}
