package dk.alexandra.fresco.stat.complex;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;

import java.math.BigDecimal;

public class Open implements Computation<OpenComplex, ProtocolBuilderNumeric> {

    private final SecretComplex z;

    public Open(SecretComplex z) {
        this.z = z;
    }

    @Override
    public DRes<OpenComplex> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.par(par -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(par);
            DRes<BigDecimal> a = fixedNumeric.open(z.a);
            DRes<BigDecimal> b = fixedNumeric.open(z.b);
            return Pair.lazy(a, b);
        }).seq((seq, w) -> new OpenComplex(w.getFirst().out().doubleValue(), w.getSecond().out().doubleValue()));
    }
}
