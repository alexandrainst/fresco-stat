package dk.alexandra.fresco.stat.complex;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

import java.math.BigDecimal;

public class SecretComplex implements DRes<SecretComplex> {

    public final DRes<SFixed> a, b;


    public static SecretComplex fromReal(DRes<SFixed> a, ProtocolBuilderNumeric builder) {
        return new SecretComplex(a, FixedNumeric.using(builder).known(BigDecimal.ZERO));
    }

    public static DRes<SecretComplex> fromReal(BigDecimal a, ProtocolBuilderNumeric builder) {
        return builder.par(par ->
            Pair.lazy(FixedNumeric.using(par).known(a),
                    FixedNumeric.using(par).known(0))).seq((seq, values) -> new SecretComplex(values.getFirst(), values.getSecond()));
    }


    public SecretComplex(DRes<SFixed> a, DRes<SFixed> b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public SecretComplex out() {
        return this;
    }
}
