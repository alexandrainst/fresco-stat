package dk.alexandra.fresco.stat.regression.logistic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.linearalgebra.VectorUtils;
import java.util.ArrayList;
import java.util.List;

public class LogisticRegressionGD
    implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> data;
  private final List<DRes<SFixed>> expected;
  private final double rate;
  private final List<DRes<SFixed>> b;

  public LogisticRegressionGD(Matrix<DRes<SFixed>> data, List<DRes<SFixed>> expected, double rate,
      List<DRes<SFixed>> b) {

    assert (data.getWidth() == b.size() - 1);
    assert (data.getHeight() == expected.size());

    this.data = data;
    this.expected = expected;
    this.rate = rate;
    this.b = b;
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      List<DRes<List<DRes<SFixed>>>> deltas = new ArrayList<>();
      for (int i = 0; i < data.getHeight(); i++) {
        deltas.add(new RowGradient(data.getRow(i), expected.get(i), b)
            .buildComputation(par));
      }
      return () -> deltas;
    }).par((par, deltas) -> {
      List<DRes<SFixed>> betaDeltas = new ArrayList<>();
      for (int i = 0; i < b.size(); i++) {
        int finalI = i;
        betaDeltas.add(AdvancedFixedNumeric.using(par)
            .sum(VectorUtils.listBuilder(data.getHeight(), j -> deltas.get(j).out().get(
                finalI))));
      }
      return () -> betaDeltas;
    }).par((par, betaDeltas) -> {
      List<DRes<SFixed>> scaledDeltas = new ArrayList<>();
      for (int i = 0; i < b.size(); i++) {
        scaledDeltas.add(FixedNumeric.using(par).mult(rate, betaDeltas.get(i)));
      }
      return () -> scaledDeltas;
    }).par((par, scaledDeltas) -> {
      List<DRes<SFixed>> newBeta = new ArrayList<>();
      for (int i = 0; i < b.size(); i++) {
        newBeta.add(FixedNumeric.using(par).add(b.get(i), scaledDeltas.get(i)));
      }
      return () -> newBeta;
    });
  }

  private static class RowGradient
      implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

    private final List<DRes<SFixed>> row;
    private final DRes<SFixed> expected;
    private final List<DRes<SFixed>> b;

    private RowGradient(List<DRes<SFixed>> row, DRes<SFixed> expected, List<DRes<SFixed>> b) {
      this.row = row;
      this.expected = expected;
      this.b = b;
    }

    @Override
    public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.seq(seq -> {
        DRes<SFixed> yHat = new LogisticRegressionPrediction(row, b).buildComputation(seq);
        return FixedNumeric.using(seq).sub(expected, yHat);
      }).par((par, t) -> {
        List<DRes<SFixed>> delta = new ArrayList<>(b.size());
        delta.add(t);
        for (DRes<SFixed> ri : row) {
          delta.add(FixedNumeric.using(par).mult(t, ri));
        }
        return () -> delta;
      });
    }

  }

}
