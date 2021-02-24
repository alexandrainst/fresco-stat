package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.util.Pair;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class LoadEnumerated {

  public static Pair<ArrayList<ArrayList<BigInteger>>, ArrayList<ArrayList<String>>> readFromCSV(String file, int limit,
      List<Integer> enumerate, List<Integer> preserve) throws IOException {

    ArrayList<ArrayList<String>> mappings = new ArrayList<>();
    for (int i = 0; i < enumerate.size(); i++) {
      mappings.add(new ArrayList<>());
    }

    Reader in = new FileReader(file);
    Iterable<CSVRecord> records = CSVFormat.DEFAULT
        .parse(in);

    ArrayList<ArrayList<BigInteger>> output = new ArrayList<>();
    for (CSVRecord record : records) {
      ArrayList<BigInteger> row = new ArrayList<>();
      for (int i = 0; i < record.size(); i++) {
        if (preserve.contains(i)) {
          row.add(new BigInteger(record.get(i)));
        } else if (enumerate.contains(i)) {
          int j = enumerate.indexOf(i);
          if (!mappings.get(j).contains(record.get(i))) {
            mappings.get(j).add(record.get(i));
            row.add(BigInteger.valueOf(mappings.get(j).size()));
          } else {
            row.add(BigInteger.valueOf(mappings.get(j).indexOf(record.get(i)) + 1));
          }
        }
      }
      output.add(row);
      if (output.size() >= limit) {
        break;
      }
    }
    in.close();

    return new Pair<>(output, mappings);
  }

}
