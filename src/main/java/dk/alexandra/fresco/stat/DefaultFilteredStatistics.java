package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.anonymisation.LeakyKAnonymity;
import dk.alexandra.fresco.stat.filtered.FilteredResult;
import dk.alexandra.fresco.stat.filtered.HistogramFiltered;
import dk.alexandra.fresco.stat.filtered.OneSampleTTestFiltered;
import dk.alexandra.fresco.stat.filtered.SampleMeanFiltered;
import dk.alexandra.fresco.stat.filtered.SampleVarianceFiltered;
import dk.alexandra.fresco.stat.utils.MatrixUtils;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultFilteredStatistics implements FilteredStatistics {

  private final ProtocolBuilderNumeric builder;

  DefaultFilteredStatistics(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SFixed> sampleMean(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    return new SampleMeanFiltered(data, filter).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, DRes<SFixed> mean,
      List<DRes<SInt>> filter) {
    return new SampleVarianceFiltered(data, mean, filter).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, List<DRes<SInt>> filter) {
    return new SampleVarianceFiltered(data, filter).buildComputation(builder);
  }

  @Override
  public DRes<FilteredResult> ttest(List<DRes<SFixed>> data, DRes<SFixed> mu, List<DRes<SInt>> filter) {
    return new OneSampleTTestFiltered(data, mu, filter).buildComputation(builder);
  }

  @Override
  public DRes<List<DRes<SInt>>> histogram(List<DRes<SInt>> buckets, List<DRes<SInt>> data,
      List<DRes<SInt>> filter) {
    return new HistogramFiltered(buckets, data, filter).buildComputation(builder);
  }

  @Override
  public DRes<MultiDimensionalArray<List<DRes<SInt>>>> kAnonymize(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes, List<List<DRes<SInt>>> buckets, int k,
      List<DRes<SInt>> filter) {
    // The shuffle method expects a matrix whose height is a two power. If this is not the case, we
    // pad with zero rows and put an extra indicator attribute to be able to remove them afterwards.

    // Smallest representable value
    BigInteger paddingValue = BigInteger.valueOf(0);
    BigInteger paddingSensitive = BigInteger.valueOf(0);

    // Distance to next power of two
    int padding = Integer.bitCount(data.getHeight()) == 1 ? 0
        : (Integer.highestOneBit(data.getHeight()) << 1) - data.getHeight();

    return builder.par(par -> {

      // Create a matrix consisting of quasi identifiers, excluded indicator and sensitive attributes
      // | quasi identifiers | excluded | sensitive |
      ArrayList<ArrayList<DRes<SInt>>> rows = new ArrayList<>();

      for (int i = 0; i < data.getHeight(); i++) {
        ArrayList<DRes<SInt>> row = new ArrayList<>(data.getRow(i));

        int finalI = i;
        row.add(par.seq(seq -> seq.numeric().sub(1, filter.get(finalI))));
        row.add(par.seq(seq -> seq.numeric().mult(filter.get(finalI), sensitiveAttributes.get(finalI))));
        rows.add(row);
      }

      // Filtered and padding values are excluded
      for (int i = 0; i < padding; i++) {
        ArrayList<DRes<SInt>> row = IntStream.range(0, data.getWidth())
            .mapToObj(j -> par.numeric().known(paddingValue))
            .collect(Collectors.toCollection(ArrayList::new));
        row.add(par.numeric().known(1)); // Add padding indicator attribute
        row.add(par.numeric().known(paddingSensitive));
        rows.add(row);
      }
      return DRes.of(new Matrix<>(rows.size(), rows.get(0).size(), rows));
    }).seq((seq, columnVector) -> {
      // Shuffle the matrix we created above
      return Collections.using(seq).shuffle(DRes.of(columnVector));
    }).seq((seq, shuffledMatrix) -> {
      // Extract matrix of quasi-identifiers and list of sensitive attributes from shuffled matrix
      Matrix<DRes<SInt>> shuffledData = MatrixUtils
          .subMatrix(shuffledMatrix, 0, shuffledMatrix.getHeight(), 0,
              shuffledMatrix.getWidth() - 1);
      List<DRes<SInt>> shuffledSensitive = shuffledMatrix.getColumn(shuffledMatrix.getWidth() - 1);
      List<List<DRes<SInt>>> bucketsWithIndicator = new ArrayList<>(buckets);
      bucketsWithIndicator.add(List.of(seq.numeric().known(0)));

      // Run "leaky" k-anonymity algorithm on these inputs. Indices are
      return seq.seq(new LeakyKAnonymity(shuffledData, shuffledSensitive, bucketsWithIndicator, k));
    }).par((par, kAnonymousData) -> {
      MultiDimensionalArray<List<DRes<SInt>>> uncorrected = kAnonymousData.project(l -> l.get(0));
      return DRes.of(uncorrected);
    });
  }

  @Override
  public DRes<MultiDimensionalArray<List<BigInteger>>> kAnonymizeAndOpen(Matrix<DRes<SInt>> data,
      List<DRes<SInt>> sensitiveAttributes, List<List<DRes<SInt>>> buckets, int k,
      List<DRes<SInt>> filter) {
    return builder
        .seq(seq -> FilteredStatistics.using(seq).kAnonymize(data, sensitiveAttributes, buckets, k, filter))
        .par((par, kAnonymousData) -> DRes.of(kAnonymousData.map(x -> VectorUtils.open(x, par))))
        .par((par, kAnonymousData) -> DRes.of(kAnonymousData.map(h -> h.stream().map(DRes::out)
            .filter(x -> !x.equals(BigInteger.ZERO)).collect(Collectors.toList()))));
  }

}