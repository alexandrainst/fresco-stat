package dk.alexandra.fresco.stat.regression.logistic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * A gradient descent algorithm to fit a logistic model to a dataset.
 */
public class LogisticRegressionGD
    implements Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SFixed>> data;
  private final List<DRes<SFixed>> expected;
  private final double rate;
  private final ArrayList<DRes<SFixed>> b;

  public LogisticRegressionGD(Matrix<DRes<SFixed>> data, List<DRes<SFixed>> expected, double rate,
      ArrayList<DRes<SFixed>> b) {

    assert (data.getWidth() == b.size() - 1);
    assert (data.getHeight() == expected.size());

    this.data = data;
    this.expected = expected;
    this.rate = rate;
    this.b = b;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      ArrayList<DRes<ArrayList<DRes<SFixed>>>> deltas = new ArrayList<>();
      for (int i = 0; i < data.getHeight(); i++) {
        deltas.add(new RowGradient(data.getRow(i), expected.get(i), b)
            .buildComputation(par));
      }
      return DRes.of(deltas);
    }).par((par, deltas) -> {
      ArrayList<DRes<SFixed>> betaDeltas = new ArrayList<>();
      for (int i = 0; i < b.size(); i++) {
        int finalI = i;
        betaDeltas.add(AdvancedFixedNumeric.using(par)
            .sum(VectorUtils.listBuilder(data.getHeight(), j -> deltas.get(j).out().get(
                finalI))));
      }
      return DRes.of(betaDeltas);
    }).par((par, betaDeltas) -> {
      ArrayList<DRes<SFixed>> scaledDeltas = new ArrayList<>();
      for (int i = 0; i < b.size(); i++) {
        scaledDeltas.add(FixedNumeric.using(par).mult(rate, betaDeltas.get(i)));
      }
      return DRes.of(scaledDeltas);
    }).par((par, scaledDeltas) -> {
      ArrayList<DRes<SFixed>> newBeta = new ArrayList<>();
      for (int i = 0; i < b.size(); i++) {
        newBeta.add(FixedNumeric.using(par).add(b.get(i), scaledDeltas.get(i)));
      }
      return DRes.of(newBeta);
    });
  }

  private static class RowGradient
      implements Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

    private final ArrayList<DRes<SFixed>> row;
    private final DRes<SFixed> expected;
    private final ArrayList<DRes<SFixed>> b;

    private RowGradient(ArrayList<DRes<SFixed>> row, DRes<SFixed> expected,
        ArrayList<DRes<SFixed>> b) {
      this.row = row;
      this.expected = expected;
      this.b = b;
    }

    @Override
    public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.seq(seq -> {
        DRes<SFixed> yHat = new LogisticRegressionPrediction(row, b).buildComputation(seq);
        return FixedNumeric.using(seq).sub(expected, yHat);
      }).par((par, t) -> {
        ArrayList<DRes<SFixed>> delta = new ArrayList<>(b.size());
        delta.add(t);
        for (DRes<SFixed> ri : row) {
          delta.add(FixedNumeric.using(par).mult(t, ri));
        }
        return DRes.of(delta);
      });
    }

  }

}
