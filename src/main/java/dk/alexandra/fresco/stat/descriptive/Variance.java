package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class Variance implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> observed;
  private DRes<SReal> mean;

  public Variance(List<DRes<SReal>> observed) {
    this.observed = observed;
  }

  public Variance(List<DRes<SReal>> observed, DRes<SReal> mean) {
    this.observed = observed;
    this.mean = mean;
  }
  
  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      if (this.mean != null) {
        return mean;
      } else {
        return seq.seq(new Mean(observed));
      }
    }).par((par, mean) -> {
      List<DRes<SReal>> terms = observed.stream().map(x -> par.realNumeric().sub(x, () -> mean))
          .collect(Collectors.toList());
      return () -> terms;
    }).par((par, terms) -> {
      List<DRes<SReal>> squaredTerms =
          terms.stream().map(x -> par.realNumeric().mult(x, x)).collect(Collectors.toList());
      return () -> squaredTerms;
    }).seq((seq, terms) -> {
      DRes<SReal> sum = seq.realAdvanced().sum(terms);
      return seq.realNumeric().div(sum, BigDecimal.valueOf(observed.size() - 1));
    });
  }

}