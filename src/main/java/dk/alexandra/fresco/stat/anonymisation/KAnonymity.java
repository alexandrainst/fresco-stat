package dk.alexandra.fresco.stat.anonymisation;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KAnonymity implements
    Computation<MultiDimensionalArray<List<DRes<SInt>>>, ProtocolBuilderNumeric> {

  private final Matrix<DRes<SInt>> data;
  private final List<DRes<SInt>> sensitive;
  private final List<List<DRes<SInt>>> buckets;
  private final int k;
  private final int dimensions;

  /**
   * Each row in the data set are the quasi-identifiers of an individual with a corresponding entry
   * in the list of values of the sensitive attribute. The buckets indicates the desired generalization
   * of the quasi-identifiers as in a histogram. K is the smallest allowed number of individuals in each bucket.
   * <p>
   * The output is a histogram on the given buckets with the value in the histogram being a list
   * of size data.getHeight() with a non-zero entry <i>x</i> at index <i>i</i> indicating that the data
   * point at row <i>i</i> is in this bucket and that the corresponding sensitive attribute was <i>x</i>.
   * <p>
   * If the corresponding indices should not be leaked, the output should be shuffled or sorted
   * before opening the result.
   */
  public KAnonymity(Matrix<DRes<SInt>> data, List<DRes<SInt>> sensitive,
      List<List<DRes<SInt>>> buckets, int k) {
    if (data.getHeight() != sensitive.size()) {
      throw new IllegalArgumentException("There must be a sensitive attribute per data point");
    }
    this.data = data;
    this.sensitive = sensitive;
    this.buckets = buckets;
    this.dimensions = buckets.size();
    this.k = k;
  }

  @Override
  public DRes<MultiDimensionalArray<List<DRes<SInt>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    return builder.par(par -> {

      // Perform all necessary comparisons (one per value per bucket) in parallel
      Matrix<DRes<List<DRes<SInt>>>> indicators = MatrixUtils.buildMatrix(data.getHeight(),
          data.getWidth(),
          (i, j) -> new MultiLEQ(data.getRow(i).get(j), buckets.get(j)).buildComputation(par));
      return DRes.of(indicators);

    }).par((par, indicators) -> {

      // Compute the histogram x the dataset -- a 1 indicates the data point is in the
      // corresponding cumulative histogram.
      List<Integer> widths = buckets.stream().mapToInt(bucket -> bucket.size() + 1).boxed()
          .collect(Collectors.toList());

      Matrix<List<DRes<SInt>>> indicatorsOut = MatrixUtils.map(indicators, DRes::out);
      MultiDimensionalArray<List<DRes<SInt>>> indicatorHistogram = MultiDimensionalArray
          .build(widths, i -> IntStream.range(0, data.getHeight()).mapToObj(j ->
              AdvancedNumeric.using(par).product(IntStream.range(0, dimensions).mapToObj(d ->
                  indicatorsOut.getRow(j).get(d).get(i.get(d))).collect(
                  Collectors.toList()))).collect(Collectors.toList()));
      return DRes.of(indicatorHistogram);
    }).par((par, cumulativeHistogram) -> {

      // Compute indicator histogram -- for each bucket we get an indicator vector of size data.size()
      // with a 1 at index i indicating that the i'th data point is in the corresponding bucket.
      MultiDimensionalArray<DRes<List<DRes<SInt>>>> histogram = MultiDimensionalArray
          .build(cumulativeHistogram.getWidths(), i -> par.seq(seq -> {
            List<DRes<SInt>> value = cumulativeHistogram.get(i);
            for (int j = 0; j < dimensions; j++) {
              if (i.get(j) == 0) {
                continue;
              }
              List<Integer> iPrime = new ArrayList<>(i);
              iPrime.set(j, i.get(j) - 1);
              value = VectorUtils
                  .mult(value, VectorUtils.negate(cumulativeHistogram.get(iPrime), seq), seq);
            }
            return DRes.of(value);
          }));
      DRes<SInt> secretK = par.numeric().known(k);
      return Pair.lazy(histogram, secretK);
    }).par((par, histogramAndK) -> {
      //Make histogram k-anonymous by suppressing all buckets with less than k members
      MultiDimensionalArray<DRes<List<DRes<SInt>>>> kAnonymousHistogram = histogramAndK.getFirst()
          .map(b -> par.seq(seq -> {
            DRes<SInt> sum = AdvancedNumeric.using(seq).sum(b.out());
            DRes<SInt> indicator = Comparison.using(seq).compareLEQ(histogramAndK.getSecond(), sum);
            return DRes.of(VectorUtils.scaleInt(b.out(), indicator, seq));
          }));
      return DRes.of(kAnonymousHistogram);
    }).par((par, kAnonymousHistogram) -> DRes
        .of(kAnonymousHistogram.map(b -> VectorUtils.mult(b.out(), sensitive, par))));
  }

  private static class MultiLEQ implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

    private final DRes<SInt> lhs;
    private final List<DRes<SInt>> rhs;

    private MultiLEQ(DRes<SInt> lhs, List<DRes<SInt>> rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.par(par -> {
        Comparison comparison = Comparison.using(par);
        List<DRes<SInt>> c = Stream.concat(rhs.stream()
            .map(x -> comparison.compareLEQ(lhs, x)), Stream.of(par.numeric().known(1))).collect(
            Collectors.toList());
        return DRes.of(c);
      });
    }

  }

}
