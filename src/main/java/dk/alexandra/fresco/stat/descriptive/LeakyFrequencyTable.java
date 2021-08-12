package dk.alexandra.fresco.stat.descriptive;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.mimc.MiMCDecryption;
import dk.alexandra.fresco.lib.mimc.MiMCEncryption;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Compute the frequencies of entries in the given data. The set of frequencies will be leaked to
 * all parties but the corresponding values will be kept secret. If the data has not been
 * obliviously shuffled before this computation, the indices of equal values will be leaked.
 */
public class LeakyFrequencyTable implements
    Computation<List<Pair<DRes<SInt>, Integer>>, ProtocolBuilderNumeric> {

  private final List<DRes<SInt>> data;

  public LeakyFrequencyTable(List<DRes<SInt>> data) {
    this.data = data;
  }

  @Override
  public DRes<List<Pair<DRes<SInt>, Integer>>> buildComputation(
      ProtocolBuilderNumeric builder) {
    // We assume the data is obliviously shuffled before running this computation
    //DRes<List<DRes<SInt>>> shuffled = Collections.using(builder).shuffle().shuffle(data);

    // Generate encryption key
    DRes<SInt> mimcKey = builder.numeric().randomElement();

    return builder.par(par -> {
      List<DRes<BigInteger>> ciphers = new ArrayList<>(data.size());
      for (final DRes<SInt> entry : data) {
        DRes<BigInteger> openedCipher =
            par.seq(
                (seq -> {
                  DRes<SInt> cipherText = new MiMCEncryption(entry, mimcKey).buildComputation(seq);
                  return seq.numeric().open(cipherText);
                }));
        ciphers.add(openedCipher);
      }
      return DRes.of(ciphers);
    }).par((par, ciphers) -> {
      // use cipher texts to perform aggregation "in-the-clear"
      Map<BigInteger, Integer> groupedByCipher = new HashMap<>();

      for (DRes<BigInteger> cipher : ciphers) {
        BigInteger cipherOut = cipher.out();
        groupedByCipher.putIfAbsent(cipherOut, 0);
        groupedByCipher.computeIfPresent(cipherOut, (k, v) -> v + 1);
      }
      List<Pair<DRes<SInt>, Integer>> frequencies = groupedByCipher.keySet().stream()
          .map(v -> new Pair<>(
              new MiMCDecryption(par.numeric().known(v), mimcKey).buildComputation(par),
              groupedByCipher.get(v)))
          .collect(Collectors.toList());
      return DRes.of(frequencies);
    });
  }
}
