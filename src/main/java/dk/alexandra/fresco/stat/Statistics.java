package dk.alexandra.fresco.stat;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.lib.real.SReal;
import java.util.List;

public interface Statistics extends ComputationDirectory {

  DRes<SReal> mean(List<DRes<SReal>> data);

  DRes<SReal> variance(List<DRes<SReal>> data);

  DRes<SReal> ssd(List<DRes<SReal>> data);

  DRes<SReal> ttest(List<DRes<SReal>> data, DRes<SReal> mu);

  DRes<SReal> ttest(List<DRes<SReal>> data1, List<DRes<SReal>> data2);
}
