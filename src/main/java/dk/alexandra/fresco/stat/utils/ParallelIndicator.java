package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a <i>range</i> and a value where <i>0 &le; value < range</i>this computation returns a
 * list of <i>0</i>'s and <i>1</i>'s such that the <i>value</i>'th entry is the only nonzero
 * value.
 */
public class ParallelIndicator implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final int range;
  private final DRes<SInt> value;

  public ParallelIndicator(int range, DRes<SInt> value) {
    this.range = range;
    this.value = value;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {

    return builder.seq(new PowerList(value, range - 1)).par((par, powers) -> {

      List<DRes<SInt>> indicator = new ArrayList<>();
      for (int filterValue = 0; filterValue < range; filterValue = filterValue + 1) {
        List<BigInteger> polynomial = Indicator
            .computeFilter(range, filterValue, par.getBasicNumericContext().getModulus());
        indicator.add(par.seq(seq -> seq.numeric().add(polynomial.get(0), AdvancedNumeric.using(seq)
            .innerProductWithPublicPart(polynomial.subList(1, polynomial.size()), powers))));
      }
      return DRes.of(indicator);
    });

  }
}
