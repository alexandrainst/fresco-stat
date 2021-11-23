package dk.alexandra.fresco.stat.filtered;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.FilteredStatistics;

public class FilteredResult implements DRes<FilteredResult> {

  private final DRes<SFixed> result;
  private final DRes<SInt> n;

  public FilteredResult(DRes<SFixed> result, DRes<SInt> n) {
    this.result = result;
    this.n = n;
  }

  public DRes<SFixed> getResult() {
    return result;
  }

  public DRes<SInt> getN() {
    return n;
  }

  @Override
  public FilteredResult out() {
    return this;
  }
}
