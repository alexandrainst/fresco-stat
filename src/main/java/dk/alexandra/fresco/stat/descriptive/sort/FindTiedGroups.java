package dk.alexandra.fresco.stat.descriptive.sort;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.mimc.MiMCEncryption;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FindTiedGroups implements Computation<List<BigInteger>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> data;

  /**
   * Returns a list of integers. If the i'th and j'th elements are the same in the result, it
   * indicates that the i'th and j'th elements are equal in the input data.
   */
  public FindTiedGroups(List<DRes<SInt>> data) {
    this.data = data;
  }

  @Override
  public DRes<List<BigInteger>> buildComputation(ProtocolBuilderNumeric builder) {
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
      return DRes.of(ciphers);
    }).seq((seq, ciphers) -> () -> ciphers.stream().map(DRes::out).collect(Collectors.toList()));
  }
}
