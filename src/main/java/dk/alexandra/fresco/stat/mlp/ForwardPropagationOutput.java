package dk.alexandra.fresco.stat.mlp;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;

/**
 * This class represents the output of forward propagation on a single layer.
 */
public class ForwardPropagationOutput implements DRes<ForwardPropagationOutput> {

  private final DRes<ArrayList<DRes<SFixed>>> beforeActivation;
  private final DRes<ArrayList<DRes<SFixed>>> afterActivation;

  ForwardPropagationOutput(DRes<ArrayList<DRes<SFixed>>> beforeActivation,
      DRes<ArrayList<DRes<SFixed>>> afterActivation) {
    this.beforeActivation = beforeActivation;
    this.afterActivation = afterActivation;
  }

  public DRes<ArrayList<DRes<SFixed>>> getBeforeActivation() {
    return beforeActivation;
  }

  public DRes<ArrayList<DRes<SFixed>>> getAfterActivation() {
    return afterActivation;
  }

  @Override
  public ForwardPropagationOutput out() {
    return this;
  }
}
