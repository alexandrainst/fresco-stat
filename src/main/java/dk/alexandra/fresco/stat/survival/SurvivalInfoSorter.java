package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convert an instance of type <code>T</code> into a {@link Pair} of a {@link DRes}&lt;{@link
 * SInt}&gt; and a {@link List}&lt;{@link DRes}&lt;{@link SInt}&gt;&gt; and back for use with {@link
 * dk.alexandra.fresco.lib.common.collections.Collections#sort(List)} to sort on the time of event
 * parameter.
 *
 * @param <T> A subject for survival analysis.
 */
public abstract class SurvivalInfoSorter<T> implements
    Computation<List<T>, ProtocolBuilderNumeric> {

  protected final List<T> data;

  public SurvivalInfoSorter(List<T> data) {
    this.data = data;
  }

  abstract Pair<DRes<SInt>, List<DRes<SInt>>> encode(T survivalInfo);

  abstract T decode(Pair<DRes<SInt>, List<DRes<SInt>>> survivalInfo);

  @Override
  public DRes<List<T>> buildComputation(
      ProtocolBuilderNumeric builder) {

    return builder.seq(seq -> {
      List<Pair<DRes<SInt>, List<DRes<SInt>>>> values =
          data.stream().map(this::encode).collect(Collectors.toList());
      return dk.alexandra.fresco.lib.common.collections.Collections.using(seq).sort(values);
    }).seq((seq, sorted) -> {
      List<T> sortedData = sorted.stream().map(this::decode).collect(Collectors.toList());
      return DRes.of(sortedData);
    });
  }
}
