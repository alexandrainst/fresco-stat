package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;

public class SurvivalInfoSorterDiscrete extends SurvivalInfoSorter<SurvivalInfoDiscrete> {

  public SurvivalInfoSorterDiscrete(
      List<SurvivalInfoDiscrete> data) {
    super(data);
  }

  @Override
  Pair<DRes<SInt>, List<DRes<SInt>>> encode(SurvivalInfoDiscrete subject) {
    ArrayList<DRes<SInt>> row = new ArrayList<>();

    row.add(subject.getCensored());

    for (List<DRes<SInt>> covariate : subject.getCovariates()) {
      row.addAll(covariate);
    }

    return new Pair<>(subject.getTime(), row);
  }

  @Override
  SurvivalInfoDiscrete decode(Pair<DRes<SInt>, List<DRes<SInt>>> subject) {
    DRes<SInt> censored = subject.getSecond().get(0);
    DRes<SInt> time = subject.getFirst();

    List<List<DRes<SInt>>> covariates = new ArrayList<>();
    int i = 1;
    for (List<DRes<SInt>> c : data.get(0).getCovariates()) {
      int s = c.size(); // Get expected size from original data
      covariates
          .add(subject.getSecond().subList(i, i + s));
      i += s;
    }
    return new SurvivalInfoDiscrete(covariates, time, censored);

  }

}
