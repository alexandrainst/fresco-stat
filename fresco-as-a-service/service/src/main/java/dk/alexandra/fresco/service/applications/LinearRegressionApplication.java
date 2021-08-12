package dk.alexandra.fresco.service.applications;

import static dk.alexandra.fresco.service.applications.Utils.input;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.service.messages.LinearRegression.LinearRegressionInput;
import dk.alexandra.fresco.service.messages.MPCInputOuterClass.MPCInput;
import dk.alexandra.fresco.service.messages.MPCInputOuterClass.MPCVector;
import dk.alexandra.fresco.stat.Statistics;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LinearRegressionApplication implements
    Application<List<BigDecimal>, ProtocolBuilderNumeric> {

  private final LinearRegressionInput input;

  public LinearRegressionApplication(LinearRegressionInput input) {
    this.input = input;
  }

  @Override
  public DRes<List<BigDecimal>> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(par);
      List<ArrayList<DRes<SFixed>>> observations = new ArrayList<>();
      ArrayList<DRes<SFixed>> dependants = new ArrayList<>();

      for (MPCVector observation : input.getXList()) {
        ArrayList<DRes<SFixed>> row = new ArrayList<>();
        row.add(fixedNumeric.known(1.0));
        for (MPCInput xi : observation.getValuesList()) {
          row.add(input(xi, par));
        }
        observations.add(row);
      }
      for (MPCInput yi : input.getY().getValuesList()) {
        dependants.add(input(yi, par));
      }
      return Pair.lazy(observations, dependants);
    }).seq((seq, data) -> Statistics.using(seq).linearRegression(data.getFirst(),
        data.getSecond())).seq((seq, result) -> {
      FixedNumeric fixedNumeric = FixedNumeric.using(seq);
      return DRes
          .of(result.getBeta().stream().map(fixedNumeric::open).collect(Collectors.toList()));
    }).seq((seq, data) -> DRes.of(data.stream().map(DRes::out).collect(Collectors.toList())));
  }
}
