package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.mimc.MiMCEncryption;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Assuming that the input data is sorted, this computation outputs the ranks of the elements using the given strategy.
 * The computation leaks what indices of elements are equal in the dataset.
 */
public class LeakyBreakTies implements Computation<List<Double>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> data;

  public LeakyBreakTies(List<DRes<SInt>> sortedData) {
    this.data = sortedData;
  }

  @Override
  public DRes<List<Double>> buildComputation(ProtocolBuilderNumeric builder) {

    // generate encryption key
    DRes<SInt> mimcKey = builder.numeric().randomElement();

    return builder.par(par -> {
      List<DRes<BigInteger>> ciphers = new ArrayList<>(data.size());
      for (final DRes<SInt> entry : data) {
        DRes<BigInteger> openedCipher =
            par.seq(
                (seq -> {
                  DRes<SInt> cipherText = seq.seq(new MiMCEncryption(entry, mimcKey));
                  return seq.numeric().open(cipherText);
                }));
        ciphers.add(openedCipher);
      }
      return () -> ciphers;
    }).seq((seq, ciphers) -> {

      List<Double> ranks = new ArrayList<>();

      int i = 1;
      while (i <= ciphers.size()) {

        int k;
        for (k = 1; k < ciphers.size() - i + 1; k++) {
          if (!ciphers.get(i + k - 1).out().equals(ciphers.get(i - 1).out())) {
            break;
          }
        }

        for (int j = 0; j < k; j++) {
          ranks.add(i + (k - 1.0) / 2.0);
        }

        i += k;
      }

      return () -> ranks;
    });
  }
}
