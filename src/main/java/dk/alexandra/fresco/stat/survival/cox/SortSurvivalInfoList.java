package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.stat.utils.sort.OddEvenIntegerMerge;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortSurvivalInfoList implements Computation<List<SurvivalInfoDiscrete>, ProtocolBuilderNumeric> {

  private final List<SurvivalInfoDiscrete> data;

  /**
   * Sort the given list of data points by the time parameter.
   *
   * @param data
   */
  public SortSurvivalInfoList(List<SurvivalInfoDiscrete> data) {
    this.data = data;
  }

  @Override
  public DRes<List<SurvivalInfoDiscrete>> buildComputation(
      ProtocolBuilderNumeric builder) {

    return builder.seq(seq -> {

      List<Pair<DRes<SInt>, List<DRes<SInt>>>> values = new ArrayList<>();

      for (SurvivalInfoDiscrete x : data) {
        ArrayList<DRes<SInt>> row = new ArrayList<>();

        row.add(x.getCensored());

        for (Pair<List<DRes<SInt>>, DRes<SInt>> covariate : x.getCovariates()) {
          row.addAll(covariate.getFirst());
          row.add(covariate.getSecond());
        }

        values.add(new Pair<>(x.getTime(), row));
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

      DRes<List<Pair<DRes<SInt>, List<DRes<SInt>>>>> sorted = new OddEvenIntegerMerge(values).buildComputation(seq);

      return sorted;

    }).seq((seq, sorted) -> {
      List<SurvivalInfoDiscrete> sortedData = new ArrayList<>();

      // Remove dummy values
      sorted = sorted.subList(0, data.size());

      // Store result as a list of SurvivalInfoDiscrete's
      for (Pair<DRes<SInt>, List<DRes<SInt>>> value : sorted) {
        DRes<SInt> censored = value.getSecond().get(0);
        DRes<SInt> time = value.getFirst();

        List<Pair<List<DRes<SInt>>, DRes<SInt>>> covariates = new ArrayList<>();
        int i = 1;
        for (Pair<List<DRes<SInt>>, DRes<SInt>> c : data.get(0).getCovariates()) {
          int s = c.getFirst().size(); // Get expected size from original data
          covariates.add(new Pair<>(value.getSecond().subList(i, i+s), value.getSecond().get(i+s)));
          i += s + 1;
        }
        sortedData.add(new SurvivalInfoDiscrete(covariates, time, censored));
      }
      return () -> sortedData;
    });
  }
}
