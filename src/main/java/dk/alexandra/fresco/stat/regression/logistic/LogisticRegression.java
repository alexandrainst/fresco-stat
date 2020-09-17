package dk.alexandra.fresco.stat.regression.logistic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;

/**
 * A naive implementation of logistic regression, not optimized for secure computation. Given a data
 * set consisting of column vectors and a list of expected classifications (0 or 1), this
 * computation performs a linear regression on the data and the expected outcome as log-odds. See
 * also <a href=
 * "https://en.wikipedia.org/wiki/Logistic_regression">https://en.wikipedia.org/wiki/Logistic_regression</a>.
 */
public class LogisticRegression implements Computation<List<DRes<SReal>>, ProtocolBuilderNumeric> {

    private final double[] guess;
    private final Matrix<DRes<SReal>> data;
    private final List<DRes<SReal>> expected;
    private final IntToDoubleFunction rate;
    private final int epochs;

    public LogisticRegression(Matrix<DRes<SReal>> data, List<DRes<SReal>> expected, double[] beta,
        IntToDoubleFunction rate,
        int epochs) {
        this.data = data;
        this.expected = expected;
        this.guess = beta;
        this.rate = rate;
        this.epochs = epochs;
    }

    public LogisticRegression(Matrix<DRes<SReal>> data, List<DRes<SReal>> expected, double[] beta,
        double rate, int epochs) {
        this(data, expected, beta, i -> rate, epochs);
    }

    @Override
    public DRes<List<DRes<SReal>>> buildComputation(ProtocolBuilderNumeric builder) {

        return builder.seq(seq -> {
            int round = 0;
            List<DRes<SReal>> b = Arrays.stream(guess).mapToObj(seq.realNumeric()::known).collect(
                Collectors.toList());
            return new IterationState(round, () -> b);
        }).whileLoop((state) -> state.round < epochs, (seq, state) -> {
            seq.debug().marker("Round " + state.round, System.out);
            DRes<List<DRes<SReal>>> newB =
                    new LogisticRegressionGD(data, expected, rate.applyAsDouble(state.round), state.b.out()).buildComputation(seq);
            return new IterationState(state.round + 1, newB);
        }).seq((seq, state) -> state.b);
    }

    private static final class IterationState implements DRes<IterationState> {

        private int round;
        private final DRes<List<DRes<SReal>>> b;

        private IterationState(int round, DRes<List<DRes<SReal>>> value) {
            this.round = round;
            this.b = value;
        }

        @Override
        public IterationState out() {
            return this;
        }

    }

}
