package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.sampling.SampleBernoulliDistribution;
import dk.alexandra.fresco.stat.sampling.SampleCategoricalDistribution;
import dk.alexandra.fresco.stat.sampling.SampleExponentialDistribution;
import dk.alexandra.fresco.stat.sampling.SampleLaplaceDistribution;
import dk.alexandra.fresco.stat.sampling.SampleNormalDistribution;
import dk.alexandra.fresco.stat.sampling.SampleRademacherDistribution;
import dk.alexandra.fresco.stat.sampling.SampleUniformDistribution;
import java.util.List;

public class DefaultSampler implements Sampler {

  private final ProtocolBuilderNumeric builder;

  DefaultSampler(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<SInt> sampleBernoulliDistribution(DRes<SFixed> p) {
    return new SampleBernoulliDistribution(p).buildComputation(builder);
  }

  @Override
  public DRes<SInt> sampleBernoulliDistribution(double p) {
    return new SampleBernoulliDistribution(p).buildComputation(builder);
  }

  @Override
  public DRes<SInt> sampleCategoricalDistribution(List<DRes<SFixed>> probabilities) {
    return sampleCategoricalDistribution(probabilities, true);
  }

  @Override
  public DRes<SInt> sampleCategoricalDistribution(List<DRes<SFixed>> probabilities,
      boolean normalized) {
    return new SampleCategoricalDistribution(probabilities, normalized).buildComputation(builder);
  }


  @Override
  public DRes<SInt> sampleCategoricalDistribution(double[] probabilities) {
    return new SampleCategoricalDistribution(probabilities).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleExponentialDistribution(DRes<SFixed> b) {
    return new SampleExponentialDistribution(b).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleExponentialDistribution(double b) {
    return new SampleExponentialDistribution(b).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleLaplaceDistribution(double b) {
    return new SampleLaplaceDistribution(b).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleLaplaceDistribution(DRes<SFixed> b) {
    return new SampleLaplaceDistribution(b).buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleNormalDistribution() {
    return new SampleNormalDistribution().buildComputation(builder);
  }

  @Override
  public DRes<SInt> sampleRademacherDistribution() {
    return new SampleRademacherDistribution().buildComputation(builder);
  }

  @Override
  public DRes<SFixed> sampleUniformDistribution() {
    return new SampleUniformDistribution().buildComputation(builder);
  }
}
