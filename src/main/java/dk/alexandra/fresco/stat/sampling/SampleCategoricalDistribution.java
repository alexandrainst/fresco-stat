package dk.alexandra.fresco.stat.sampling;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
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
   * @param probabilities The <i>i</i>'th element of this list is the probability of drawing
   *                      <i>i</i> from this distribution.
   * @param normalized    Does the probabilities sum to 1? If not, the computation handles the
   *                      normalization.
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

        DRes<SFixed> finalR = r;
        return builder.par(par -> {

          double c = knownProbabilities[0];
          List<DRes<SInt>> terms = new ArrayList<>();
          for (int i = 0; i < knownProbabilities.length; i++) {
            if (i > 0) {
              c += knownProbabilities[i];
            }
            terms.add(
                FixedNumeric.using(par)
                    .leq(numeric.known(BigDecimal.valueOf(c)), finalR));
          }
          return DRes.of(terms);
        }).seq((seq, terms) -> AdvancedNumeric.using(seq).sum(terms));

      } else {

        // Compute c_i's
        List<DRes<SFixed>> accProbabilities = new ArrayList<>();
        DRes<SFixed> s = probabilities.get(0);
        accProbabilities.add(s);
        for (int i = 1; i < probabilities.size(); i++) {
          s = numeric.add(s, probabilities.get(i));
          accProbabilities.add(s);
        }

        // Normalize if needed
        if (!normalized) {
          r = numeric.mult(accProbabilities.get(accProbabilities.size() - 1), r);
        }

        DRes<SFixed> finalR = r;
        return builder.par(par -> {
          List<DRes<SInt>> terms = new ArrayList<>();
          for (int i = 0; i < probabilities.size() - 1; i++) {
            terms.add(FixedNumeric.using(par).leq(finalR, accProbabilities.get(i)));
          }
          return DRes.of(terms);
        }).seq((seq, terms) -> seq.numeric()
            .sub(probabilities.size() - 1, AdvancedNumeric.using(seq).sum(terms)));
      }
    });
  }

}
