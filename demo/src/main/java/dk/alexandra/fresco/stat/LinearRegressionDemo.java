package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.MersennePrimeFieldDefinition;
import dk.alexandra.fresco.framework.configuration.NetworkConfiguration;
import dk.alexandra.fresco.framework.configuration.NetworkConfigurationImpl;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.socket.SocketNetwork;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class LinearRegressionDemo {

  // Perform a simple linear regression on a dataset of average masses (kg) for women as a function of
  // their height (m) in a sample of American women of age 30–39.
  public static void main(String[] arguments) throws IOException {
    if (arguments.length != 2) {
      throw new IllegalArgumentException("Usage: java Demo [myId] [otherIP]");
    }

    final int myId = Integer.parseInt(arguments[0]);
    final String otherIP = arguments[1];
    final int noParties = 2;
    final int otherId = 3 - myId;
    final int modBitLength = 256;
    final int maxBitLength = 180;
    final int maxBatchSize = 4096;

    Party me = new Party(myId, "localhost", 9000 + myId);
    Party other = new Party(myId, otherIP, 9000 + otherId);
    NetworkConfiguration networkConfiguration = new NetworkConfigurationImpl(myId,
        Map.of(myId, me, otherId, other));
    Network network = new SocketNetwork(networkConfiguration);

    MersennePrimeFieldDefinition definition = MersennePrimeFieldDefinition.find(modBitLength);
    SpdzProtocolSuite suite = new SpdzProtocolSuite(maxBitLength);

    SpdzDataSupplier supplier = new SpdzDummyDataSupplier(myId, noParties, definition,
        BigInteger.valueOf(1234));

    SpdzResourcePool resourcePool = new SpdzResourcePoolImpl(myId, noParties,
        new SpdzOpenedValueStoreImpl(), supplier,
        AesCtrDrbg::new);

    BatchedProtocolEvaluator<SpdzResourcePool> evaluator =
        new BatchedProtocolEvaluator<>(EvaluationStrategy.SEQUENTIAL_BATCHED.getStrategy(), suite,
            maxBatchSize);
    SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce = new SecureComputationEngineImpl<>(
        suite, evaluator);

    Reader in = new FileReader("linreg" + myId + ".csv");
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withRecordSeparator(",").parse(in);
    List<List<String>> data = StreamSupport.stream(records.spliterator(), false).map(
        record -> StreamSupport.stream(record.spliterator(), false).collect(Collectors.toList()))
        .collect(Collectors.toList());

    // Party 1 has all independent variables, party 2 has the dependent variable
    List<BigDecimal> x, y;

    int n = data.size();
    if (myId == 1) {
      x = List.of(1.47, 1.50, 1.52, 1.55, 1.57, 1.60, 1.63, 1.65, 1.68, 1.70, 1.73, 1.75, 1.78, 1.80, 1.83).stream().map(BigDecimal::valueOf).collect(Collectors.toList());
      y = new ArrayList<>(Collections.nCopies(n, null));
    } else {
      x = new ArrayList<>(Collections.nCopies(n, null));
      y = List.of(52.21, 53.12, 54.48, 55.84, 57.20, 58.57, 59.93, 61.29, 63.11, 64.47, 66.28, 68.10, 69.92, 72.19, 74.46).stream().map(BigDecimal::valueOf).collect(Collectors.toList());
    }

    System.out.println("id = " + myId);
    System.out.println("n  = " + n);
    System.out.println("x  = " + x);
    System.out.println("y  = " + y);

    Instant start = Instant.now();

    List<BigDecimal> out = sce
        .runApplication(new LinearRegressionApplication(x, y),
            resourcePool, network);

    System.out.println("a     = " + out.get(0));
    System.out.println("b     = " + out.get(1));
    System.out.println("s_a^2 = " + out.get(2));
    System.out.println("s_b^2 = " + out.get(3));
    System.out.println("r^2   = " + out.get(4));

    System.out.println("Took " + Duration.between(start, Instant.now()));
  }

  public static class LinearRegressionApplication implements
      Application<List<BigDecimal>, ProtocolBuilderNumeric> {

    private final List<BigDecimal> myX, myY;

    public LinearRegressionApplication(List<BigDecimal> x, List<BigDecimal> y) {
      this.myX = x;
      this.myY = y;
    }

    public DRes<List<BigDecimal>> buildComputation(
        ProtocolBuilderNumeric builder) {

      return builder.par(par -> {

        // Party 1 inputs x. Party 2 should input a list of nulls.
        List<DRes<SFixed>> xSecret = myX.stream().map(x -> FixedNumeric
            .using(par).input(x, 1))
            .collect(Collectors.toList());

        // Party 2 inputs y. Party 1 should input a list of nulls.
        List<DRes<SFixed>> ySecret = myY.stream().map(y -> FixedNumeric
            .using(par).input(y, 2))
            .collect(Collectors.toList());

        return Pair.lazy(xSecret, ySecret);
      }).seq((seq, input) -> {

        // Compute simple linear regression on the combined input
        return Statistics.using(seq)
            .simpleLinearRegression(input.getFirst(), input.getSecond());

      }).par((par, f) -> {

        // Reveal result to both parties
        List<DRes<BigDecimal>> result =
            List.of(
                FixedNumeric.using(par).open(f.getAlpha()),
                FixedNumeric.using(par).open(f.getBeta()),
                FixedNumeric.using(par).open(f.getErrorAlphaSquared()),
                FixedNumeric.using(par).open(f.getErrorBetaSquared()),
                FixedNumeric.using(par).open(f.getRSquared())
            );
        return DRes.of(result);

      }).seq((seq, result) -> DRes.of(result.stream().map(DRes::out).collect(Collectors.toList())));
    }
  }

}
