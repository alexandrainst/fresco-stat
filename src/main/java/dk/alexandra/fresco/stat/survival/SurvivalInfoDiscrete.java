package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * Represents a data point in data for survival analysis with only discrete covariates on finite
 * sets. Each entry in the covariates set is an 0-1 indicator vector with exactly one non-zero value
 * indicating the value of that particular covariate, e.g. (1,0,0,0), (0,1,0,0), (0,0,1,0) or
 * (0,0,0,1) if the covariate has four different values.
 */
public class SurvivalInfoDiscrete {

  private final List<List<DRes<SInt>>> covariates;
  private final DRes<SInt> time;
  private final DRes<SInt> censored;

  public SurvivalInfoDiscrete(List<List<DRes<SInt>>> covariates, DRes<SInt> time,
      DRes<SInt> censored) {
    this.covariates = covariates;
    this.time = time;
    this.censored = censored;
  }

  public List<List<DRes<SInt>>> getCovariates() {
    return covariates;
  }

  public DRes<SInt> getTime() {
    return time;
  }

  public DRes<SInt> getCensored() {
    return censored;
  }
}
