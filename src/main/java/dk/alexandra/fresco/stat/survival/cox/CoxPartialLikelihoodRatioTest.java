package dk.alexandra.fresco.stat.survival.cox;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.survival.SurvivalEntry;
import dk.alexandra.fresco.stat.survival.cox.CoxHessian.CoxHessianInternal.State;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoxPartialLikelihoodRatioTest implements Computation<SFixed, ProtocolBuilderNumeric> {

  private final List<SurvivalEntry> data;
  private final List<BigInteger> tiedGroups;
  private final List<DRes<SFixed>> beta;
  private final State fromHessian;

  CoxPartialLikelihoodRatioTest(List<SurvivalEntry> data, List<BigInteger> tiedGroups,
      List<DRes<SFixed>> beta, CoxHessian.CoxHessianInternal.State fromHessian) {
    this.data = data;
    this.tiedGroups = tiedGroups;
    this.beta = beta;
    this.fromHessian = fromHessian;
  }

  @Override
  public DRes<SFixed> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {

      List<DRes<SFixed>> logs = fromHessian.sum2.stream().map(AdvancedFixedNumeric.using(par)::log)
          .collect(
              Collectors.toList());
      return DRes.of(logs);

    }).par((par, logSums) -> {

      List<Double> zero = logPartialLikelihoodAtZero();

      List<DRes<SFixed>> differences = IntStream.range(0, data.size()).mapToObj(i ->
              par.seq(seq -> FixedNumeric.using(seq).add(zero.get(i),
                  FixedNumeric.using(seq).sub(fromHessian.innerProducts.get(i), logSums.get(i)))))
          .collect(Collectors.toList());
      return DRes.of(differences);

    }).seq((seq, differences) -> {

      // Only include terms with status = 1
      List<DRes<SInt>> status = VectorUtils
          .listBuilder(data.size(), j -> data.get(j).getCensored());
      return FixedNumeric.using(seq).mult(2, VectorUtils.innerProductWithBitvector(
              status, differences, seq));

    });
  }

  private List<Double> logPartialLikelihoodAtZero() {

    List<Integer> counts = new ArrayList<>();
    counts.add(1);

    for (int i = 1; i < tiedGroups.size(); i++) {

      int c = counts.get(i-1) + 1;
      counts.add(c);
      if (tiedGroups.get(i-1).equals(tiedGroups.get(i))) {
        for (int j = 1;  j <= i && tiedGroups.get(i - j).equals(tiedGroups.get(i)); j++) {
          counts.set(i-j, c);
        }
      }

    }

    return counts.stream().mapToDouble(Math::log).boxed().collect(Collectors.toList());

  }

}
