package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequencyTable implements Computation<List<Pair<DRes<SInt>, DRes<BigInteger>>>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> data;

  public FrequencyTable(List<DRes<SInt>> data) {
    this.data = data;
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, DRes<BigInteger>>>> buildComputation(
      ProtocolBuilderNumeric builder) {
return builder.par(par -> {
  List<DRes<BigInteger>> ciphers = new ArrayList<>(data.size());
  for (final DRes<SInt> entry : data) {
    DRes<BigInteger> openedCipher =
        par.seq(
            (seq -> {
              // TODO: encryption should be on a directory
              DRes<SInt> cipherText = seq.seq(new MiMCEncryption(groupBy, mimcKey));
              return seq.numeric().open(cipherText);
            }));
    ciphers.add(openedCipher);
  }
  return () -> ciphers;
}).seq((seq, ciphers) -> {
      // use cipher texts to perform aggregation "in-the-clear"
      Map<BigInteger, BigInteger> groupedByCipher = new HashMap<>();

      for (BigInteger cipher : ciphers) {

        if (!groupedByCipher.containsKey(cipher)) {
          groupedByCipher.put(cipher, BigInteger.ONE);
        } else {
          groupedByCipher.put(cipher, subTotal);
        }
      }
      return () -> toMatrix(groupedByCipher, cipherToShare);
    });

    return null;
  }
}
