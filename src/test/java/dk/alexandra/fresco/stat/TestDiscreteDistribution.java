package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.math3.stat.inference.ChiSquareTest;

/**
 * Generic test for continuous distribution sampling.
 */
public class TestDiscreteDistribution<ResourcePoolT extends ResourcePool>
    extends TestDistribution<SInt, BigInteger, long[], ResourcePoolT> {

  public TestDiscreteDistribution(int n,
      Function<ProtocolBuilderNumeric, DRes<SInt>> sampler, double[] expected, double alpha) {
    super(n, sampler, (builder, secret) -> builder.numeric().open(secret),
        output -> {

          Map<Integer, Long> counts = output.stream().mapToInt(BigInteger::intValue).boxed()
              .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

          return IntStream.range(0, expected.length)
              .mapToLong(i -> counts.containsKey(i) ? counts.get(i) : 0).toArray();
        },
        output -> !new ChiSquareTest()
            .chiSquareTest(Arrays.stream(expected).map(p -> p * n).toArray(), output, alpha),
        output -> "Goodness of fit test rejected with p = " + new ChiSquareTest()
            .chiSquareTest(Arrays.stream(expected).map(p -> p * n).toArray(), output));
  }

}
