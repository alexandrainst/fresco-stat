package dk.alexandra.fresco.stat;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import dk.alexandra.fresco.stat.utils.RealUtils;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;

public class UtilTests {

  public static <A, B> void testVectorEntrywiseOpSizeMismatch(EntrywiseListOp<A, B> op)
      throws IllegalArgumentException {

    ProtocolBuilderNumeric builder = mock(ProtocolBuilderNumeric.class);

    when(builder.seq(any(Computation.class))).thenAnswer(invocation -> {
      return invocation.getArgument(0, Computation.class).buildComputation(null);
    });
    when(builder.par(any(ComputationParallel.class))).thenAnswer(invocation -> {
      return invocation.getArgument(0, Computation.class).buildComputation(null);
    });

    List<A> list1 = mock(List.class);
    when(list1.size()).thenReturn(2);

    List<B> list2 = mock(List.class);
    when(list1.size()).thenReturn(3);

    op.apply(list1, list2, builder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInnerProductWithBitVectorSizeMismatch() {
    testVectorEntrywiseOpSizeMismatch(VectorUtils::innerProductWithBitvectorPublic);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInnerProductWithPublicBitVectorSizeMismatch() {
    testVectorEntrywiseOpSizeMismatch(VectorUtils::innerProductWithBitvector);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVectorSubSizeMismatch() {
    testVectorEntrywiseOpSizeMismatch(VectorUtils::sub);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVectorAddSizeMismatch() {
    testVectorEntrywiseOpSizeMismatch(VectorUtils::add);
  }

  @Test
  public void testCreateMultidimensionalArray() {
    MultiDimensionalArray<Integer> array = MultiDimensionalArray.build(List.of(2, 2, 3), i -> {
      if (i.get(0) == 1 && i.get(1) == 0 && i.get(2) == 1) {
        return 1;
      }
      return 0;
    });
    Assert.assertEquals(array.get(1, 0, 1).intValue(), 1);
    Assert.assertEquals(array.get(1, 1, 2).intValue(), 0);
    array.set(List.of(1, 0, 1), 0);
    array.set(List.of(1, 1, 2), 1);
    Assert.assertEquals(array.get(1, 0, 1).intValue(), 0);
    Assert.assertEquals(array.get(1, 1, 2).intValue(), 1);
  }

  @Test
  public void testMultidimensionalArrayIterator() {
    MultiDimensionalArray<Integer> array = MultiDimensionalArray.build(List.of(2, 2, 3), i ->
        i.get(0) * 2 * 3 + i.get(1) * 3 + i.get(2));
    int n = 2 * 2 * 3;
    Assert.assertEquals(n, array.size());
    Assert.assertEquals(n, array.stream().count());
    Assert.assertArrayEquals(IntStream.range(0, n).toArray(),
        array.stream().mapToInt(Integer::intValue).toArray());
  }

  private interface EntrywiseListOp<A, B> {

    Object apply(List<A> list1, List<B> list2, ProtocolBuilderNumeric builder);
  }

  public static class TestProduct<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        final List<Double> a = List.of(1.3, 2.1, -3.9, 0.01, -11.7);

        @Override
        public void test() {

          Application<BigDecimal, ProtocolBuilderNumeric> testApplication = builder ->
              builder.seq(seq -> {
                FixedNumeric numeric = FixedNumeric.using(seq);
                List<DRes<SFixed>> inputA =
                    a.stream().map(numeric::known).collect(Collectors.toList());
                return RealUtils.product(inputA, seq);
              }).seq((seq, product) -> FixedNumeric.using(seq).open(product));

          BigDecimal out = runApplication(testApplication);
          double expected = a.stream().reduce(1.0, (a, b) -> a * b);
          assertEquals(expected, out.doubleValue(), 0.01);
        }
      };
    }
  }

}
