package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a data entry in data for survival analysis with continuous covariates.
 */
public class SurvivalEntry implements DRes<SurvivalEntry> {

  private final List<DRes<SFixed>> covariates;
  private final DRes<SInt> time;
  private final DRes<SInt> censored;

  public SurvivalEntry(List<DRes<SFixed>> covariates, DRes<SInt> time,
      DRes<SInt> censored) {
    this.covariates = covariates;
    this.time = time;
    this.censored = censored;
  }

  /**
   * Encode an entry in a survival analysis dataset as a key-value pair where the event time is the
   * key and the status and covariates are the value. This may be decoded using {@link
   * SurvivalEntry#decode(Pair)} method.
   */
  public static Pair<DRes<SInt>, List<DRes<SInt>>> encode(SurvivalEntry subject) {
    List<DRes<SInt>> row = new ArrayList<>();

    row.add(subject.getCensored());

    for (DRes<SFixed> covariate : subject.getCovariates()) {
      row.add(covariate.out().getSInt());
    }

    return new Pair<>(subject.getTime(), row);
  }

  /**
   * Decode a key-value pair encoded using the {@link dk.alexandra.fresco.stat.survival.SurvivalEntry#encode(dk.alexandra.fresco.stat.survival.SurvivalEntry)}
   * method
   */
  public static SurvivalEntry decode(Pair<DRes<SInt>, List<DRes<SInt>>> subject) {
    DRes<SInt> censored = subject.getSecond().get(0);
    DRes<SInt> time = subject.getFirst();

    List<DRes<SFixed>> covariates = new ArrayList<>();
    for (int i = 1; i < subject.getSecond().size(); i++) {
      covariates
          .add(new SFixed(subject.getSecond().get(i)));
    }
    return new SurvivalEntry(covariates, time, censored);
  }

  public List<DRes<SFixed>> getCovariates() {
    return covariates;
  }

  public DRes<SInt> getTime() {
    return time;
  }

  public DRes<SInt> getCensored() {
    return censored;
  }

  @Override
  public SurvivalEntry out() {
    return this;
  }
}
