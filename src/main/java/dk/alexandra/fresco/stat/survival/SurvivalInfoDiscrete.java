package dk.alexandra.fresco.stat.survival;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.List;

/**
 * Represents a data point in data for survival analysis with only discrete covariates on finite
 * sets. Each entry in the covariates set is an 0-1 indicator vector with exactly one non-zero value
 * indicating the value of that particular covariate.
 */
public class SurvivalInfoDiscrete {

  private final List<Pair<List<DRes<SInt>>, DRes<SInt>>> covariates;
  private final DRes<SInt> time;
  private final DRes<SInt> censored;

  public SurvivalInfoDiscrete(List<Pair<List<DRes<SInt>>, DRes<SInt>>> covariates, DRes<SInt> time,
      DRes<SInt> censored) {
    this.covariates = covariates;
    this.time = time;
    this.censored = censored;
  }

  public List<Pair<List<DRes<SInt>>, DRes<SInt>>> getCovariates() {
    return covariates;
  }

  public DRes<SInt> getTime() {
    return time;
  }

  public DRes<SInt> getCensored() {
    return censored;
  }
}
