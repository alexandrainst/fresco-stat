package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.stat.optimization.dea.DeaSolver;
import dk.alexandra.fresco.stat.optimization.dea.DeaSolver.AnalysisType;
import dk.alexandra.fresco.stat.optimization.dea.DeaSolver.DeaResult;
import dk.alexandra.fresco.stat.optimization.lp.LPSolver;
import dk.alexandra.fresco.stat.optimization.lp.LPSolver.LPOutput;
import dk.alexandra.fresco.stat.optimization.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.stat.optimization.lp.LPTableau;
import java.util.List;

public class DefaultOptimization implements Optimization {

  private final ProtocolBuilderNumeric builder;

  DefaultOptimization(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<LPOutput> lpSolver(PivotRule pivotRule, LPTableau tableau,
      Matrix<DRes<SInt>> updateMatrix, DRes<SInt> pivot, List<DRes<SInt>> initialBasis,
      int maxNumberOfIterations) {
    return builder.seq(new LPSolver(pivotRule, tableau, updateMatrix, pivot, initialBasis, maxNumberOfIterations));
  }

  @Override
  public DRes<List<DeaResult>> deaSolver(AnalysisType type, List<List<DRes<SInt>>> inputValues,
      List<List<DRes<SInt>>> outputValues, List<List<DRes<SInt>>> setInput,
      List<List<DRes<SInt>>> setOutput) {
    return builder.seq(new DeaSolver(type, inputValues, outputValues, setInput, setOutput));
  }
}
