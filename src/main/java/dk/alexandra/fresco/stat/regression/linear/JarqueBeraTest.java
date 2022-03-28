package dk.alexandra.fresco.stat.regression.linear;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;

public class JarqueBeraTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final int n;
  private final DRes<SFixed> skewness;
  private final DRes<SFixed> kurtosis;

  public JarqueBeraTest(int n, DRes<SFixed> skewness, DRes<SFixed> kurtosis) {
    this.n = n;
    this.skewness = skewness;
    this.kurtosis = kurtosis;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(sub -> {
      DRes<SFixed> sSquared = FixedNumeric
          .using(sub).mult(skewness,
              skewness);
      DRes<SFixed> kSquared = sub.seq(seq -> {
        DRes<SFixed> kMinusThree = FixedNumeric.using(seq).sub(kurtosis, 3);
        return FixedNumeric.using(seq).mult(kMinusThree, kMinusThree);
      });
      return Pair.lazy(sSquared, kSquared);
    }).seq((seq, sAndK) -> {
      FixedNumeric f = FixedNumeric.using(seq);
      return f.mult(n / 6.0, f.add(sAndK.getFirst(), f.mult(0.25, sAndK.getSecond())));
    });
  }
}
