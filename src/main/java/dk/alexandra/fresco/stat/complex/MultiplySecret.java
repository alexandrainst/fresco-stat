package dk.alexandra.fresco.stat.complex;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

import java.util.List;

public class MultiplySecret implements Computation<SecretComplex, ProtocolBuilderNumeric> {

    private final SecretComplex z, w;

    public MultiplySecret(SecretComplex z, SecretComplex w) {
        this.z = z;
        this.w = w;
    }

    @Override
    public DRes<SecretComplex> buildComputation(ProtocolBuilderNumeric builder) {
        return builder.par(par -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(par);
            DRes<SFixed> aa = fixedNumeric.mult(z.a, w.a);
            DRes<SFixed> bb = fixedNumeric.mult(z.b, w.b);
            DRes<SFixed> ab = fixedNumeric.mult(z.a, w.b);
            DRes<SFixed> ba = fixedNumeric.mult(z.b, w.a);
            return DRes.of(List.of(aa, bb, ab, ba));
        }).par((par, products) -> {
            FixedNumeric fixedNumeric = FixedNumeric.using(par);
            DRes<SFixed> a = fixedNumeric.sub(products.get(0), products.get(1));
            DRes<SFixed> b = fixedNumeric.add(products.get(2), products.get(3));
            return new SecretComplex(a, b);
        });
    }
}
