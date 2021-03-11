package dk.alexandra.fresco.stat.anonymisation;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.common.compare.Comparison;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.Sampler;
import dk.alexandra.fresco.stat.Statistics;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoisyHistogram implements Computation<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> buckets;
  private final List<DRes<SInt>> data;
  private final double epsilon;

  /**
   * Given a list of upper bounds for buckets and a list of samples, this computation computes a differentially private
   * histogram for the given buckets. The last bucket contains all samples larger than the last
   * upper bound.
   *
   * @param buckets Soft upper bounds for buckets
   * @param data    List of samples
   * @param epsilon The privacy parameter
   */
  public NoisyHistogram(List<DRes<SInt>> buckets, List<DRes<SInt>> data, double epsilon) {
    this.buckets = buckets;
    this.data = data;
    this.epsilon = epsilon;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<List<DRes<SInt>>> histogram = Statistics.using(par).histogramDiscrete(buckets, data);
      List<DRes<SFixed>> noise = new ArrayList<>();
      Sampler sampler = Sampler.using(par);
      for (int i = 0; i < buckets.size() + 1; i++) {
        noise.add(sampler.sampleLaplaceDistribution(1.0 / epsilon));
      }
      return Pair.lazy(histogram, noise);
    }).par((par, histogramAndNoise) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      List<DRes<SFixed>> noisePlusHalf = histogramAndNoise.getSecond().stream().map(x -> fixedNumeric.add(
          0.5, x)).collect(Collectors.toList());
      return Pair.lazy(histogramAndNoise.getFirst().out(), noisePlusHalf);
    }).par((par, histogramAndNoise) -> {
      AdvancedFixedNumeric fixedNumeric = AdvancedFixedNumeric.using(par);
      List<DRes<SInt>> noiseRounded = histogramAndNoise.getSecond().stream().map(fixedNumeric::floor).collect(
          Collectors.toList());
      return Pair.lazy(histogramAndNoise.getFirst(), noiseRounded);
    }).par((par, histogramAndNoise) -> {
      List<DRes<SInt>> noisyHistogram = new ArrayList<>();
      for (int i = 0; i < histogramAndNoise.getFirst().size(); i++) {
        noisyHistogram.add(par.numeric().add(histogramAndNoise.getFirst().get(i), histogramAndNoise.getSecond().get(i)));
      }
      return DRes.of(noisyHistogram);
    });
  }

}
