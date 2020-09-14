package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class InvertPermutation implements Computation<Matrix<DRes<SInt>>, ProtocolBuilderNumeric> {

  final private DRes<Matrix<DRes<SInt>>> values;
  final private Random rand;

  public InvertPermutation(DRes<Matrix<DRes<SInt>>> values, Random rand) {
    super();
    this.values = values;
    this.rand = rand;
  }

  public InvertPermutation(DRes<Matrix<DRes<SInt>>> values) {
    this(values, new SecureRandom());
  }

  private int[] getIdxPerm(int n) {
    List<Integer> indices = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      indices.add(i);
    }
    Collections.shuffle(indices, rand);
    int[] idxPerm = new int[indices.size()];
    for (int i = 0; i < indices.size(); i++) {
      idxPerm[indices.get(i)] = i;
    }
    return idxPerm;
  }

  private int[] getInverseIdxPerm(int[] idxPerm) {
    int[] inverseIdxPerm = new int[idxPerm.length];
    for (int i = 0; i < idxPerm.length; i++) {
      inverseIdxPerm[i] = i;
    }
    return inverseIdxPerm;
  }

  @Override
  public DRes<Matrix<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    /*
     * There is a round for each party in pids. Each party chooses a random permutation (of indexes)
     * and applies it to values using PermuteRows.
     */

    Matrix<DRes<SInt>> valuesOut = values.out();
    final int height = valuesOut.getHeight();
    if (height < 2) {
      return values;
    }
    final int pid = builder.getBasicNumericContext().getMyId();
    final int numPids = builder.getBasicNumericContext().getNoOfParties();

    int[] idxPerm = getIdxPerm(height);

    return builder.seq(
        (seq) -> new IterationState(0, values)
    ).whileLoop((state) -> state.round < numPids, (seq, state) -> {
      int thisRoundPid = state.round + 1; // parties start from 1
      DRes<Matrix<DRes<SInt>>> permuted;
      if (pid == thisRoundPid) {
        permuted = seq.collections().permute(state.intermediate, idxPerm);
      } else {
        permuted = seq.collections().permute(state.intermediate, thisRoundPid);
      }
      return new IterationState(state.round + 1, permuted);
    }).seq((seq, state) -> seq.collections().openMatrix(state.intermediate)).seq((seq, masked) -> {
      List<BigInteger> inverted = new ArrayList<>(Collections.nCopies(height, null));
      for (int i = 0; i < height; i++) {
        int idx = masked.getRow(i).get(0).out().intValue();
        inverted.set(idx, BigInteger.valueOf(i));
      }
      Matrix<BigInteger> invertedMatrix = new Matrix<>(height, 1,
          i -> new ArrayList<>(List.of(inverted.get(i))));
      return new IterationState(0, seq.collections().closeMatrix(invertedMatrix, 1));
    }).whileLoop(state -> state.round < numPids, (seq, state) -> {
      int thisRoundPid = state.round + 1; // reverse order of parties
      DRes<Matrix<DRes<SInt>>> permuted;
      if (pid == thisRoundPid) {
        permuted = seq.collections().permute(state.intermediate, idxPerm);
      } else {
        permuted = seq.collections().permute(state.intermediate, thisRoundPid);
      }
      return new IterationState(state.round + 1, permuted);
    }).seq((seq, state) -> state.intermediate);
  }

  private static final class IterationState implements DRes<IterationState> {

    private final int round;
    private final DRes<Matrix<DRes<SInt>>> intermediate;

    private IterationState(int round, DRes<Matrix<DRes<SInt>>> intermediate) {
      this.round = round;
      this.intermediate = intermediate;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
