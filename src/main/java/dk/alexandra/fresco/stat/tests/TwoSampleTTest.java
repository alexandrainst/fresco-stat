package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.Mean;
import dk.alexandra.fresco.stat.descriptive.Variance;
import java.math.BigDecimal;
import java.util.List;

public class TwoSampleTTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<DRes<SReal>> data1;
  private List<DRes<SReal>> data2;

  public TwoSampleTTest(List<DRes<SReal>> data1, List<DRes<SReal>> data2) {
    this.data1 = data1;
    this.data2 = data2;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      // TODO: Calculations on the two datasets can be done in parallel.
      DRes<SReal> mean1 = seq.seq(new Mean(data1));
      DRes<SReal> mean2 = seq.seq(new Mean(data2));
      DRes<SReal> var1 = seq.seq(new Variance(data1, mean1));
      DRes<SReal> var2 = seq.seq(new Variance(data2, mean2));
      return () -> new Pair<>(new Pair<>(mean1, var1), new Pair<>(mean2, var2));
    }).seq((seq, stats) -> {
      DRes<SReal> n =
          seq.realNumeric().sub(stats.getFirst().getFirst(), stats.getSecond().getFirst());
      DRes<SReal> d = null;
      if (data1.size() == data2.size()) {
        d = seq.realNumeric().add(stats.getFirst().getSecond(), stats.getSecond().getSecond());
        d = seq.realAdvanced().sqrt(d);
        d = seq.realNumeric().mult(BigDecimal.valueOf(1.0 / Math.sqrt(data1.size())), d);
      } else {
        d = seq.realNumeric().mult(BigDecimal.valueOf(data1.size() - 1),
            stats.getFirst().getSecond());
        d = seq.realNumeric().add(d, seq.realNumeric().mult(BigDecimal.valueOf(data2.size() - 1),
            stats.getSecond().getSecond()));
        d = seq.realNumeric().div(d, BigDecimal.valueOf(data1.size() + data2.size() - 2));
        d = seq.realNumeric()
            .mult(BigDecimal.valueOf(Math.sqrt(1.0 / data1.size() + 1.0 / data2.size())), d);
      }
      return seq.realNumeric().div(n, d);
    });
  }


}
