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

public class SampleSkewness implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<DRes<SFixed>> data;
  private DRes<SFixed> mean;

  public SampleSkewness(List<DRes<SFixed>> data, DRes<SFixed> mean) {
    this.data = data;
    this.mean = mean;
  }

  public SampleSkewness(List<DRes<SFixed>> data) {
    this(data, null);
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      if (mean == null) {
        mean = new SampleMean(data).buildComputation(seq);
      }
      return mean;
    }).par((par, mean) -> {
          List<DRes<SFixed>> differences = new ArrayList<>();
          for (DRes<SFixed> xi : data) {
            differences.add(FixedNumeric.using(par).sub(xi, mean));
          }
          return DRes.of(differences);
        }).par((par, differences) -> {
      List<DRes<SFixed>> squares = new ArrayList<>();
      for (DRes<SFixed> di : differences) {
        squares.add(FixedNumeric.using(par).mult(di, di));
      }
      return Pair.lazy(squares, differences);
    }).par((par, squaresAndDifferences) -> {
      List<DRes<SFixed>> cubes = new ArrayList<>();
      for (int i = 0; i < data.size(); i++) {
        cubes.add(FixedNumeric.using(par).mult(squaresAndDifferences.getSecond().get(i), squaresAndDifferences.getFirst().get(i)));
      }
      return Pair.lazy(squaresAndDifferences.getFirst(), cubes);
    }).par((par, squaresAndCubes) -> {
      DRes<SFixed> sumOfSquares = AdvancedFixedNumeric.using(par).sum(squaresAndCubes.getFirst());
      DRes<SFixed> sumOfCubes = AdvancedFixedNumeric.using(par).sum(squaresAndCubes.getSecond());
      return Pair.lazy(sumOfSquares, sumOfCubes);
    }).par((par, sums) -> {
      DRes<SFixed> numerator = FixedNumeric.using(par).mult(1.0 / data.size(), sums.getSecond());
      DRes<SFixed> denominator = par.seq(seq -> FixedNumeric.using(seq).mult(1.0 / data.size(), sums.getFirst()))
          .seq((seq, normalized) -> AdvancedFixedNumeric.using(seq).sqrt(normalized))
          .seq((seq, sqrt) -> FixedNumeric.using(seq).mult(sqrt, FixedNumeric.using(seq).mult(sqrt, sqrt)));
      return Pair.lazy(numerator, denominator);
    }).seq((seq, fraction) -> FixedNumeric.using(seq).div(fraction.getFirst(), fraction.getSecond()));
  }

}
