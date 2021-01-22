package dk.alexandra.fresco.stat.regression;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.LinearInverseProblem;
import dk.alexandra.fresco.stat.linearalgebra.MoorePenrosePseudoInverse;
import java.util.ArrayList;
import java.util.List;

public class LinearRegression implements
    Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<ArrayList<DRes<SFixed>>> observations;
  private final int n;
  private final int p;
  private final ArrayList<DRes<SFixed>> y;

  public LinearRegression(List<ArrayList<DRes<SFixed>>> observations, ArrayList<DRes<SFixed>> y) {
    if (observations.stream().mapToInt(ArrayList::size).distinct().count() != 1) {
      throw new IllegalArgumentException("Each observation must contain the same number of entries");
    }

    if (observations.size() != y.size()) {
      throw new IllegalArgumentException("There must be the same number of observations and observed response variables");
    }

    this.observations = observations;
    this.n = observations.size();
    this.p = observations.get(0).size();
    this.y = y;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    ArrayList<ArrayList<DRes<SFixed>>> rows = new ArrayList<>();
    FixedNumeric fixedNumeric = FixedNumeric.using(builder);
    for (ArrayList<DRes<SFixed>> observation : observations) {
      ArrayList<DRes<SFixed>> row = new ArrayList<>();
      row.add(fixedNumeric.known(1));
      row.addAll(observation);
      rows.add(row);
    }
    Matrix<DRes<SFixed>> x = new Matrix<>(n, p+1, rows);

    return builder.seq(seq -> {
      return new LinearInverseProblem(x, y).buildComputation(seq);
//      return new MoorePenrosePseudoInverse(x).buildComputation(seq);
//    }).seq((seq, inv) -> {
//      return FixedLinearAlgebra.using(seq)
//          .vectorMult(DRes.of(inv), DRes.of(y));
    });
  }
}
