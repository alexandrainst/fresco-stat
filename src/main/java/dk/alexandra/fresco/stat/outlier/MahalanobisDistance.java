package dk.alexandra.fresco.stat.outlier;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.descriptive.SampleCovariance;
import dk.alexandra.fresco.stat.linearalgebra.MoorePenrosePseudoInverse;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MahalanobisDistance implements Computation<ArrayList<DRes<SFixed>>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SFixed>>> X;
  private final List<DRes<SFixed>> mean;

  public MahalanobisDistance(List<List<DRes<SFixed>>> X, List<DRes<SFixed>> mean) {
    this.X = X;
    this.mean = mean;
  }

  @Override
  public DRes<ArrayList<DRes<SFixed>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq ->
      seq.seq(new SampleCovariance(X, mean))
    ).seq((seq, S) ->
      seq.seq(new MoorePenrosePseudoInverse(S))
    ).par((par, Sinv) -> {
      ArrayList<DRes<SFixed>> result = new ArrayList<>();
      for (int i = 0; i < X.size(); i++) {
        int finalI = i;
        DRes<SFixed> d = par.seq(seq -> {
          ArrayList<DRes<SFixed>> diff = VectorUtils.sub(getIthObservation(finalI), mean, seq);
          return Pair
              .lazy(FixedLinearAlgebra.using(seq).vectorMult(DRes.of(Sinv), DRes.of(diff)), diff);
        }).seq((seq, rightDiff) -> AdvancedFixedNumeric.using(seq)
            .innerProduct(rightDiff.getFirst().out(), rightDiff.getSecond()));
        result.add(d);
      }

      return DRes.of(result);
    });
  }

  private List<DRes<SFixed>> getIthObservation(int i) {
    return X.stream().map(x -> x.get(i)).collect(Collectors.toList());
  }

}
