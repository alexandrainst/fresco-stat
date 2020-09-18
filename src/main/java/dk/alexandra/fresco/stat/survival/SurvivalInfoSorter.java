package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.stat.utils.sort.OddEvenIntegerMerge;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SurvivalInfoSorter<T> implements
    Computation<List<T>, ProtocolBuilderNumeric> {

  protected final List<T> data;

  abstract Pair<DRes<SInt>, List<DRes<SInt>>> encode(T survivalInfo);
  abstract T decode(Pair<DRes<SInt>, List<DRes<SInt>>> survivalInfo);

  public SurvivalInfoSorter(List<T> data) {
    this.data = data;
  }

  @Override
  public DRes<List<T>> buildComputation(
      ProtocolBuilderNumeric builder) {

    return builder.seq(seq -> {

      List<Pair<DRes<SInt>, List<DRes<SInt>>>> values = new ArrayList<>();

      for (T subject : data) {
        values.add(encode(subject));
      }

      // Compute the smallest two power larger than or equal to the number of data points
      int m = 1;
      while (m < data.size()) {
        m *= 2;
      }

      // Pad with small values to ensure size is a two power
      DRes<SInt> min = seq.numeric().known(
          BigInteger.ONE.shiftLeft(seq.getBasicNumericContext().getMaxBitLength() - 1).negate());
      List<DRes<SInt>> indicators = new ArrayList<>(
          Collections.nCopies(values.get(0).getSecond().size(), seq.numeric().known(0)));
      for (int i = data.size(); i < m; i++) {
        values.add(new Pair<>(min, indicators));
      }

      DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> sorted = new OddEvenIntegerMerge(values)
          .buildComputation(seq);

      return sorted;

    }).seq((seq, sorted) -> {
      List<T> sortedData = new ArrayList<>();

      // Remove dummy values
      sorted = sorted.subList(0, data.size());

      for (Pair<DRes<SInt>, List<DRes<SInt>>> row : sorted) {
        sortedData.add(decode(row));
      }

      return () -> sortedData;
    });
  }
}
