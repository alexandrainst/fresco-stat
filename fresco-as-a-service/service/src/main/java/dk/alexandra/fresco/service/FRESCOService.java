package dk.alexandra.fresco.service;

import com.google.protobuf.InvalidProtocolBufferException;
import dk.alexandra.fresco.framework.Application;
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
import dk.alexandra.fresco.service.applications.LinearRegressionApplication;
import dk.alexandra.fresco.service.applications.NeuralNetworkApplication;
import dk.alexandra.fresco.service.applications.TTestApplication;
import dk.alexandra.fresco.service.messages.Common;
import dk.alexandra.fresco.service.messages.LinearRegression.LinearRegressionOutput;
import dk.alexandra.fresco.service.messages.NeuralNetwork.NeuralNetworkOutput;
import dk.alexandra.fresco.service.messages.ServiceIO.ServiceInput;
import dk.alexandra.fresco.service.messages.ServiceIO.ServiceOutput;
import dk.alexandra.fresco.service.messages.ServiceIO.ServiceOutput.Builder;
import dk.alexandra.fresco.service.messages.TTest.TTestOutput;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class FRESCOService {

  public static void main(String[] arguments) {

    if (arguments.length != 3) {
      throw new IllegalArgumentException("Usage: java Demo [id] [peer] [zmq_port]");
    }

    final int myId = Integer.parseInt(arguments[0]);
    final String peer = arguments[1];
    final int zmqPort = Integer.parseInt(arguments[2]);

    final int noParties = 2;
    final int otherId = 3 - myId;
    final int modBitLength = 256;
    final int maxBitLength = 180;
    final int maxBatchSize = 4096;
    final Duration timeout = Duration.of(8, ChronoUnit.HOURS);

    System.out.println(
        "\n ███████╗██████╗ ███████╗███████╗ ██████╗ ██████╗        █████╗  █████╗ ███████╗\n"
        + " ██╔════╝██╔══██╗██╔════╝██╔════╝██╔════╝██╔═══██╗      ██╔══██╗██╔══██╗██╔════╝\n"
        + " █████╗  ██████╔╝█████╗  ███████╗██║     ██║   ██║█████╗███████║███████║███████╗\n"
        + " ██╔══╝  ██╔══██╗██╔══╝  ╚════██║██║     ██║   ██║╚════╝██╔══██║██╔══██║╚════██║\n"
        + " ██║     ██║  ██║███████╗███████║╚██████╗╚██████╔╝      ██║  ██║██║  ██║███████║\n"
        + " ╚═╝     ╚═╝  ╚═╝╚══════╝╚══════╝ ╚═════╝ ╚═════╝       ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝\n");

    Party me = new Party(myId, "localhost", 9000 + myId);
    Party other = new Party(myId, peer, 9000 + otherId);
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
    System.out.println("Connecting to peer...");

    SecureComputationEngine<SpdzResourcePool, ProtocolBuilderNumeric> sce = new SecureComputationEngineImpl<>(
        suite, evaluator);

    try (ZContext context = new ZContext()) {
      ZMQ.Socket socket = context.createSocket(SocketType.REP);
      String address = "tcp://*:" + zmqPort;
      socket.bind(address);

      while (!Thread.interrupted()) {
        System.out.println("Waiting for computation request on " + address + "...");
        byte[] received = socket.recv(0);

        try {
          ServiceInput input = ServiceInput.parseFrom(received);
          System.out.println("Got request: " + input.toString().substring(0, 64));
          Instant start = Instant.now();

          Builder builder = ServiceOutput.newBuilder();

          switch (input.getInputCase()) {

            case LINEARREGRESSIONINPUT: {
              Application<List<BigDecimal>, ProtocolBuilderNumeric> app = new LinearRegressionApplication(
                  input.getLinearRegressionInput());
              List<BigDecimal> result = sce.runApplication(app, resourcePool, network,
                  timeout);
              LinearRegressionOutput output = LinearRegressionOutput.newBuilder()
                  .addAllBeta(result.stream().mapToDouble(BigDecimal::doubleValue).boxed().collect(Collectors.toList())).build();
              builder.setLinearRegressionOutput(output);
              break;
            }

            case TTESTINPUT: {
              Application<BigDecimal, ProtocolBuilderNumeric> app = new TTestApplication(
                  input.getTTestInput());
              BigDecimal result = sce.runApplication(app, resourcePool, network,
                  timeout);
              TTestOutput output = TTestOutput.newBuilder()
                  .setT(result.doubleValue()).build();
              builder.setTTestOutput(output);
              break;
            }

            case NEURALNETWORKINPUT: {
              NeuralNetworkApplication app = new NeuralNetworkApplication(
                  input.getNeuralNetworkInput());
              List<Pair<Common.Matrix, Common.Vector>> result = sce
                  .runApplication(app, resourcePool, network, timeout);
              NeuralNetworkOutput output = NeuralNetworkOutput.newBuilder().addAllWeights(result.stream().map(Pair::getFirst).collect(
                  Collectors.toList())).addAllBiases(result.stream().map(Pair::getSecond).collect(
                  Collectors.toList())).build();
              builder.setNeuralNetworkOutput(output);
              break;
            }

            case INPUT_NOT_SET: {
              throw new InvalidProtocolBufferException("No valid input defined in message");
            }
          }


          ServiceOutput output = builder.build();
          Duration duration = Duration.between(start, Instant.now());
          System.out.println("Computed output in " + duration + ": " + output);
          byte[] resultSerialized = output.toByteArray();
          socket.send(resultSerialized, 0);

        } catch (InvalidProtocolBufferException e) {
          System.out.println(e.getMessage());
        }
      }
    }
  }

}
