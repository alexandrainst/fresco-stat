package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;
import java.util.function.Function;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

/**
 * Generic test for continuous distribtion sampling.
 */
public class TestContinuousDistributionMean<ResourcePoolT extends ResourcePool>
    extends TestDistribution<SFixed, BigDecimal, double[], ResourcePoolT> {

  public TestContinuousDistributionMean(int n,
      Function<ProtocolBuilderNumeric, DRes<SFixed>> sampler, double expected, double delta) {
    super(n, sampler, (builder, secret) -> FixedNumeric.using(builder).open(secret),
        output -> output.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
        output -> Math.abs(new Mean().evaluate(output, 0, output.length) - expected) < delta,
        output -> "Samples had observed mean " + new Mean().evaluate(output, 0, output.length)
            + " but " + expected + " +/- " + delta + " was expected.");
  }

}

