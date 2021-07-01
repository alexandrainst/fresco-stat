package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.collections.sort.OddEvenMerge;
import dk.alexandra.fresco.lib.common.math.AdvancedBinary;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.AdvancedLinearAlgebra;
import java.util.List;
import java.util.stream.Collectors;

/** Compute the sample median of a sample */
public class SampleMedian implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;

  public SampleMedian(List<DRes<SFixed>> data) {
    this.data = data;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      List<DRes<SInt>> empty = List.of();
      return Collections.using(seq).sort(data.stream().map(x -> new Pair<>(x.out().getSInt(), empty)).collect(Collectors.toList()));
    }).seq((seq, sorted) -> {
      if (Math.floorMod(sorted.size(), 2) == 1) {
        return new SFixed(sorted.get(sorted.size() / 2).getFirst());
      } else {
        DRes<SInt> sum = seq.numeric().add(sorted.get(sorted.size() / 2).getFirst(),
            sorted.get(sorted.size() / 2 - 1).getFirst());
        return new SFixed(AdvancedNumeric.using(seq).truncate(sum, 1));
      }
    });
  }
}
