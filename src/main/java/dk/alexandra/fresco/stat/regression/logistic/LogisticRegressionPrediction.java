package dk.alexandra.fresco.stat.regression.logistic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.math.Reciprocal;
import java.util.ArrayList;
import java.util.List;

public class LogisticRegressionPrediction implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> row;
  private final List<DRes<SFixed>> b;

  public LogisticRegressionPrediction(List<DRes<SFixed>> row, List<DRes<SFixed>> b) {
    assert (row.size() == b.size() - 1);

    this.row = row;
    this.b = b;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SFixed>> terms = new ArrayList<>();
      terms.add(b.get(0));
      for (int i = 0; i < row.size(); i++) {
        terms.add(FixedNumeric.using(par).mult(b.get(i + 1), row.get(i)));
      }
      return DRes.of(terms);
    }).seq((seq, terms) -> {
      DRes<SFixed> sum = AdvancedFixedNumeric.using(seq).sum(terms);
      return new Reciprocal(
          FixedNumeric.using(seq).add(1,
              new Reciprocal(AdvancedFixedNumeric.using(seq).exp(sum)).buildComputation(seq)))
          .buildComputation(seq);
    });
  }

}
