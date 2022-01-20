package dk.alexandra.fresco.stat.filtered;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.DivideBySInt;
import dk.alexandra.fresco.stat.filtered.helpers.SSDFiltered;
import java.util.List;
import java.util.Optional;

/**
 * Compute the sample variance of a set of samples. The input is <i>filtered</i>, meaning that besides
 * the data set, the input also consists of a secret 0-1-vector (a <code>filter</code>) which
 * indicates what entries of the data set should be included in the analysis
 */
public class SampleVarianceFiltered implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> observed;
  private final List<DRes<SInt>> filter;
  private final Optional<DRes<SFixed>> mean;

  public SampleVarianceFiltered(List<DRes<SFixed>> observed, List<DRes<SInt>> filter) {
    this(observed, Optional.empty(), filter);
  }

  public SampleVarianceFiltered(List<DRes<SFixed>> observed, DRes<SFixed> mean, List<DRes<SInt>> filter) {
    this(observed, Optional.of(mean), filter);
  }

  private SampleVarianceFiltered(List<DRes<SFixed>> observed, Optional<DRes<SFixed>> mean, List<DRes<SInt>> filter) {
    this.observed = observed;
    this.mean = mean;
    this.filter = filter;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(par -> {
      DRes<SFixed> sum = mean.isPresent() ? new SSDFiltered(observed, mean.get(), filter).buildComputation(par) : new SSDFiltered(observed, filter).buildComputation(par);
      DRes<SInt> n = AdvancedNumeric.using(par).sum(filter);
      return Pair.lazy(sum, n);
    }).seq((seq, sumAndN) -> new DivideBySInt(sumAndN.getFirst(), seq.numeric().sub(sumAndN.getSecond(), 1)).buildComputation(seq));
  }

}
