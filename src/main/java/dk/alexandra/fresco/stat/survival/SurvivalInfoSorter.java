package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convert an instance of type T into a <code>Pair<DRes<SInt>, List<DRes<SInt>>></code> and back for
 * use with {@link dk.alexandra.fresco.lib.common.collections.Collections#sort(List)} to sort on the time of event parameter.
 *
 * @param <T> A subject for survival analysis.
 */
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

      return dk.alexandra.fresco.lib.common.collections.Collections.using(seq).sort(values);
    }).seq((seq, sorted) -> {
      List<T> sortedData = sorted.stream().map(this::decode).collect(Collectors.toList());
      return () -> sortedData;
    });
  }
}
