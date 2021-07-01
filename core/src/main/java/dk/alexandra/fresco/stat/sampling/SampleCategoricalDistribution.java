package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Sample an element from a categorical distribution.
 */
public class SampleCategoricalDistribution implements Computation<SInt, ProtocolBuilderNumeric> {

  private final boolean normalized;
  private List<DRes<SFixed>> probabilities;
  private double[] knownProbabilities;

  /**
   * @param probabilities The <i>i</i>'th element of this list is the probability of drawing <i>i</i> from this
   *                      distribution.
   * @param normalized    Does the probabilities sum to 1? If not, the computation handles the normalization.
   */
  public SampleCategoricalDistribution(List<DRes<SFixed>> probabilities, boolean normalized) {
    this.probabilities = probabilities;
    this.normalized = normalized;
  }

  public SampleCategoricalDistribution(double[] probabilities) {
    double sum = Arrays.stream(probabilities).sum();
    this.knownProbabilities = Arrays.stream(probabilities).map(p -> p / sum).toArray();
    this.normalized = true;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric root) {
    return root.seq(builder -> {

      /*
       * Let p_0,...,p_{n-1} be the probabilities of drawing 0, ..., n-1 resp.
       *
       * Now sample r uniformly in [0,1). Let c_i = p_0 + ... + p_i and let t_i = 0 if c_i <= r and
       * 1 otherwise.
       *
       * We return Sum_{j=0}^n t_j which will be i with probability p_i
       */

      DRes<SFixed> r = new SampleUniformDistribution().buildComputation(builder);
      FixedNumeric numeric = FixedNumeric.using(builder);

      if (Objects.nonNull(knownProbabilities)) {

        double c = knownProbabilities[0];
        List<DRes<SInt>> terms = new ArrayList<>();
        for (int i = 0; i < knownProbabilities.length; i++) {
          if (i > 0) {
            c += knownProbabilities[i];
          }
          terms.add(
              numeric
                  .leq(numeric.known(BigDecimal.valueOf(c)), r));
        }
        return AdvancedNumeric.using(builder).sum(terms);

      } else {

        if (!normalized) {
          DRes<SFixed> sum = AdvancedFixedNumeric.using(builder).sum(probabilities);
          r = numeric.mult(sum, r);
        }

        DRes<SFixed> c = probabilities.get(0);
        List<DRes<SInt>> terms = new ArrayList<>();
        for (int i = 0; i < probabilities.size(); i++) {
          if (i > 0) {
            c = numeric.add(c, probabilities.get(i));
          }
          terms.add(numeric.leq(c, r));
        }
        return AdvancedNumeric.using(builder).sum(terms);

      }
    });
  }

}
