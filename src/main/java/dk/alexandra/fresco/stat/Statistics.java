package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.SReal;
import dk.alexandra.fresco.stat.tests.LinearRegression.LinearFunction;
import java.util.List;

public interface Statistics extends ComputationDirectory {

  DRes<SReal> sampleMean(List<DRes<SReal>> data);

  DRes<SReal> sampleVariance(List<DRes<SReal>> data, DRes<SReal> mean);
  
  DRes<SReal> sampleVariance(List<DRes<SReal>> data);

  DRes<SReal> sampleStandardDeviation(List<DRes<SReal>> data, DRes<SReal> mean);
  
  DRes<SReal> sampleStandardDeviation(List<DRes<SReal>> data);

  DRes<SReal> ttest(List<DRes<SReal>> data, DRes<SReal> mu);

  DRes<SReal> ttest(List<DRes<SReal>> data1, List<DRes<SReal>> data2);

  /**
   * Compute the test statistics for a &Chi;<sup>2</sup>-test.
   *  
   * @param observed
   * @param expected
   * @return
   */
  DRes<SReal> chiSquare(List<DRes<SInt>> observed, List<DRes<SReal>> expected);

  DRes<LinearFunction> linearRegression(List<DRes<SReal>> x, List<DRes<SReal>> y);

  DRes<LinearFunction> linearRegression(List<DRes<SReal>> x, DRes<SReal> meanX, List<DRes<SReal>> y,
      DRes<SReal> meanY);
  
  DRes<SReal> correlation(List<DRes<SReal>> data1, DRes<SReal> mean1, List<DRes<SReal>> data2, DRes<SReal> mean2);
  
  DRes<SReal> correlation(List<DRes<SReal>> data1, List<DRes<SReal>> data2);
}
