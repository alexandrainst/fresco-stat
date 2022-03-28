package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.stat.descriptive.sort.FindTiedGroups;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convert an instance of type <code>T</code> into a {@link Pair} of a {@link DRes}&lt;{@link
 * SInt}&gt; and a {@link List}&lt;{@link DRes}&lt;{@link SInt}&gt;&gt; and back for use with {@link
 * dk.alexandra.fresco.lib.common.collections.Collections#sort(List)} to sort on the time of event
 * parameter.
 */
public class SurvivalEntrySorter implements
    Computation<Pair<List<SurvivalEntry>, List<BigInteger>>, ProtocolBuilderNumeric> {

  private final List<SurvivalEntry> data;

  public SurvivalEntrySorter(
      List<SurvivalEntry> data) {
    this.data = data;
  }

  Pair<DRes<SInt>, List<DRes<SInt>>> encode(SurvivalEntry survivalInfo) {
    return SurvivalEntry.encode(survivalInfo);
  }

  SurvivalEntry decode(Pair<DRes<SInt>, List<DRes<SInt>>> survivalInfo) {
    return SurvivalEntry.decode(survivalInfo);
  }

  @Override
  public DRes<Pair<List<SurvivalEntry>, List<BigInteger>>> buildComputation(
      ProtocolBuilderNumeric builder) {

    return builder.seq(seq -> {

      List<Pair<DRes<SInt>, List<DRes<SInt>>>> values =
          data.stream().map(this::encode).collect(Collectors.toList());
      return dk.alexandra.fresco.lib.common.collections.Collections.using(seq).sort(values);

    }).seq((seq, sorted) -> {
      List<SurvivalEntry> sortedData = sorted.stream().map(this::decode)
          .collect(Collectors.toList());

      // Find ties
      DRes<List<BigInteger>> tiedGroups = new FindTiedGroups(
          VectorUtils.listBuilder(sortedData.size(), i -> sorted.get(i).getFirst()))
          .buildComputation(seq);
      return Pair.lazy(sortedData, tiedGroups);

    }).seq((seq, result) -> Pair.lazy(result.getFirst(), result.getSecond().out()));
  }
}
