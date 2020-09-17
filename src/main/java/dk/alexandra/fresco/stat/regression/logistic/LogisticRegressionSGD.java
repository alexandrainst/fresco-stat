package dk.alexandra.fresco.stat.regression.logistic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LogisticRegressionSGD
    implements Computation<List<DRes<SReal>>, ProtocolBuilderNumeric> {

  private Matrix<DRes<SReal>> data;
  private List<DRes<SReal>> expected;
  private double rate;
  private List<DRes<SReal>> b;

  public LogisticRegressionSGD(Matrix<DRes<SReal>> data, List<DRes<SReal>> expected, double rate,
      List<DRes<SReal>> b) {

    assert (data.getWidth() == b.size() - 1);
    assert (data.getHeight() == expected.size());

    this.data = data;
    this.expected = expected;
    this.rate = rate;
    this.b = b;
  }

  @Override
  public DRes<List<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
            List<DRes<List<DRes<SReal>>>> deltas = new ArrayList<>();
            for (int i = 0; i < data.getHeight(); i++) {
                deltas.add(new RowGradient(data.getRow(i), expected.get(i), b, rate)
                    .buildComputation(par));
            }
            return () -> deltas;
        }).par((par, deltas) -> {
            List<DRes<SReal>> betaDeltas = new ArrayList<>();
            for (int i = 0; i < b.size(); i++) {
                int finalI = i;
                betaDeltas.add(par.realAdvanced().sum(VectorUtils.listBuilder(data.getHeight(), j -> deltas.get(j).out().get(
                    finalI))));
            }
            return () -> betaDeltas;
        }).par((par, betaDeltas) -> {
            List<DRes<SReal>> newBeta = new ArrayList<>();
            for (int i = 0; i < b.size(); i++) {
                newBeta.add(par.realNumeric().add(b.get(i), betaDeltas.get(i)));
            }
            return  () -> newBeta;
        });
  }

  private static class RowGradient
      implements Computation<List<DRes<SReal>>, ProtocolBuilderNumeric> {

    private List<DRes<SReal>> row;
    private DRes<SReal> expected;
    private List<DRes<SReal>> b;
    private double rate;

    private RowGradient(List<DRes<SReal>> row, DRes<SReal> expected, List<DRes<SReal>> b,
        double rate) {
      this.row = row;
      this.expected = expected;
      this.b = b;
      this.rate = rate;
    }

    @Override
    public DRes<List<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.seq(seq -> {
        DRes<SReal> yHat = new LogisticRegressionPrediction(row, b).buildComputation(seq);
        DRes<SReal> error = seq.realNumeric().mult(rate, seq.realNumeric().sub(expected, yHat));
        return error;
      }).par((par, t) -> {
        List<DRes<SReal>> delta = new ArrayList<>(b.size());
        delta.add(t);
        for (DRes<SReal> ri : row) {
          delta.add(par.realNumeric().mult(t, ri));
        }
        return () -> delta;
      });
    }

  }

  private static class IterationState implements DRes<IterationState> {

    private int i;
    private final DRes<List<DRes<SReal>>> b;

    private IterationState(int round, DRes<List<DRes<SReal>>> value) {
      this.i = round;
      this.b = value;
    }

    @Override
    public IterationState out() {
      return this;
    }

  }

}
