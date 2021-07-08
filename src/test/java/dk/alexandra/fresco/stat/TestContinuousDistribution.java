package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;
import java.util.function.Function;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

/**
 * Generic test for continuous distribtion sampling.
 */
public class TestContinuousDistribution<ResourcePoolT extends ResourcePool>
    extends TestDistribution<SFixed, BigDecimal, double[], ResourcePoolT> {

  public TestContinuousDistribution(int n,
      Function<ProtocolBuilderNumeric, DRes<SFixed>> sampler, RealDistribution expected,
      double alpha) {
    super(n, sampler, (builder, secret) -> FixedNumeric.using(builder).open(secret),
        output -> output.stream().mapToDouble(BigDecimal::doubleValue).toArray(),
        output -> !new KolmogorovSmirnovTest().kolmogorovSmirnovTest(expected, output, alpha),
        output -> "Goodness of fit test rejected with p = " + new KolmogorovSmirnovTest()
            .kolmogorovSmirnovTest(expected, output));
  }

}

