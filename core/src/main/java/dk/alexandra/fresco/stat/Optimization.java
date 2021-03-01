package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.stat.optimization.dea.DeaSolver.AnalysisType;
import dk.alexandra.fresco.stat.optimization.dea.DeaSolver.DeaResult;
import dk.alexandra.fresco.stat.optimization.lp.LPSolver.LPOutput;
import dk.alexandra.fresco.stat.optimization.lp.LPSolver.PivotRule;
import dk.alexandra.fresco.stat.optimization.lp.LPTableau;
import java.util.List;

public interface Optimization {

  static Optimization using(ProtocolBuilderNumeric builder) {
    return new DefaultOptimization(builder);
  }

  /**
   * Optimize an . Note, we do not do full two-phase Simplex, so the initial state is assumed to be
   * good.
   *
   * @param pivotRule             the pivot rule to apply
   * @param tableau               the initial tableau, this will not be modified
   * @param updateMatrix          the initial update matrix, will be modified to the current state
   * @param pivot                 the initial pivot, will be modified to reflect the  state
   * @param initialBasis          the initial basis, will be modified to reflect the state
   * @param maxNumberOfIterations we might not terminate, the solver stops after this iteration
   */
  DRes<LPOutput> lpSolver(PivotRule pivotRule, LPTableau tableau, Matrix<DRes<SInt>> updateMatrix,
      DRes<SInt> pivot, List<DRes<SInt>> initialBasis, int maxNumberOfIterations);

  /**
   * Construct a DEA problem for the solver to solve. The problem consists of 4 matrices: 2 basis
   * input/output matrices containing the dataset which the queries will be measured against 2 query
   * input/output matrices containing the data to be evaluated.
   *
   * @param type         The type of analysis to do
   * @param inputValues  Matrix of query input values
   * @param outputValues Matrix of query output values
   * @param setInput     Matrix containing the basis input
   * @param setOutput    Matrix containing the basis output
   */
  DRes<List<DeaResult>> deaSolver(AnalysisType type, List<List<DRes<SInt>>> inputValues,
      List<List<DRes<SInt>>> outputValues, List<List<DRes<SInt>>> setInput,
      List<List<DRes<SInt>>> setOutput);
}
