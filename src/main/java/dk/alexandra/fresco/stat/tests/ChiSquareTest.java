package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.ArrayList;
import java.util.List;

/**
 * Compute the &Chi;<sup>2</sup>-test for goodness of fit for the given observatinos.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class ChiSquareTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SInt>> observed;
  private List<DRes<SReal>> expected;

  public ChiSquareTest(List<DRes<SInt>> observed, List<DRes<SReal>> expected) {
    this.observed = observed;
    this.expected = expected;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SReal>> terms = new ArrayList<>();
      for (int i = 0; i < observed.size(); i++) {
        terms.add(par.seq(calculateTerm(observed.get(i), expected.get(i))));
      }
      return () -> terms;
    }).seq((seq, terms) -> {
      return seq.realAdvanced().sum(terms);
    });
  }

  private Computation<SReal, ProtocolBuilderNumeric> calculateTerm(DRes<SInt> o, DRes<SReal> e) {
    return builder -> {
      DRes<SReal> t = builder.realNumeric().sub(builder.realNumeric().fromSInt(o), e);
      t = builder.realNumeric().mult(t, t);
      return builder.realNumeric().div(t, e);
    };
  }

}
