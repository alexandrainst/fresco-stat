package dk.alexandra.fresco.stat.filtered.helpers;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.helpers.USS;
import dk.alexandra.fresco.stat.filtered.SampleMeanFiltered;
import dk.alexandra.fresco.stat.filtered.helpers.USSFiltered;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SSDFiltered implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private final List<DRes<SInt>> filter;
  private final Optional<DRes<SFixed>> mean;

  public SSDFiltered(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    this(data, Optional.empty(), filter);
  }

  public SSDFiltered(List<DRes<SFixed>> data, DRes<SFixed> mean, List<DRes<SInt>> filter) {
    this(data, Optional.of(mean), filter);
  }

  private SSDFiltered(List<DRes<SFixed>> data, Optional<DRes<SFixed>> mean, List<DRes<SInt>> filter) {
    this.data = data;
    this.filter = filter;
    this.mean = mean;
  }


  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(seq -> mean
        .orElseGet(() -> new SampleMeanFiltered(data, filter).buildComputation(seq))
    ).par((par, mean) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      List<DRes<SFixed>> terms = data.stream().map(x -> fixedNumeric.sub(x, mean))
          .collect(Collectors.toList());
      return DRes.of(terms);
    }).seq((seq, terms) -> seq.seq(new USSFiltered(terms, filter)));
  }

}