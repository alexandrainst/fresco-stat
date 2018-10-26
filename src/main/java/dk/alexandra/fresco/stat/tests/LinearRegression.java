package dk.alexandra.fresco.stat.tests;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.descriptive.Mean;
import dk.alexandra.fresco.stat.tests.LinearRegression.LinearFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This computation returns coeffieints a and b based on a simple linear regression of the observed
 * x and y values.
 * 
 * TODO: We do not calculate the sample correlation coefficient r here, since this would make the
 * computation harder. Maybe we should do this in another computation?
 */
public class LinearRegression implements Computation<LinearFunction, ProtocolBuilderNumeric> {

  public class LinearFunction {
    
    private DRes<SReal> b;
    private DRes<SReal> a;

    private LinearFunction(DRes<SReal> a, DRes<SReal> b) {
      this.a = a;
      this.b = b;
    }
    
    public DRes<SReal> getA() {
      return a;
    }
    
    public DRes<SReal> getB() {
      return b;
    }
  }
  
  private List<DRes<SReal>> x;
  private List<DRes<SReal>> y;
  private DRes<SReal> xMean;
  private DRes<SReal> yMean;

  public LinearRegression(List<DRes<SReal>> x, List<DRes<SReal>> y) {
    this.x = x;
    this.y = y;
  }
  
  @Override
  public DRes<LinearFunction> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(par -> {
      DRes<SReal> xMean = par.seq(new Mean(x));
      DRes<SReal> yMean = par.seq(new Mean(y));
      return () -> new Pair<>(xMean, yMean);
    }).par((par, pair) -> {
      
      // TODO: We propably shouldn't keep these, btu we need them in the very end of the
      // calculation, and passing them through the rounds will make the code hard to read.
      this.xMean = pair.getFirst();
      this.yMean = pair.getSecond();
      
      List<DRes<SReal>> xTerms = x.stream().map(x -> par.realNumeric().sub(x, xMean))
          .collect(Collectors.toList());
      List<DRes<SReal>> yTerms = y.stream().map(y -> par.realNumeric().sub(y, yMean))
          .collect(Collectors.toList());
      return () -> new Pair<>(xTerms, yTerms);
    }).par((par, terms) -> {
      List<DRes<SReal>> numeratorTerms = new ArrayList<>();
      for (int i = 0; i < x.size(); i++) {
        numeratorTerms.add(par.realNumeric().mult(terms.getFirst().get(i), terms.getSecond().get(i)));
      }
      List<DRes<SReal>> denominatorTerms =
          terms.getFirst().stream().map(x -> par.realNumeric().mult(x, x)).collect(Collectors.toList());
      return () -> new Pair<>(numeratorTerms, denominatorTerms);
    }).par((par, terms) -> {
      DRes<SReal> numeratorSum = par.realAdvanced().sum(terms.getFirst());
      DRes<SReal> denominatorSum = par.realAdvanced().sum(terms.getSecond());
      return () -> new Pair<>(numeratorSum, denominatorSum);
    }).seq((seq, fraction) -> {
      DRes<SReal> b = seq.realNumeric().div(fraction.getFirst(), fraction.getSecond());
      DRes<SReal> a = seq.realNumeric().sub(yMean, seq.realNumeric().mult(b, xMean));
      return () -> new LinearFunction(a, b);
    });
  }
  
}
