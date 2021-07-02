package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
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
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.stat.utils.LoadEnumerated;
import dk.alexandra.fresco.stat.utils.MultiDimensionalArray;
import dk.alexandra.fresco.stat.utils.Triple;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KAnonymityDemo {

  /**
   * Compute a k-anonymized version of a subset of the attributes of the "adult" dataset with
   * predefined generalizations. Equivalence classes with less than k individuals are suppressed.
   */
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

    int limit = 1000;
    Pair<ArrayList<ArrayList<BigInteger>>, ArrayList<ArrayList<String>>> load =
        LoadEnumerated.readFromCSV("adult.csv", limit, List.of(1, 3, 6, 8, 9), List.of(0));
    ArrayList<ArrayList<BigInteger>> data = load.getFirst();
    ArrayList<BigInteger> sensitive = LoadEnumerated.readFromCSV("adult.csv", limit,
        List.of(14), List.of()).getFirst().stream().map(e -> e.get(0))
        .collect(Collectors.toCollection(ArrayList::new));

    // All enumerated attributes start from 1
    List<List<Integer>> buckets = List.of(
        List.of(20, 40), // age
        List.of(5, 10),
        // workclass -- Self-emp-not-inc=0,  Federal-gov=1,  Local-gov=2,  Self-emp-inc=3,  Never-worked=4,  State-gov=5,  Private=6,  ?=7,  Without-pay=8
        List.of(3),
        // education -- 1st-4th=0,  Bachelors=1,  9th=2,  HS-grad=3,  Masters=4,  Assoc-voc=5,  Some-college=6,  10th=7,  11th=8,  Assoc-acdm=9,  12th=10,  Prof-school=11,  5th-6th=12,  7th-8th=13,  Doctorate=14,  Preschool=15
        //List.of(2, 4), // marital-status -- Married-spouse-absent=0,  Never-married=1,  Married-civ-spouse=2,  Divorced=3,  Separated=4,  Married-AF-spouse=5,  Widowed=6
        List.of(5, 10),
        // occupation -- Craft-repair=0,  Other-service=1,  Handlers-cleaners=2,  Priv-house-serv=3,  Exec-managerial=4,  Machine-op-inspct=5,  Prof-specialty=6,  Farming-fishing=7,  Armed-Forces=8,  Sales=9,  Transport-moving=10,  Tech-support=11,  Adm-clerical=12,  Protective-serv=13,  ?=14
        List.of(3),
        // race -- Black=0,  Asian-Pac-Islander=1,  Other=2,  White=3,  Amer-Indian-Eskimo=4
        List.of(1) // sex -- Female=0,  Male=1
    );

    int n = data.size();

    System.out.println("id = " + myId);
    System.out.println("n  = " + n);
    System.out.println("x  = " + data.subList(0, 10) + " ...");
    System.out.println("y  = " + sensitive.subList(0, 10) + " ...");
    System.out.println("b  = " + buckets);

    Instant start = Instant.now();

    MultiDimensionalArray<List<BigInteger>> out = sce
        .runApplication(new KAnonymityApplication(data, sensitive, buckets, 3),
            resourcePool, network, Duration.ofHours(2));

    System.out.println();
    System.out.println("Result:");
    out.forEachWithIndices((value, indices) -> {
      List<BigInteger> nonZeroEntries = value.stream().filter(t -> t.intValue() > 0)
          .collect(Collectors.toList());
      if (!nonZeroEntries.isEmpty()) {
        System.out.println(bucketToString(buckets, indices) + ": " + nonZeroEntries.stream()
            .collect(Collectors.groupingBy(
                Function.identity(), Collectors.counting())));
      }
    });

    long suppressed = out.stream().flatMap(Collection::stream).mapToInt(BigInteger::intValue)
        .filter(u -> u != 0).count();
    System.out.println("Suppressed rows: " + (n - suppressed));
    System.out.println("Took " + Duration.between(start, Instant.now()));
  }

  private static String bucketToString(List<Integer> buckets, int j) {
    StringBuilder sb = new StringBuilder();
    if (j == 0) {
      sb.append("(-∞, ").append(buckets.get(0)).append("]");
    } else if (j == buckets.size()) {
      sb.append("(").append(buckets.get(j - 1)).append(", ∞)");
    } else {
      sb.append("(").append(buckets.get(j - 1)).append(", ")
          .append(buckets.get(j)).append("]");
    }
    return sb.toString();
  }

  private static String bucketToString(List<List<Integer>> buckets, List<Integer> index) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < index.size(); i++) {
      List<Integer> bucket = buckets.get(i);
      int j = index.get(i);
      sb.append(bucketToString(bucket, j));
      if (i < index.size() - 1) {
        sb.append(" x ");
      }
    }
    return sb.toString();
  }

  public static class KAnonymityApplication implements
      Application<MultiDimensionalArray<List<BigInteger>>, ProtocolBuilderNumeric> {

    private final ArrayList<ArrayList<BigInteger>> myData;
    private final ArrayList<BigInteger> sensitive;
    private final List<List<Integer>> buckets;
    private final int k;

    public KAnonymityApplication(ArrayList<ArrayList<BigInteger>> myData,
        ArrayList<BigInteger> sensitive, List<List<Integer>> buckets, int k) {
      this.myData = myData;
      this.sensitive = sensitive;
      this.buckets = buckets;
      this.k = k;
    }

    @Override
    public DRes<MultiDimensionalArray<List<BigInteger>>> buildComputation(
        ProtocolBuilderNumeric builder) {
      return builder
          .par(par -> {
            Numeric numeric = par.numeric();

            ArrayList<ArrayList<DRes<SInt>>> x = myData.stream().map(
                row -> row.stream().map(entry -> numeric.input(entry, 1))
                    .collect(Collectors.toCollection(ArrayList::new)))
                .collect(Collectors.toCollection(ArrayList::new));
            ArrayList<DRes<SInt>> y = sensitive.stream().map(entry -> numeric.input(entry, 2))
                .collect(Collectors.toCollection(ArrayList::new));

            List<List<DRes<SInt>>> b = buckets.stream()
                .map(row -> row.stream().map(numeric::known).collect(Collectors.toList()))
                .collect(Collectors.toList());

            return Triple.lazy(x, y, b);
          }).seq((seq, data) -> {
            Matrix<DRes<SInt>> x = new Matrix<>(data.getFirst().size(),
                data.getFirst().get(0).size(),
                data.getFirst());
            return Statistics.using(seq).kAnonymize(x, data.getSecond(), data.getThird(), k);
          }).seq((seq, histogram) -> {
            Collections collections = Collections.using(seq);
            MultiDimensionalArray<DRes<List<DRes<BigInteger>>>> opened = histogram
                .map(l -> collections.openList(DRes.of(l)));
            return () -> opened
                .map(a -> a.out().stream().map(DRes::out).collect(Collectors.toList()));
          });
    }
  }

}
