package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

/**
 * Represents a data point in data for survival analysis with continuous covariates.
 */
public class SurvivalInfoContinuous {

  private final List<DRes<SFixed>> covariates;
  private final DRes<SInt> time;
  private final DRes<SInt> censored;

  public SurvivalInfoContinuous(List<DRes<SFixed>> covariates, DRes<SInt> time,
      DRes<SInt> censored) {
    this.covariates = covariates;
    this.time = time;
    this.censored = censored;
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
}
