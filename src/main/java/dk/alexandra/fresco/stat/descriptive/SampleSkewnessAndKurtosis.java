package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SampleSkewnessAndKurtosis implements
    Computation<Pair<SFixed, SFixed>, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private DRes<SFixed> mean;

  public SampleSkewnessAndKurtosis(List<DRes<SFixed>> data, DRes<SFixed> mean) {
    this.data = data;
    this.mean = mean;
  }

  public SampleSkewnessAndKurtosis(List<DRes<SFixed>> data) {
    this(data, null);
  }

  @Override
  public DRes<Pair<SFixed, SFixed>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {

      // If sample mean is not present, we compute it
      if (mean == null) {
        mean = new SampleMean(data).buildComputation(seq);
      }
      return mean;

    }).par((par, mean) -> {

      // Compute x_i - mean for all data points
      List<DRes<SFixed>> differences = new ArrayList<>();
      for (DRes<SFixed> xi : data) {
        differences.add(FixedNumeric.using(par).sub(xi, mean));
      }
      return DRes.of(differences);

    }).par((par, differences) -> {

      // Compute the squares of all differences...
      return DRes.of(List.of(differences, differences.stream().map(d -> FixedNumeric.using(par).mult(d,d)).collect(
          Collectors.toList())));

    }).par((par, powers) -> {

      // ...and cubes...
      return DRes.of(List.of(powers.get(0), powers.get(1),
          IntStream.range(0, data.size()).mapToObj(i -> FixedNumeric.using(par).mult(powers.get(0).get(i),
          powers.get(1).get(i))).collect(Collectors.toList())));

    }).par((par, powers) -> {

      // ...and cubes...
      return DRes.of(List.of(powers.get(0), powers.get(1), powers.get(2),
          IntStream.range(0, data.size()).mapToObj(i -> FixedNumeric.using(par).mult(powers.get(1).get(i),
              powers.get(1).get(i))).collect(Collectors.toList())));

    }).par((par, powers) ->
        DRes.of(powers.stream().map(AdvancedFixedNumeric.using(par)::sum).collect(Collectors.toList()))
    ).par((par, sums) ->
        DRes.of(sums.stream().map(sum -> FixedNumeric.using(par).mult(1.0 / data.size(), sum)).collect(Collectors.toList()))
    ).pairInPar((seq, sums) -> {

      // Skewness
      DRes<SFixed> numerator = sums.get(2);
      DRes<SFixed> sqrt = AdvancedFixedNumeric.using(seq).sqrt(sums.get(1));
      DRes<SFixed> denominator = FixedNumeric.using(seq)
          .mult(sqrt, FixedNumeric.using(seq).mult(sqrt, sqrt));
      return FixedNumeric.using(seq).div(numerator, denominator);

    }, (seq, sums) -> {

      // Kurtosis
      DRes<SFixed> numerator = sums.get(3);
      DRes<SFixed> denominator = FixedNumeric.using(seq).mult(sums.get(1), sums.get(1));
      return FixedNumeric.using(seq).div(numerator, denominator);

    });
  }

}
