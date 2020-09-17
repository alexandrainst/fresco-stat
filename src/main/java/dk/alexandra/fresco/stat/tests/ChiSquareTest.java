package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compute the &Chi;<sup>2</sup>-test for goodness of fit of the given observatinos.
 *
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class ChiSquareTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private final double[] expectedKnown;
  private final List<DRes<SInt>> observed;
  private final List<DRes<SReal>> expected;

  public ChiSquareTest(List<DRes<SInt>> observed, List<DRes<SReal>> expected) {
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
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SReal>> terms = new ArrayList<>();
      for (int i = 0; i < observed.size(); i++) {
        if (Objects.nonNull(expectedKnown)) {
          terms.add(par.seq(calculateTerm(observed.get(i), expectedKnown[i])));
        } else {
          terms.add(par.seq(calculateTerm(observed.get(i), expected.get(i))));
        }
      }
      return () -> terms;
    }).seq((seq, terms) -> seq.realAdvanced().sum(terms));
  }

  private Computation<SReal, ProtocolBuilderNumeric> calculateTerm(DRes<SInt> o, DRes<SReal> e) {
    return builder -> {
      DRes<SReal> t = builder.realNumeric().sub(builder.realNumeric().fromSInt(o), e);
      t = builder.realNumeric().mult(t, t);
      return builder.realNumeric().div(t, e);
    };
  }

  private Computation<SReal, ProtocolBuilderNumeric> calculateTerm(DRes<SInt> o, double e) {
    return builder -> {
      DRes<SReal> t = builder.realNumeric().sub(builder.realNumeric().fromSInt(o), e);
      t = builder.realNumeric().mult(t, t);
      return builder.realNumeric().div(t, e);
    };
  }
}
