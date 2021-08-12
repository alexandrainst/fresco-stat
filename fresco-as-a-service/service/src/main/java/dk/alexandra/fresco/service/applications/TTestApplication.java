package dk.alexandra.fresco.service.applications;

import static dk.alexandra.fresco.service.applications.Utils.input;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.service.messages.MPCInputOuterClass.MPCInput;
import dk.alexandra.fresco.service.messages.TTest.TTestInput;
import dk.alexandra.fresco.stat.Statistics;
import java.math.BigDecimal;
import java.util.ArrayList;

public class TTestApplication implements
    Application<BigDecimal, ProtocolBuilderNumeric> {

  private final TTestInput input;

  public TTestApplication(TTestInput input) {
    this.input = input;
  }

  @Override
  public DRes<BigDecimal> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      ArrayList<DRes<SFixed>> x = new ArrayList<>();
      ArrayList<DRes<SFixed>> y = new ArrayList<>();

      if (input.getX().getValuesCount() != input.getY().getValuesCount()) {
        throw new IllegalArgumentException("");
      }

      for (MPCInput xi : input.getX().getValuesList()) {
        x.add(input(xi, par));
      }
      for (MPCInput yi : input.getY().getValuesList()) {
        y.add(input(yi, par));
      }

      return Pair.lazy(x, y);
    }).seq((seq, data) -> Statistics.using(seq).ttest(data.getFirst(),
        data.getSecond())).seq((seq, result) -> FixedNumeric.using(seq).open(result));
  }
}
