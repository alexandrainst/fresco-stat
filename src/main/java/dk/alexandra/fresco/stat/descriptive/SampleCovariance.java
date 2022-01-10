package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.helpers.SP;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.List;

/**
 * Compute the unbiased covariance matrix for the given observations
 */
public class SampleCovariance implements Computation<Matrix<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SFixed>>> observations;
  private final List<DRes<SFixed>> mean;

  /**
   * Create a new computation with a given computed sample mean. Use {@link SampleMean} to compute
   * the mean.
   */
  public SampleCovariance(List<List<DRes<SFixed>>> observations, List<DRes<SFixed>> mean) {
    if (mean.size() != observations.size()) {
      throw new IllegalArgumentException("There must be a sample mean per variable");
    }
    this.observations = observations;
    this.mean = mean;
  }

  @Override
  public DRes<Matrix<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric root) {
    // The columns represents variables
    int k = observations.size();
    int n = observations.get(0).size();

    return root.par(par -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      Matrix<DRes<SFixed>> corrected = MatrixUtils.buildMatrix(n, k, (i,j) ->
          fixedNumeric.sub(observations.get(j).get(i), mean.get(j)));
      return DRes.of(corrected);

    }).par((par, corrected) -> {

      // The covariance matrix is symmetric, so we compute only the lower half
      List<List<DRes<SFixed>>> upper = VectorUtils.listBuilder(k, i -> VectorUtils.listBuilder(i + 1,
          j -> par.seq(seq -> FixedNumeric.using(seq).mult(1.0 / (n-1),
              new SP(corrected.getColumn(i), corrected.getColumn(j)).buildComputation(seq)))));

      // Construct entire matrix
      return DRes.of(MatrixUtils.buildMatrix(k, k, (i,j) ->
          i >= j ? upper.get(i).get(j) : upper.get(j).get(i)));
    });
  }

}
