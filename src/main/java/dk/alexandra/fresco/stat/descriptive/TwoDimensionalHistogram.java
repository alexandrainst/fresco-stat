package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.stat.linearalgebra.MatrixUtils;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import java.util.List;

public class TwoDimensionalHistogram
    implements Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets;
  private final List<Pair<DRes<SInt>, DRes<SInt>>> data;

  public TwoDimensionalHistogram(Pair<List<DRes<SInt>>, List<DRes<SInt>>> buckets,
      List<Pair<DRes<SInt>, DRes<SInt>>> data) {
    this.buckets = buckets;
    this.data = data;
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    int n = data.size();
    int w = buckets.getSecond().size() + 1;
    int h = buckets.getFirst().size() + 1;

    return builder.par(par -> {
      MultiDimensionalArray<DRes<SInt>> counts =
          MultiDimensionalArray.build(List.of(w, h, n), l -> {
            int i = l.get(0);
            int j = l.get(1);
            int k = l.get(2);

            if (i == w - 1 && j == h - 1) {
              return par.numeric().known(1);
            } else if (i == w - 1) {
              return Comparison.using(par).compareLEQ(data.get(k).getFirst(),
                  buckets.getSecond().get(j));
            } else if (j == h - 1) {
              return Comparison.using(par).compareLEQ(data.get(k).getSecond(),
                  buckets.getFirst().get(i));
            } else {
              return new LEQPair(data.get(k).getFirst(), buckets.getSecond().get(j),
                  data.get(k).getSecond(), buckets.getFirst().get(i)).buildComputation(par);
            }
          });
      return () -> counts;
    }).par((par, counts) -> {
      MultiDimensionalArray<DRes<SInt>> sums = counts.project(r -> AdvancedNumeric.using(par).sum(r));
      return () -> sums;
    }).par((par, sums) -> {
      Matrix<DRes<SInt>> histogram = MatrixUtils.buildMatrix(h, w, (i, j) -> {
        if (i == 0 && j == 0) {
          return sums.get(0, 0);
        } else if (i == 0) {
          return par.numeric().sub(sums.get(i, j), sums.get(i, j - 1));
        } else if (j == 0) {
          return par.numeric().sub(sums.get(i, j), sums.get(i - 1, j));
        }
        return par.numeric().sub(par.numeric().add(sums.get(i, j), sums.get(i - 1, j - 1)),
            par.numeric().add(sums.get(i - 1, j), sums.get(i, j - 1)));
      });
      return () -> histogram;
    });
  }

  private class LEQPair implements Computation<SInt, ProtocolBuilderNumeric> {

    private DRes<SInt> a1, a2, b1, b2;

    private LEQPair(DRes<SInt> a1, DRes<SInt> a2, DRes<SInt> b1, DRes<SInt> b2) {
      this.a1 = a1;
      this.a2 = a2;
      this.b1 = b1;
      this.b2 = b2;
    }

    @Override
    public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.par(par -> {
        Comparison comparison = Comparison.using(par);
        DRes<SInt> c1 = comparison.compareLEQ(a1, a2);
        DRes<SInt> c2 = comparison.compareLEQ(b1, b2);
        return Pair.lazy(c1, c2);
      }).seq((seq, c) -> seq.numeric().mult(c.getFirst(), c.getSecond()));
    }

  }
}
