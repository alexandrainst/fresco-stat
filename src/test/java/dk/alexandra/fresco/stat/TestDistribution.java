package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;

/**
 * Generic test for distribution sampling.
 */
public class TestDistribution<S, O, T, ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

  private final int n;
  private final Function<ProtocolBuilderNumeric, DRes<S>> sampler;
  private final Predicate<T> expected;
  private final BiFunction<ProtocolBuilderNumeric, DRes<S>, DRes<O>> open;
  private final Function<List<O>, T> metric;
  private final Function<T, String> errorMessage;

  public TestDistribution(int n,
      Function<ProtocolBuilderNumeric, DRes<S>> sampler, BiFunction<ProtocolBuilderNumeric, DRes<S>,
      DRes<O>> open, Function<List<O>, T> metric, Predicate<T> expected,
      Function<T, String> errorMessage) {
    this.n = n;
    this.sampler = sampler;
    this.open = open;
    this.expected = expected;
    this.metric = metric;
    this.errorMessage = errorMessage;
  }

  @Override
  public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {

    return new TestThread<>() {

      @Override
      public void test() throws Exception {

        Application<List<O>, ProtocolBuilderNumeric> testApplication = producer -> producer
            .seq(seq -> {
              List<DRes<S>> samples =
                  Stream.generate(() -> sampler.apply(seq)).limit(n)
                      .collect(Collectors.toList());

              List<DRes<O>> opened =
                  samples.stream().map(o -> open.apply(seq, o))
                      .collect(Collectors.toList());

              return DRes.of(opened);
            }).seq(
                (seq, opened) -> DRes
                    .of(opened.stream().map(DRes::out).collect(Collectors.toList())));

        List<O> output = runApplication(testApplication);

        T actual = metric.apply(output);

        Assert.assertTrue(errorMessage.apply(actual), expected.test(actual));
      }
    };

  }

}

