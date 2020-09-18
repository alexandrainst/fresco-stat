package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

public class SortSurvivalInfoListContinuous extends SurvivalInfoSorter<SurvivalInfoContinuous> {

  public SortSurvivalInfoListContinuous(
      List<SurvivalInfoContinuous> data) {
    super(data);
  }

  @Override
  Pair<DRes<SInt>, List<DRes<SInt>>> encode(SurvivalInfoContinuous subject) {
    List<DRes<SInt>> row = new ArrayList<>();

    row.add(subject.getCensored());

    for (DRes<SReal> covariate : subject.getCovariates()) {
      row.add(((SFixed) covariate.out()).getSInt());
    }

    return new Pair<>(subject.getTime(), row);
  }

  @Override
  SurvivalInfoContinuous decode(Pair<DRes<SInt>, List<DRes<SInt>>> subject) {
    DRes<SInt> censored = subject.getSecond().get(0);
    DRes<SInt> time = subject.getFirst();

    List<DRes<SReal>> covariates = new ArrayList<>();
    for (int i = 1; i < subject.getSecond().size(); i++) {
      covariates
          .add(new SFixed(subject.getSecond().get(i)));
    }
    return new SurvivalInfoContinuous(covariates, time, censored);
  }

}
