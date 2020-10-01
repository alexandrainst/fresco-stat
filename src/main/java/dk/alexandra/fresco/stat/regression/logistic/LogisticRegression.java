package dk.alexandra.fresco.stat.regression.logistic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;

/**
 * A naive implementation of logistic regression, not optimized for secure computation. Given a data
 * set consisting of column vectors and a list of expected classifications (0 or 1), this
 * computation performs a linear regression on the data and the expected outcome as log-odds. See
 * also <a href= "https://en.wikipedia.org/wiki/Logistic_regression">https://en.wikipedia.org/wiki/Logistic_regression</a>.
 */
public class LogisticRegression implements Computation<List<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final double[] guess;
  private final Matrix<DRes<SFixed>> data;
  private final List<DRes<SFixed>> expected;
  private final IntToDoubleFunction rate;
  private final int epochs;

  public LogisticRegression(Matrix<DRes<SFixed>> data, List<DRes<SFixed>> expected, double[] beta,
      IntToDoubleFunction rate,
      int epochs) {
    this.data = data;
    this.expected = expected;
    this.guess = beta;
    this.rate = rate;
    this.epochs = epochs;
  }

  public LogisticRegression(Matrix<DRes<SFixed>> data, List<DRes<SFixed>> expected, double[] beta,
      double rate, int epochs) {
    this(data, expected, beta, i -> rate, epochs);
  }

  @Override
  public DRes<List<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      int round = 0;
      List<DRes<SFixed>> b = Arrays.stream(guess).mapToObj(FixedNumeric.using(seq)::known).collect(
          Collectors.toList());
      return new IterationState(round, () -> b);
    }).whileLoop((state) -> state.round < epochs, (seq, state) -> {
//            for (int i = 0; i < guess.length; i++) {
//              new OpenAndPrintSFixed("beta_" + i, state.b.out().get(i)).buildComputation(seq);
//            }
//            seq.debug().marker("Round " + state.round, System.out);
      DRes<List<DRes<SFixed>>> newB =
          new LogisticRegressionGD(data, expected, rate.applyAsDouble(state.round), state.b.out())
              .buildComputation(seq);
      return new IterationState(state.round + 1, newB);
    }).seq((seq, state) -> state.b);
  }

  private static final class IterationState implements DRes<IterationState> {

    private final DRes<List<DRes<SFixed>>> b;
    private final int round;

    private IterationState(int round, DRes<List<DRes<SFixed>>> value) {
      this.round = round;
      this.b = value;
    }

    @Override
    public IterationState out() {
      return this;
    }

  }

}
