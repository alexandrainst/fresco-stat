package dk.alexandra.fresco.stat.filtered;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.filtered.helpers.SumFiltered;
import dk.alexandra.fresco.stat.utils.DivideBySInt;
import java.util.List;

/**
 * Compute the sample mean of a set of samples. The input is <i>filtered</i>, meaning that besides
 * the data set, the input also consists of a secret 0-1-vector (a <code>filter</code>) which
 * indicates what entries of the data set should be included in the analysis
 */
public class SampleMeanFiltered implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final List<DRes<SInt>> filter;

  public SampleMeanFiltered(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    this.data = data;
    this.filter = filter;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<SFixed> s = new SumFiltered(data, filter).buildComputation(par);
      DRes<SInt> n = AdvancedNumeric.using(par).sum(filter);
      return Pair.lazy(s, n);
    }).seq((seq, sn) -> new DivideBySInt(sn.getFirst(), sn.getSecond()).buildComputation(seq));
  }
}
