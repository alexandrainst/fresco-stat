package dk.alexandra.fresco.stat.regression.logistic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.lib.real.math.Reciprocal;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LogisticRegressionPrediction implements Computation<SReal, ProtocolBuilderNumeric> {

    private List<DRes<SReal>> row;
    private List<DRes<SReal>> b;

    public LogisticRegressionPrediction(List<DRes<SReal>> row, List<DRes<SReal>> b) {
        assert (row.size() == b.size() - 1);

        this.row = row;
        this.b = b;
    }

    @Override
    public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.par(par -> {
            List<DRes<SReal>> terms = new ArrayList<>();
            terms.add(b.get(0));
            for (int i = 0; i < row.size(); i++) {
                terms.add(par.realNumeric().mult(b.get(i + 1), row.get(i)));
            }
            return () -> terms;
        }).seq((seq, terms) -> {
            DRes<SReal> sum = seq.realAdvanced().sum(terms);
            DRes<SReal> yHat = new Reciprocal(
                    seq.realNumeric().add(BigDecimal.ONE, new Reciprocal(seq.realAdvanced().exp(sum)).buildComputation(seq))).buildComputation(seq);
            return yHat;
        });
    }

}
