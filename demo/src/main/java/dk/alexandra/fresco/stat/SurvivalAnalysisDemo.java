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
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.collections.Collections;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.stat.survival.SurvivalInfoDiscrete;
import dk.alexandra.fresco.suite.spdz.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePool;
import dk.alexandra.fresco.suite.spdz.SpdzResourcePoolImpl;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDummyDataSupplier;
import dk.alexandra.fresco.suite.spdz.storage.SpdzOpenedValueStoreImpl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SurvivalAnalysisDemo {

  /**
   * Compute the difference in death rate between two patient groups, resp. stage III and stage IV diffuse histiocytic lymphoma, using Cox regression. Party 1 has data for stage III patients, and party 2 has data for the stave IV patients.
   */
  public static void main(String[] arguments) {
    if (arguments.length != 2) {
      throw new IllegalArgumentException("Usage: java Demo [myId] [otherIP]");
    }

    /*
     The following data represent the survival in days since entry to the trial of patients with
     diffuse histiocytic lymphoma. Two different groups of patients, those with stage III and those
     with stage IV disease, are compared. Dataset from Armitage, P. and Berry, G. (1994) Statistical
     Methods in Medical Research. 3rd Edition, Blackwell Scientific Publications, Oxford. See also
     https://www.statsdirect.com/help/survival_analysis/cox_regression.htm
     */

    // Stage III
    int[] group1event = new int[]{6, 19, 32, 42, 42, 43, 94, 126, 169, 207, 211, 227, 253, 255, 270,
        310, 316, 335, 346};
    int[] group1censor = new int[]{1, 1, 1, 1, 1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0};

    // Stage IV
    int[] group2event = new int[]{4, 6, 10, 11, 11, 11, 13, 17, 20, 20, 21, 22, 24, 24, 29, 30, 30,
        31, 33, 34, 35, 39, 40, 41, 43, 45, 46, 50, 56, 61, 61, 63, 68, 82, 85, 88, 89, 90,
        93, 104, 110, 134, 137, 160, 169, 171, 173, 175, 184, 201, 222, 235, 247, 260, 284,
        290, 291, 302, 304, 341, 345};
    int[] group2censor = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0};

    // Configurate fresco
    final int myId = Integer.parseInt(arguments[0]);
    final String otherIP = arguments[1];
    final int noParties = 2;
    final int otherId = 3 - myId;
    final int modBitLength = 256;
    final int maxBitLength = 180;
    final int maxBatchSize = 4096;

    Party me = new Party(myId, "localhost", 9000 + myId);
    Party other = new Party(otherId, otherIP, 9000 + otherId);
    NetworkConfiguration networkConfiguration = new NetworkConfigurationImpl(myId,
        Map.of(myId, me, otherId, other));
    Network network = new SocketNetwork(networkConfiguration);
    MersennePrimeFieldDefinition definition = MersennePrimeFieldDefinition.find(modBitLength);
    SpdzProtocolSuite suite = new SpdzProtocolSuite(maxBitLength);

    // Use "dummy" multiplication triples to simulate doing only the online phase
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

    // Load data into X
    List<List<Integer>> x = new ArrayList<>();
    if (myId == 1) {
      for (int i = 0; i < group1event.length; i++) {
        x.add(List.of(0, group1event[i], group1censor[i]));
      }
      for (int i = 0; i < group2event.length; i++) {
        x.add(null);
      }
    } else {
      for (int i = 0; i < group1event.length; i++) {
        x.add(null);
      }
      for (int i = 0; i < group2event.length; i++) {
        x.add(List.of(1, group2event[i], group2censor[i]));
      }
    }

    int n = x.size();
    System.out.println("id = " + myId);
    System.out.println("n  = " + n);
    System.out.println("x  = " + x);

    Instant start = Instant.now();

    BigDecimal out = sce
        .runApplication(new SurvivalAnalysisApplication(x),
            resourcePool, network);

    System.out.println("l  = " + out);
    System.out.println("Took " + Duration.between(start, Instant.now()));
  }

  public static class SurvivalAnalysisApplication implements
      Application<BigDecimal, ProtocolBuilderNumeric> {

    private final List<List<Integer>> x;

    public SurvivalAnalysisApplication(List<List<Integer>> x) {
      this.x = x;
    }

    @Override
    public DRes<BigDecimal> buildComputation(ProtocolBuilderNumeric builder) {
      return builder.par(par -> {
        int id = par.getBasicNumericContext().getMyId();
        List<Pair<DRes<SInt>, List<DRes<SInt>>>> data = new ArrayList<>();
        if (id == 1) {
          for (List<Integer> xi : x) {
            List<DRes<SInt>> row = new ArrayList<>();
            DRes<SInt> time;
            if (xi != null) {
              time = par.numeric().input(xi.get(1), 1);

              // Indicator for group 1
              row.add(par.numeric().input(1, 1));
              row.add(par.numeric().input(0, 1));

              row.add(par.numeric().input(xi.get(2), 1));
            } else {
              time = par.numeric().input(null, 2);
              row.add(par.numeric().input(null, 2));
              row.add(par.numeric().input(null, 2));
              row.add(par.numeric().input(null, 2));
            }
            data.add(new Pair<>(time, row));
          }
        } else if (id == 2) {
          for (List<Integer> xi : x) {
            List<DRes<SInt>> row = new ArrayList<>();
            DRes<SInt> time;
            if (xi != null) {
              time = par.numeric().input(xi.get(1), 2);

              // Indicator for group 2
              row.add(par.numeric().input(0, 2));
              row.add(par.numeric().input(1, 2));

              row.add(par.numeric().input(xi.get(2), 2));
            } else {
              time = par.numeric().input(null, 1);
              row.add(par.numeric().input(null, 1));
              row.add(par.numeric().input(null, 1));
              row.add(par.numeric().input(null, 1));
            }
            data.add(new Pair<>(time, row));
          }
        } else {
          throw new IllegalArgumentException("Id must be 1 or 2 but was " + id);
        }
        return DRes.of(data);
      }).seq((seq, data) -> Collections.using(seq).sort(data)
      ).seq((seq, data) -> {
        List<SurvivalInfoDiscrete> structuredData = new ArrayList<>();
        for (Pair<DRes<SInt>, List<DRes<SInt>>> row : data) {
          structuredData.add(new SurvivalInfoDiscrete(
              List.of(List.of(row.getSecond().get(0), row.getSecond().get(1))),
              row.getFirst(), row.getSecond().get(2)));
        }
        return Statistics.using(seq).coxRegressionDiscrete(structuredData, 5, 0.1,
            new double[]{1});
      }).seq((seq, result) -> FixedNumeric.using(seq).open(result.get(0)));
    }
  }


}
