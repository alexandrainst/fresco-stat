package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.compare.Comparison;

/**
 * Returns [max(x, y), 1 if y = max(x, y) and 0 otherwise]
 */
public class MaxPair implements Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

  private final DRes<SInt> x, y;

  public MaxPair(DRes<SInt> x, DRes<SInt> y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.seq(seq ->
      Comparison.using(seq).compareLEQ(x, y)).pairInPar(
        (seq, indicator) -> Pair.lazy(indicator, seq.numeric().mult(indicator, y)),
        (seq, indicator) -> Pair.lazy(indicator, seq.numeric().mult(seq.numeric().sub(1, indicator), x))
    ).seq((seq, indicators) -> Pair.lazy(
        seq.numeric().add(indicators.getFirst().getSecond(), indicators.getSecond()
        .getSecond()), indicators.getFirst().getFirst()));
    }

}
