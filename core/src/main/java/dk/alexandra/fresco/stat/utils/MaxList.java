package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Given a list <i>X = [x<sub>0</sub>, ..., x<sub>n-1</sub>]</i> with <i>n</i> a power of <i>2</i>,
 * this method returns a pair [max(X), i : x<sub>i</sub> = max(X)]. In the case of ties, the highest
 * index is chosen. The algorithm is in log n rounds each with a total of n-1 comparisons where n is
 * the number of inputs.
 */
public class MaxList implements Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> input;

  public MaxList(List<DRes<SInt>> input) {
    this.input = input;
  }

  public static Computation<Pair<DRes<SFixed>, DRes<SInt>>, ProtocolBuilderNumeric> withSFixed(
      List<DRes<SFixed>> input) {
    return builder -> builder.seq(seq -> {
      List<DRes<SInt>> inputAsSInts = input.stream().map(DRes::out).map(SFixed::getSInt).collect(
          Collectors.toList());
      return seq.seq(new MaxList(inputAsSInts));
    }).seq((seq, intOutput) -> Pair.lazy(new SFixed(intOutput.getFirst()), intOutput.getSecond()));
  }

  @Override
  public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<Pair<DRes<SInt>, DRes<SInt>>>> maximums = new ArrayList<>();
      for (int i = 0; i < input.size(); i += 2) {
        if (i < input.size() / 2) {
          maximums.add(
              new MaxWithIndicatorOffset(input.get(i), input.get(i + 1), i).buildComputation(par));
        } else {
          // There was an odd number of inputs, so we just pass the last input on to the next round
          maximums.add(Pair.lazy(input.get(i), par.numeric().known(i)));
        }
      }
      return DRes.of(maximums);
    }).whileLoop(maximums -> maximums.size() > 1, (seq, maximums) -> seq.par(par -> {
      List<DRes<Pair<DRes<SInt>, DRes<SInt>>>> newMaximums = new ArrayList<>();
      for (int i = 0; i < maximums.size(); i += 2) {
        if (i < maximums.size() / 2) {
          newMaximums.add(new MaxKeyValue(maximums.get(i).out(), maximums.get(i + 1).out())
              .buildComputation(par));
        } else {
          // There was an odd number of maximums from last round, so we just pass it on to the next round
          newMaximums.add(maximums.get(i));
        }
      }
      return DRes.of(newMaximums);
    })).seq((seq, indicators) -> indicators.get(0));
  }

  /**
   * Returns [max(x, y), indicatorOffset+1 if y = max(x, y) and indicatorOffset otherwise]
   */
  private static class MaxWithIndicatorOffset implements
      Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

    private final DRes<SInt> x, y;
    private final int indicatorOffset;

    public MaxWithIndicatorOffset(DRes<SInt> x, DRes<SInt> y, int indicatorOffset) {
      this.x = x;
      this.y = y;
      this.indicatorOffset = indicatorOffset;
    }

    @Override
    public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(
        ProtocolBuilderNumeric builder) {
      return builder.seq(new MaxPair(x, y)).seq((seq, max) -> Pair
          .lazy(max.getFirst(), seq.numeric().add(indicatorOffset, max.getSecond())));
    }
  }

  /**
   * Give two pairs [x, x'] and [y, y'], this computation returns a pair [max(x,y), y' if y =
   * max(x,y) and x' otherwise]
   */
  private static class MaxKeyValue implements
      Computation<Pair<DRes<SInt>, DRes<SInt>>, ProtocolBuilderNumeric> {

    private final Pair<DRes<SInt>, DRes<SInt>> x;
    private final Pair<DRes<SInt>, DRes<SInt>> y;

    public MaxKeyValue(Pair<DRes<SInt>, DRes<SInt>> x, Pair<DRes<SInt>, DRes<SInt>> y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public DRes<Pair<DRes<SInt>, DRes<SInt>>> buildComputation(
        ProtocolBuilderNumeric builder) {
      return builder.seq(new MaxPair(x.getFirst(), y.getFirst())).seq(
          (seq, max) -> Pair.lazy(max.getFirst(),
              seq.numeric().add(seq.numeric().mult(max.getSecond(), y.getSecond()),
                  seq.numeric().mult(seq.numeric().sub(1, max.getSecond()), x.getSecond()))));
    }
  }

}
