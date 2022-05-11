package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.compare.Comparison;
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
public class WilcoxonTestOneSample implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;

  public WilcoxonTestOneSample(List<DRes<SFixed>> data) {
    this.data = data;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder
        .par(par -> DRes.of(data.stream().map(AdvancedFixedNumeric.using(par)::sign).collect(
            Collectors.toList()))
        ).par((par, signs) -> {

          // The sign function in fresco has sign(0) = 1, but we need sign(0) = 0 here so we need to
          // compare all values to zero
          List<DRes<SInt>> isZero = data.stream().map(DRes::out).map(SFixed::getSInt)
              .map(value -> Comparison.using(par)
                  .compareZero(value, par.getBasicNumericContext().getMaxBitLength())).collect(
                  Collectors.toList());
          return Pair.lazy(signs, isZero);

        }).par((par, signsAndZeroIndicator) -> {

          // Adjust sign indicators to handle zeroes
          List<DRes<SInt>> adjustedSigns = new ArrayList<>();
          for (int i = 0; i < data.size(); i++) {
            int finalI = i;
            adjustedSigns.add(par.seq(seq -> {
              DRes<SInt> isNonZero = seq.numeric().sub(1, signsAndZeroIndicator.getSecond().get(
                  finalI));
              return seq.numeric().mult(signsAndZeroIndicator.getFirst().get(finalI), isNonZero);
            }));
          }
          return DRes.of(adjustedSigns);

        }).seq((seq, signs) -> {

          // Compute absolute value of x as x * sign(x)
          List<DRes<SFixed>> absoluteValues = VectorUtils.entrywiseBinaryOp(data, signs,
              (a, b, c) -> new MultiplyWithSInt(a, b).buildComputation(c), seq);
          return Pair.lazy(absoluteValues, signs);

        }).seq((seq, absoluteValuesAndSigns) -> {

          // Sort entries according to their absolute values as key and let the sign be the value.
          return Collections.using(seq).sort(
              IntStream.range(0, data.size())
                  .mapToObj(
                      i -> new Pair<>(absoluteValuesAndSigns.getFirst().get(i).out().getSInt(),
                          List.of(absoluteValuesAndSigns.getSecond().get(i))))
                  .collect(Collectors.toList()));

        }).seq((seq, sortedSigns) -> {

          // Sorting in fresco returns the values in desceding order, so we reverse
          java.util.Collections.reverse(sortedSigns);
          return Pair
              .lazy(seq.seq(new LeakyBreakTies(sortedSigns.stream().map(Pair::getFirst).collect(
                  Collectors.toList()))),
                  sortedSigns.stream().map(Pair::getSecond).map(x -> x.get(0))
                      .collect(Collectors.toList()));

        }).par((par, rankedSigns) -> {

          List<DRes<SFixed>> terms = new ArrayList<>();
          for (int i = 0; i < data.size(); i++) {
            int finalI = i;
            // Ranks are computed from 1 but should be from 0, so we subtract 1
            terms.add(par.seq(seq -> FixedNumeric.using(seq).mult(rankedSigns.getFirst().out().get(
                finalI) - 1.0,
                FixedNumeric.using(seq).fromSInt(rankedSigns.getSecond().get(finalI)))));
          }
          return DRes.of(terms);

        }).seq((seq, terms) -> AdvancedFixedNumeric.using(seq).sum(terms));
  }
}
