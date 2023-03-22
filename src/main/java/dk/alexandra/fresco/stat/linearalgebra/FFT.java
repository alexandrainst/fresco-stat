package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.stat.complex.*;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FFT implements Computation<ArrayList<DRes<SecretComplex>>, ProtocolBuilderNumeric> {

    private final ArrayList<DRes<SecretComplex>> x;

    public FFT(ArrayList<DRes<SecretComplex>> x) {
        int n = x.size();
        if ((n & n - 1) != 0) {
            throw new IllegalArgumentException("Vector size must be a power of two");
        }
        this.x = x;
    }

    private static OpenComplex exp(int k, int N) {
        return exp(-2.0 * k / N);
    }

    /**
     * e^{ix}
     */
    private static OpenComplex exp(double x) {
        return new OpenComplex(Math.cos(x), Math.sin(x));
    }

    @Override
    public DRes<ArrayList<DRes<SecretComplex>>> buildComputation(ProtocolBuilderNumeric builder) {

        int n = x.size();

        if (n == 1) {
            return DRes.of(x);
        }

        return builder.par(par -> {
            DRes<ArrayList<DRes<SecretComplex>>> even = new FFT(IntStream.iterate(0, i -> i < n, i -> i + 2).mapToObj(x::get).collect(Collectors.toCollection(ArrayList::new))).buildComputation(par);
            DRes<ArrayList<DRes<SecretComplex>>> odd = new FFT(IntStream.iterate(1, i -> i < n, i -> i + 2).mapToObj(x::get).collect(Collectors.toCollection(ArrayList::new))).buildComputation(par);
            return Pair.lazy(even, odd);
        }).par((par, results) -> {
            ArrayList<DRes<SecretComplex>> y = new ArrayList<>();
            y.addAll(results.getFirst().out());
            y.addAll(results.getSecond().out());
            for (int k = 0; k < n / 2; k++) {
                int finalK = k;
                par.seq(seq -> {
                    DRes<SecretComplex> p = y.get(finalK);
                    DRes<SecretComplex> q = new MultiplyOpen(y.get(finalK + n / 2).out(), exp(finalK, n)).buildComputation(seq);
                    return Pair.lazy(p, q);
                }).seq((seq, pq) -> {
                    SecretComplex p = pq.getFirst().out();
                    SecretComplex q = pq.getSecond().out();
                    y.set(finalK, new SecretAdd(p, q).buildComputation(seq));
                    y.set(finalK + n / 2, new SecretSubtract(p, q).buildComputation(seq));
                    return null;
                });
            }
            return DRes.of(y);
        });
    }
}
