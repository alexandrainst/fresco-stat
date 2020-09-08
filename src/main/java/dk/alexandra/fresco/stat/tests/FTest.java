package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.helpers.USS;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compute the F-test for equal mean (one-way-anova) for the given data sets.
 */
public class FTest implements Computation<SReal, ProtocolBuilderNumeric> {

  private List<List<DRes<SReal>>> observed;

  public FTest(List<List<DRes<SReal>>> observed) {
    this.observed = observed;
  }

  @Override
  public DRes<SReal> buildComputation(ProtocolBuilderNumeric builder) {
    int n = observed.stream().map(List::size).mapToInt(Integer::intValue).sum();
    int f1 = n - observed.size(); // degrees of freedom

    return builder.par(par -> {
      List<DRes<SReal>> sums = new ArrayList<>();
      List<DRes<SReal>> uss = new ArrayList<>();
      for (List<DRes<SReal>> sample : observed) {
        sums.add(par.realAdvanced().sum(sample));
        uss.add(new USS(sample).buildComputation(par));
      }

      return Pair.lazy(sums, uss);
    }).par((par, values) -> {
      List<DRes<Pair<DRes<SReal>, DRes<SReal>>>> ratiosAndSsds = new ArrayList<>();

      for (int i = 0; i < observed.size(); i++) {
        int finalI = i;
        DRes<Pair<DRes<SReal>, DRes<SReal>>> ratioAndSsd = par.seq(subSeq -> {
          DRes<SReal> ratio = subSeq.realNumeric().mult(values.getFirst().get(finalI), values.getFirst().get(finalI));
          ratio = subSeq.realNumeric().div(ratio, observed.get(finalI).size());

          DRes<SReal> ssd = subSeq.realNumeric().sub(values.getSecond().get(finalI), ratio);
          return Pair.lazy(ratio, ssd);
        });
        ratiosAndSsds.add(ratioAndSsd);
      }
      return Pair.lazy(values.getFirst(), ratiosAndSsds);

    }).par((par, values) -> {
      List<DRes<SReal>> ratios = values.getSecond().stream().map(DRes::out).map(Pair::getFirst)
          .collect(
              Collectors.toList());
      List<DRes<SReal>> ssds = values.getSecond().stream().map(DRes::out).map(Pair::getSecond)
          .collect(
              Collectors.toList());
      DRes<SReal> ssd1 = par.realAdvanced().sum(ssds);
      DRes<SReal> sumOfRatios = par.realAdvanced().sum(ratios);
      DRes<SReal> sum = par.realAdvanced().sum(values.getFirst());
      return () -> List.of(ssd1, sumOfRatios, sum);
    }).par((par, values) -> {

      DRes<SReal> s2 = par.seq(subSeq -> {
        DRes<SReal> ssd2 = subSeq.realNumeric().mult(values.get(2), values.get(2));
        ssd2 = subSeq.realNumeric().div(ssd2, n);
        ssd2 = subSeq.realNumeric().sub(values.get(1), ssd2);
        return subSeq.realNumeric().div(ssd2, observed.size() - 1);
      });

      DRes<SReal> s1 = par.seq(subSeq -> subSeq.realNumeric().div(values.get(0), f1));

      return Pair.lazy(s1,s2);
    }).seq((seq, values) -> seq.realNumeric().div(values.getSecond(), values.getFirst()));
  }

}
