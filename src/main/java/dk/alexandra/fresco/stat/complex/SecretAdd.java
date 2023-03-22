package dk.alexandra.fresco.stat.complex;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;

public class SecretAdd implements Computation<SecretComplex, ProtocolBuilderNumeric> {

    private final SecretComplex z, w;

    public SecretAdd(SecretComplex z, SecretComplex w) {
        this.z = z;
        this.w = w;
    }

    @Override
    public DRes<SecretComplex> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.par(par -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(par);
            return new SecretComplex(fixedNumeric.add(z.a, w.a), fixedNumeric.add(z.b, w.b));
        });
    }
}
