package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MultiDimensionalHistogram
    implements Computation<MultiDimensionalArray<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<List<DRes<SInt>>> buckets;
  private final Matrix<DRes<SInt>> data;
  private final int dimensions;

  public MultiDimensionalHistogram(List<List<DRes<SInt>>> buckets,
      Matrix<DRes<SInt>> data) {
    if (data.getWidth() != buckets.size()) {
      throw new IllegalArgumentException("There should be a list of buckets per dimension");
    }
    this.buckets = buckets;
    this.dimensions = buckets.size();
    this.data = data;
  }

  /**
   * Return all subsets of a list
   */
  private static <E> Stream<List<E>> subsets(List<E> set) {
    return subsets(set.size()).map(subset -> sublist(set, subset));
  }

  /**
   * Return a list containing the given indices from the input list in the same order.
   */
  private static <E> List<E> sublist(List<E> list, int[] indices) {
    return Arrays.stream(indices).mapToObj(list::get).collect(Collectors.toList());
  }

  /**
   * Return all subsets of {0,1,2,...,r-1} lexicographical order.
   */
  private static Stream<int[]> subsets(int r) {

    Stream<int[]> stream = Stream.of(new int[0]);
    if (r == 0) {
      return stream;
    }

    return Stream.concat(stream,
        IntStream.range(0, r).boxed().flatMap(
            i -> subsets(r - i - 1).map(set -> Arrays.stream(set).map(x -> x + i + 1).toArray())
                .map(s -> prepend(i, s))));
  }

  /**
   * Return a new array from prepending <i>a</i> to the given list.
   */
  private static int[] prepend(int a, int[] list) {
    int[] out = new int[list.length + 1];
    out[0] = a;
    System.arraycopy(list, 0, out, 1, list.length);
    return out;
  }

  @Override
  public DRes<MultiDimensionalArray<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {

      // Perform all necessary comparisons (one per value per bucket) in parallel
      Matrix<DRes<List<DRes<SInt>>>> indicators = MatrixUtils.buildMatrix(data.getHeight(),
          data.getWidth(),
          (i, j) -> new MultiLEQ(data.getRow(i).get(j), buckets.get(j)).buildComputation(par));
      return DRes.of(indicators);

    }).par((par, indicators) -> {

      // Compute the histogram x the dataset -- a 1 indicates the data point is in the
      // corresponding cumulative histogram.
      List<Integer> widths = Stream
          .concat(buckets.stream().mapToInt(bucket -> bucket.size() + 1).boxed(),
              Stream.of(data.getHeight())).collect(Collectors.toList());

      Matrix<List<DRes<SInt>>> indicatorsOut = MatrixUtils.map(indicators, DRes::out);
      MultiDimensionalArray<DRes<SInt>> indicatorHistogram = MultiDimensionalArray
          .build(widths, i -> {
            int row = i.get(i.size() - 1);
            List<DRes<SInt>> factors = IntStream.range(0, dimensions).mapToObj(d ->
                indicatorsOut.getRow(row).get(d).get(i.get(d))).collect(
                Collectors.toList());
            return AdvancedNumeric.using(par).product(factors);
          });
      return DRes.of(indicatorHistogram);

    }).par((par, indicatorHistogram) -> {

      // Add all indicators per datapoint to compute the cumulative histogram.
      AdvancedNumeric advancedNumeric = AdvancedNumeric.using(par);
      MultiDimensionalArray<DRes<SInt>> cumulativeHistogram = indicatorHistogram
          .project(advancedNumeric::sum);
      return DRes.of(cumulativeHistogram);

    }).par((par, cumulativeHistogram) -> {

      // Correct the cumulative histogram to the actual histogram
      MultiDimensionalArray<DRes<SInt>> histogram = MultiDimensionalArray
          .build(cumulativeHistogram.getShape(), i -> par.seq(seq -> {
            AtomicReference<DRes<SInt>> value = new AtomicReference<>(cumulativeHistogram.get(i));

            List<Integer> nonZeroIndices = IntStream.range(0, dimensions).filter(d -> i.get(d) > 0)
                .boxed()
                .collect(
                    Collectors.toList());
            if (!nonZeroIndices.isEmpty()) {
              subsets(nonZeroIndices).filter(subset -> !subset.isEmpty()).forEach(subset -> {
                List<Integer> x = IntStream.range(0, dimensions)
                    .map(d -> subset.contains(d) ? i.get(d) - 1 : i.get(d))
                    .boxed().collect(Collectors.toList());

                if (Math.floorMod(subset.size(), 2) == 1) {
                  value.set(seq.numeric().sub(value.get(), cumulativeHistogram.get(x)));
                } else {
                  value.set(seq.numeric().add(value.get(), cumulativeHistogram.get(x)));
                }
              });
            }
            return value.get();
          }));
      return DRes.of(histogram);
    });
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
