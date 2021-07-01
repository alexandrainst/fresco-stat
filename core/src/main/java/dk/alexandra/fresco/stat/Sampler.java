package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

/** This computation library contains functions which samples random values from various distributions. */
public interface Sampler {

  static Sampler using(ProtocolBuilderNumeric builder) {
    return new DefaultSampler(builder);
  }

  /**
   * Draw a sample from a Bernoulli distribution with parameter <i>p</i> with <i>0 &le; p &le;
   * 1</i>.
   */
  DRes<SInt> sampleBernoulliDistribution(DRes<SFixed> p);

  /**
   * Draw a sample from a Bernoulli distribution with parameter <i>p</i> with <i>0 &le; p &le;
   * 1</i>.
   */
  DRes<SInt> sampleBernoulliDistribution(double p);

  /**
   * Draw a sample from the set <i>{0, ..., probabilities.size() - 1}</i> with
   * <i>probabilities.get(i)</i> indicating the probability of drawing <i>i</i>. Note that the sum
   * of probabilities should be equal to 1.
   */
  DRes<SInt> sampleCategoricalDistribution(List<DRes<SFixed>> probabilities);

  /**
   * Draw a sample from the set <i>{0, ..., probabilities.length - 1}</i> with
   * <i>probabilities[i]</i> indicating the probability of drawing <i>i</i>. Note that the sum of
   * probabilities should be equal to 1.
   */
  DRes<SInt> sampleCategoricalDistribution(double[] probabilities);

  /**
   * Draw a sample from the set <i>{0, ..., probabilities.size() - 1}</i> with
   * <i>probabilities.get(i)</i> indicating the probability of drawing <i>i</i>. If it is not known
   * whether the sum of the probabilities is equal to 1, the <i>normalized</i> parameter should be
   * set to <code>false</code>.
   */
  DRes<SInt> sampleCategoricalDistribution(List<DRes<SFixed>> probabilities, boolean normalized);

  /**
   * Draw a sample from an exponential distribution with parameter <i>λ = 1 / b</i> with <i>b gt;
   * 0</i>.
   */
  DRes<SFixed> sampleExponentialDistribution(DRes<SFixed> b);

  /**
   * Draw a sample from an exponential distribution with parameter <i>λ = 1 / b</i> with <i>b gt;
   * 0</i>.
   */
  DRes<SFixed> sampleExponentialDistribution(double b);

  /**
   * Draw a sample from a Laplace distribution with location <i>0</i> and scale <i>b &gt; 0</i>.
   */
  DRes<SFixed> sampleLaplaceDistribution(double b);

  /**
   * Draw a sample from a Laplace distribution with location <i>0</i> and scale <i>b gt; 0</i>.
   */
  DRes<SFixed> sampleLaplaceDistribution(DRes<SFixed> b);

  /**
   * Draw a sample from a normal distribution with mean 0 and variance 1.
   */
  DRes<SFixed> sampleNormalDistribution();

  /**
   * Draw a sample from a Rademacher distribution.
   */
  DRes<SInt> sampleRademacherDistribution();

  /**
   * Draw a sample form a uniform distribution on <i>[0, 1)</i>.
   */
  DRes<SFixed> sampleUniformDistribution();

}
