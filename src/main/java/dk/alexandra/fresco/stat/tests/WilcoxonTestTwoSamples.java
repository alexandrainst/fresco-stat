package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.lib.fixed.utils.MultiplyWithSInt;
import dk.alexandra.fresco.stat.descriptive.LeakyBreakTies;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Wilcoxon signed-rank test
 */
public class WilcoxonTestTwoSamples implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<Pair<DRes<SFixed>, DRes<SFixed>>> data;

  public WilcoxonTestTwoSamples(List<Pair<DRes<SFixed>, DRes<SFixed>>> data) {
    this.data = data;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<SFixed>> oneSample = data.stream()
          .map(pair -> FixedNumeric.using(par).sub(pair.getFirst(), pair.getSecond())).collect(
              Collectors.toList());
      return DRes.of(oneSample);
    }).seq((seq, oneSample) -> seq.seq(new WilcoxonTestOneSample(oneSample)));
  }
}
