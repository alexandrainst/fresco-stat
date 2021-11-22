package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.util.List;

public interface FilteredStatistics {

  static FilteredStatistics using(ProtocolBuilderNumeric builder) {
    return new DefaultFilteredStatistics(builder);
  }

  DRes<SFixed> sampleMean(List<DRes<SFixed>> data, List<DRes<SInt>> filter);

  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, DRes<SFixed> mean, List<DRes<SInt>> filter);

  DRes<SFixed> sampleVariance(List<DRes<SFixed>> data, List<DRes<SInt>> filter);

  DRes<SFixed> ttest(List<DRes<SFixed>> data, DRes<SFixed> mu, List<DRes<SInt>> filter);

  DRes<SFixed> chiSquare(List<DRes<SInt>> observed, List<DRes<SFixed>> expected, List<DRes<SInt>> filter);

  DRes<List<DRes<SInt>>> histogramContinuous(double[] buckets, List<DRes<SFixed>> data, List<DRes<SInt>> filter);

}