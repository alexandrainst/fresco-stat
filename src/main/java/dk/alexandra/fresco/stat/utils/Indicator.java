package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Indicator implements Computation<SInt, ProtocolBuilderNumeric> {

  // Cache polynomials
  private static final Map<Triple<Integer, Integer, BigInteger>, List<BigInteger>> polynomials = new HashMap<>();
  private final int range;
  private final int filterValue;
  private final DRes<SInt> value;

  /**
   * Given a value <i>x</i> with <i>0 &le; x &lt; range</i> this computation returns <i>1</i> if <i>x =
   * filterValue</i> and <i>0</i> otherwise. If the value is not in the range, there are no
   * guarantees as to what the output is.
   */
  public Indicator(int range, int filterValue, DRes<SInt> value) {
    this.range = range;
    this.filterValue = filterValue;

    if (filterValue < 0 || filterValue >= range) {
      throw new IllegalArgumentException("Invalid filter value. Must be in range.");
    }

    this.value = value;

  }

  /**
   * Compute the product of two polynomials
   */
  private static List<BigInteger> polynomialMultiplication(List<BigInteger> x, List<BigInteger> y) {
    int degreeX = degree(x);
    int degreeY = degree(y);

    int degreeZ = degreeX + degreeY;
    List<BigInteger> z = new ArrayList<>(degreeZ + 1);
    for (int i = 0; i < degreeZ + 1; i++) {
      BigInteger c = BigInteger.ZERO;
      for (int j = 0; j < i + 1; j++) {
        c = c.add((j < x.size() ? x.get(j) : BigInteger.ZERO)
            .multiply(i - j < y.size() ? y.get(i - j) : BigInteger.ZERO));
      }
      z.add(c);
    }
    return z;
  }

  /**
   * Compute the degree of a polynomial
   */
  private static int degree(List<BigInteger> x) {
    int degree = 0;
    for (int i = 0; i < x.size(); i++) {
      if (!x.get(i).equals(BigInteger.ZERO)) {
        degree = i;
      }
    }
    return degree;
  }

  /**
   * Compute the coefficients of a polynomial <i>p</i> such that <i>p(filterValue) = 1</i> and
   * <i>p(x) = 0</i> for <i>0 &le; x /lt; range</i> and <i>x &ne; filterValue</i>.
   */
  static List<BigInteger> computeFilter(int range, int filterValue, BigInteger modulus) {

    if (polynomials.containsKey(new Triple<>(range, filterValue, modulus))) {
      return polynomials.get(new Triple<>(range, filterValue, modulus));
    }

    List<BigInteger> p = List.of(BigInteger.ONE);
    for (int i = 0; i < range; i++) {
      if (i == filterValue) {
        continue;
      }
      p = polynomialMultiplication(p, List.of(BigInteger.valueOf(-i), BigInteger.ONE));
    }

    BigInteger correction = evaluate(p, BigInteger.valueOf(filterValue), modulus)
        .modInverse(modulus);
    List<BigInteger> polynomial = p.stream().map(pi -> pi.multiply(correction).mod(modulus))
        .collect(
            Collectors.toList());
    polynomials.put(new Triple<>(range, filterValue, modulus), polynomial);
    return polynomial;
  }

  /**
   * Evaluate a polynomial on a given value <i>x</i>.
   */
  static BigInteger evaluate(List<BigInteger> polynomial, BigInteger x, BigInteger modulus) {
    BigInteger y = BigInteger.ZERO;
    for (int i = 0; i < polynomial.size(); i++) {
      y = y.add(polynomial.get(i).multiply(x.modPow(BigInteger.valueOf(i), modulus))).mod(modulus);
    }
    return y;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {

    // If there's only two values, the polynomial is x or 1 - x depending on the filter value
    if (range == 2) {
      if (filterValue == 1) {
        return value;
      } else {
        return builder.seq(seq -> seq.numeric().sub(1, value));
      }
    }

    List<BigInteger> polynomial = computeFilter(range, filterValue,
        builder.getBasicNumericContext().getModulus());

    return builder.seq(new PowerList(value, polynomial.size() - 1)).seq((seq, powers) ->
        seq.numeric().add(polynomial.get(0), AdvancedNumeric.using(seq)
            .innerProductWithPublicPart(polynomial.subList(1, polynomial.size()), powers))
    );

  }
}
