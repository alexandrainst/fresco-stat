package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/** Compute some quantiles for a sample */
public class SampleQuantiles implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final double[] quantiles;

  public SampleQuantiles(List<DRes<SFixed>> data, double[] quantiles) {
    this.data = data;
    this.quantiles = quantiles;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      List<DRes<SInt>> empty = List.of();
      return Collections.using(seq).sort(data.stream().map(x -> new Pair<>(x.out().getSInt(), empty)).collect(Collectors.toList()));
    }).seq((seq, sorted) -> {
      java.util.Collections.reverse(sorted);
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      List<DRes<SFixed>> percentiles = new ArrayList<>();
      for (double quantile : quantiles) {
        double pos = quantile * (sorted.size() + 1);
        if (pos < 1) {
          percentiles.add(new SFixed(sorted.get(0).getFirst()));
        } else if (pos >= sorted.size()) {
          percentiles.add(new SFixed(sorted.get(sorted.size() - 1).getFirst()));
        } else {
          double d = pos - Math.floor(pos);
          int i = (int) Math.floor(pos);
          DRes<SFixed> lower = new SFixed(sorted.get(i - 1).getFirst());
          DRes<SFixed> upper = new SFixed(sorted.get(i).getFirst());
          percentiles.add(fixedNumeric.add(lower, fixedNumeric.mult(d, fixedNumeric.sub(upper, lower))));
        }
      }
      return DRes.of(percentiles);
    });
  }
}
