package dk.alexandra.fresco.stat.mlp;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.FixedLinearAlgebra;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.stat.mlp.evaluation.AccuracyBinary;
import dk.alexandra.fresco.stat.utils.Triple;
import dk.alexandra.fresco.stat.utils.VectorUtils;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Assert;

public class NNTests {

  public static class TestForwardPropagation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {

                double[][] weights1 = new double[][]{{0.13436424411240122, 0.8474337369372327}};
                double[] bias1 = {0.763774618976614};
                double[][] weights2 = new double[][]{{0.2550690257394217}, {0.4494910647887381}};
                double[] bias2 = {0.49543508709194095, 0.651592972722763};
                MLP nn = new MLP(List.of(new Layer(weights1, bias1, seq),
                    new Layer(weights2, bias2, seq)));

                ArrayList<BigDecimal> input = List.of(1.0, 0.0).stream().map(BigDecimal::valueOf)
                    .collect(Collectors.toCollection(ArrayList::new));
                DRes<ArrayList<DRes<SFixed>>> secret = FixedLinearAlgebra.using(seq)
                    .input(input, 1);

                return Pair.lazy(nn, secret);
              }).seq((seq, networkAndInput) -> seq.seq(
                  networkAndInput.getFirst().apply(networkAndInput.getSecond().out())))
                  .seq((seq, output) -> DRes.of(output.stream().map(FixedNumeric.using(seq)::open)
                      .collect(Collectors.toList()))
                  ).seq((seq, opened) -> () -> opened.stream().map(DRes::out)
                      .collect(Collectors.toList()));

          List<BigDecimal> output = runApplication(testApplication);

          // Test vector
          Assert.assertEquals(0.6629970129852887, output.get(0).doubleValue(), 0.0001);
          Assert.assertEquals(0.7253160725279748, output.get(1).doubleValue(), 0.0001);
        }
      };
    }
  }

  public static class TestBackwardPropagation<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {
                double[][] weights1 = new double[][]{{0.13436424411240122, 0.8474337369372327}};
                double[] bias1 = {0.763774618976614};
                double[][] weights2 = new double[][]{{0.2550690257394217}, {0.4494910647887381}};
                double[] bias2 = {0.49543508709194095, 0.651592972722763};
                MLP nn = new MLP(List.of(new Layer(weights1, bias1, seq),
                    new Layer(weights2, bias2, seq)));
                ArrayList<BigDecimal> input = List.of(1.0, 0.0).stream().map(BigDecimal::valueOf)
                    .collect(Collectors.toCollection(ArrayList::new));
                DRes<ArrayList<DRes<SFixed>>> secret = FixedLinearAlgebra.using(seq)
                    .input(input, 1);
                return Pair.lazy(nn, secret);
              }).seq((seq, networkAndInput) -> {
                DRes<List<ForwardPropagationOutput>> outputs = networkAndInput.getFirst()
                    .forwardPropagation(networkAndInput.getSecond().out()).buildComputation(seq);
                return Pair.lazy(networkAndInput.getFirst(), outputs);
              }).seq((seq, networkAndOutput) -> {
                List<ForwardPropagationOutput> outputs = networkAndOutput.getSecond().out();
                ArrayList<DRes<SFixed>> output = outputs.get(outputs.size() - 1).getAfterActivation().out();
                ArrayList<DRes<SFixed>> error = new ArrayList<>(
                    List.of(FixedNumeric.using(seq).sub(0.0, output.get(0)),
                        FixedNumeric.using(seq).sub(1.0, output.get(1))));
                return DRes.of(new Triple<>(networkAndOutput.getFirst(),
                    networkAndOutput.getSecond(), error));
              }).seq((seq, networkOutputError) -> {
                DRes<List<ArrayList<DRes<SFixed>>>> deltas = networkOutputError.getFirst()
                    .backPropagation(
                        networkOutputError.getSecond().out(), networkOutputError.getThird())
                    .buildComputation(seq);
                return Pair.lazy(networkOutputError.getFirst(), deltas);
              }).seq((seq, networkAndDeltas) -> DRes.of(VectorUtils
                  .entrywiseUnaryOp(networkAndDeltas.getSecond().out().get(1),
                      (x, b) -> FixedNumeric.using(b).open(x), seq))).seq((seq, opened) -> DRes.of(opened.stream().map(DRes::out)
                  .collect(Collectors.toList())));

          List<BigDecimal> output = runApplication(testApplication);
          Assert.assertArrayEquals(
              new double[]{-0.14813473120687762, 0.05472601157879688},
              output.stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.0001);
        }
      };
    }
  }


  public static class TestSingleStepTraining<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    double[][] data = new double[][]{
        {2.7810836, 2.550537003},
        {1.465489372, 2.362125076},
        {3.396561688, 4.400293529},
        {1.38807019, 1.850220317},
        {3.06407232, 3.005305973},
        {7.627531214, 2.759262235},
        {5.332441248, 2.088626775},
        {6.922596716, 1.77106367},
        {8.675418651, -0.242068655},
        {7.673756466, 3.508563011}
    };

    double[][] labels = new double[][]{
        {1, 0},
        {1, 0},
        {1, 0},
        {1, 0},
        {1, 0},
        {0, 1},
        {0, 1},
        {0, 1},
        {0, 1},
        {0, 1}
    };

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<List<BigDecimal>, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {
                double[][] weights1 = new double[][]{{0.13436424411240122, 0.8474337369372327}};
                double[] bias1 = {0.763774618976614};
                double[][] weights2 = new double[][]{{0.2550690257394217}, {0.4494910647887381}};
                double[] bias2 = {0.49543508709194095, 0.651592972722763};
                MLP nn = new MLP(List.of(new Layer(weights1, bias1, seq),
                    new Layer(weights2, bias2, seq)));
                FixedNumeric fixedNumeric = FixedNumeric.using(seq);
                List<ArrayList<DRes<SFixed>>> input = Arrays.stream(data)
                    .map(row -> Arrays.stream(row).mapToObj(fixedNumeric::known)
                        .collect(Collectors.toCollection(ArrayList::new)))
                    .collect(Collectors.toList());
                List<ArrayList<DRes<SFixed>>> expected = Arrays.stream(labels)
                    .map(row -> Arrays.stream(row).mapToObj(fixedNumeric::known)
                        .collect(Collectors.toCollection(ArrayList::new)))
                    .collect(Collectors.toList());

                return Pair.lazy(nn, new Pair<>(input, expected));
              }).seq((seq, networkAndInput) -> seq.seq(
                  networkAndInput.getFirst().fit(networkAndInput.getSecond().getFirst(),
                      networkAndInput.getSecond().getSecond(),  10, 1.0))).seq((seq, network) -> {
                List<DRes<SFixed>> parameters = new ArrayList<>();
                parameters.addAll(network.getLayer(0).getWeights().getRow(0));
                parameters.addAll(network.getLayer(0).getBias());
                parameters.addAll(network.getLayer(1).getWeights().getColumn(0));
                parameters.addAll(network.getLayer(1).getBias());

                List<DRes<BigDecimal>> open = parameters.stream().map(FixedNumeric.using(seq)::open)
                    .collect(
                        Collectors.toList());
                return DRes.of(open);
              }).seq((seq, open) -> {
                List<BigDecimal> out = open.stream().map(DRes::out).collect(Collectors.toList());
                return DRes.of(out);
              });

          List<BigDecimal> output = runApplication(testApplication);

          // Compare with test vector
          Assert.assertArrayEquals(
              new double[]{-1.069287, 0.794906, 0.731589, 0.809887, -1.003111, -0.478726, 0.567792},
              output.stream().mapToDouble(BigDecimal::doubleValue).toArray(), 0.0001);
        }
      };
    }
  }


  public static class TestPrediction<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    double[][] data = new double[][]{
        {2.7810836, 2.550537003},
        {1.465489372, 2.362125076},
        {3.396561688, 4.400293529},
        {1.38807019, 1.850220317},
        {3.06407232, 3.005305973},
        {7.627531214, 2.759262235},
        {5.332441248, 2.088626775},
        {6.922596716, 1.77106367},
        {8.675418651, -0.242068655},
        {7.673756466, 3.508563011}
    };

    int[] labels = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1};

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<BigInteger, ProtocolBuilderNumeric> testApplication =
              root -> root.seq(seq -> {
                // Output from 10 epochs of single step training
                double[][] weights1 = new double[][]{
                    {-1.06928768008947372436523437500000, 0.79490690003149211406707763671875}};
                double[] bias1 = {0.73158905119635164737701416015625};
                double[][] weights2 = new double[][]{{0.80988725298084318637847900390625},
                    {-1.00311183976009488105773925781250}};
                double[] bias2 = {-0.47872626874595880508422851562500,
                    0.56779224239289760589599609375000};
                MLP nn = new MLP(List.of(new Layer(weights1, bias1, seq),
                    new Layer(weights2, bias2, seq)));
                FixedNumeric fixedNumeric = FixedNumeric.using(seq);
                List<ArrayList<DRes<SFixed>>> input = Arrays.stream(data)
                    .map(row -> Arrays.stream(row).mapToObj(fixedNumeric::known)
                        .collect(Collectors.toCollection(ArrayList::new)))
                    .collect(Collectors.toList());

                ArrayList<DRes<SInt>> expected = Arrays.stream(labels)
                    .mapToObj(seq.numeric()::known)
                    .collect(Collectors.toCollection(ArrayList::new));

                return Pair.lazy(nn, new Pair<>(input, expected));
              }).seq((seq, networkAndInput) -> new AccuracyBinary(networkAndInput.getFirst(),
                  networkAndInput.getSecond()
                      .getFirst(), networkAndInput.getSecond().getSecond()).buildComputation(seq))
                  .seq((seq, correct) -> seq.numeric().open(correct));

          BigInteger output = runApplication(testApplication);

          // From test vector 8/10 should be correct after training
          Assert.assertEquals(8, output.intValue());
        }
      };
    }
  }

  public static class TestFit<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    double[][] data = new double[][]{
        {0.0, 0.0},
        {1.0, 0.0},
        {0.0, 1.0},
        {0.5, 0.5}};
    int[] labels = {0, 0, 0, 1};
    int categories = 2;

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<>() {

        @Override
        public void test() {

          Application<BigInteger, ProtocolBuilderNumeric> testApplication =
              root -> root.par(par -> {
                FixedNumeric numeric = FixedNumeric.using(par);

                List<ArrayList<DRes<SFixed>>> x = Arrays.stream(data).map(
                    row -> Arrays.stream(row)
                        .mapToObj(entry -> numeric.input(entry, 1))
                        .collect(Collectors.toCollection(ArrayList::new)))
                    .collect(Collectors.toList());
                List<ArrayList<DRes<SFixed>>> y = Arrays.stream(labels).mapToObj(entry -> {
                  ArrayList<DRes<SFixed>> yi = new ArrayList<>();
                  for (int i = 0; i < categories; i++) {
                    if (entry == i) {
                      yi.add(numeric.known(1.0));
                    } else {
                      yi.add(numeric.known(0.0));
                    }
                  }
                  return yi;
                }).collect(Collectors.toList());
                return Pair.lazy(x, y);
              }).seq((seq, data) -> {
                Random prng = new Random(1234);
                MLP nn = new MLP(
                    List.of(new Layer(2, 16, prng, seq), new Layer(16, 2, prng, seq)));
                return seq.seq(nn.fit(data.getFirst(), data.getSecond(), 10, 1));
              }).seq((seq, nn) -> {
                FixedNumeric numeric = FixedNumeric.using(seq);

                List<ArrayList<DRes<SFixed>>> x = Arrays.stream(data).map(
                    row -> Arrays.stream(row)
                        .mapToObj(entry -> numeric.input(entry, 1))
                        .collect(Collectors.toCollection(ArrayList::new)))
                    .collect(Collectors.toList());
                ArrayList<DRes<SInt>> y = Arrays.stream(labels)
                    .mapToObj(entry -> seq.numeric().input(entry, 2)).collect(
                        Collectors.toCollection(ArrayList::new));

                return new AccuracyBinary(nn, x, y).buildComputation(seq);
              }).seq((seq, correct) -> seq.numeric().open(correct));

          BigInteger output = runApplication(testApplication);

          System.out.println(output);
        }
      };
    }
  }
}
