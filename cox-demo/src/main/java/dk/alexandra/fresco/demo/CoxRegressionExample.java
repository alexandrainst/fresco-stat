package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.cli.CmdLineUtil;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.sce.SecureComputationEngine;
import dk.alexandra.fresco.framework.sce.SecureComputationEngineImpl;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class CoxRegressionExample {

  public CoxRegressionExample(){
  }
  
  /**
   * Run the CoxRegression example application.
   * @param sce The SCE to use
   * @param resourcePool The ResourcePool to use  
   * @param network The network to use
   */
  public <ResourcePoolT extends ResourcePool> void runApplication(
      SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce,
      ResourcePoolT resourcePool, Network network) {

    CoxRegressionApplication app = new CoxRegressionApplication();

    List<BigDecimal> result = sce.runApplication(app, resourcePool, network);
    System.out.println("Result was: " + result);
  }

  /**
   * Main method for InputSumExample.
   * @param args arguments for the demo
   * @throws IOException if the network fails
   */
  public static <ResourcePoolT extends ResourcePool> void main(String[] args) throws IOException {
    CmdLineUtil<ResourcePoolT, ProtocolBuilderNumeric> util = new CmdLineUtil<>();

    util.parse(args);

    ProtocolSuite<ResourcePoolT, ProtocolBuilderNumeric> psConf = util.getProtocolSuite();

    SecureComputationEngine<ResourcePoolT, ProtocolBuilderNumeric> sce =
        new SecureComputationEngineImpl<>(psConf, util.getEvaluator());

    ResourcePoolT resourcePool = util.getResourcePool();
    new CoxRegressionExample().runApplication(sce, resourcePool, util.getNetwork());
    
    util.closeNetwork();
    sce.shutdownSCE();
  }

}
