package dk.alexandra.fresco.stat.mlp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;

/**
 * This class represents the output of back propagation on a single layer.
 */
public class BackPropagationOutput implements DRes<BackPropagationOutput> {

  private final DRes<ArrayList<DRes<SFixed>>> error, delta;

  public BackPropagationOutput(DRes<ArrayList<DRes<SFixed>>> error,
      DRes<ArrayList<DRes<SFixed>>> delta) {
    this.error = error;
    this.delta = delta;
  }

  public DRes<ArrayList<DRes<SFixed>>> getError() {
    return error;
  }

  public DRes<ArrayList<DRes<SFixed>>> getDelta() {
    return delta;
  }

  @Override
  public BackPropagationOutput out() {
    return this;
  }
}
