package dk.alexandra.fresco.stat.linearalgebra;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class VectorUtils {

  /**
   * Compute the inner product of a public vector with a secret bit vector.
   *
   * @param a       A secret bit vector
   * @param b       A public vector
   * @param builder The builder to use.
   * @return The inner product of a and b.
   */
  public static DRes<SFixed> innerProductWithBitvectorPublic(List<DRes<SInt>> a, List<Double> b,
      ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {

      /*
       * Exploit that we are working with fixed point numbers to avoid truncation for each
       * multiplication.
       */

      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      /* See dk.alexandra.fresco.lib.fixed.fixed.FixedNumeric.unscaled */
      List<BigInteger> bFixed =
          b.stream().map(x -> BigDecimal.valueOf(x).multiply(new BigDecimal(
              BigInteger.valueOf(2)
                  .pow(builder.getBasicNumericContext().getDefaultFixedPointPrecision())))
              .setScale(0, RoundingMode.HALF_UP)
              .toBigIntegerExact()).collect(Collectors.toList());
      DRes<SInt> innerProduct = AdvancedNumeric.using(seq).innerProductWithPublicPart(bFixed, a);

      /* No truncation needed */
      return new SFixed(innerProduct);
    });
  }

  /**
   * Compute the inner product of a secret vector with a secret bit vector.
   *
   * @param a       A secret bit vector
   * @param b       A secret vector
   * @param builder The builder to use.
   * @return The inner product of a and b.
   */
  public static DRes<SFixed> innerProductWithBitvector(List<DRes<SInt>> a, List<DRes<SFixed>> b,
      ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {

      /*
       * Exploit that we are working with fixed point numbers to avoid truncation for each
       * multiplication.
       */

      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }

      /* See dk.alexandra.fresco.lib.fixed.fixed.FixedNumeric.unscaled */
      List<DRes<SInt>> bFixed =
          b.stream().map(x -> x.out().getSInt()).collect(Collectors.toList());
      DRes<SInt> innerProduct = AdvancedNumeric.using(seq).innerProduct(bFixed, a);

      /* No truncation needed */
      return new SFixed(innerProduct);
    });
  }

  /**
   * Build a list of the given size using a generator. Note that this is <i>not</i> lazy -- the
   * generator is called for all indices when this is called.
   *
   * @param size      The size of the list.
   * @param generator The generator used to create the entries.
   * @param <T>
   * @return A list containg the created elements.
   */
  public static <T> List<T> listBuilder(int size, IntFunction<T> generator) {

    List<T> list = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      list.add(generator.apply(i));
    }

    return new AbstractList<T>() {

      @Override
      public T get(int index) {
        return list.get(index);
      }

      @Override
      public int size() {
        return size;
      }
    };
  }

  /**
   * Scale all values in the given vector by the scalar.
   *
   * @param vector  A secret vector.
   * @param scalar  A secret scalar.
   * @param builder The builder to use.
   * @return A scaled vector.
   */
  public static List<DRes<SFixed>> scale(List<DRes<SFixed>> vector, DRes<SFixed> scalar,
      ProtocolBuilderNumeric builder) {
    List<DRes<SFixed>> result = new ArrayList<>();
    builder.par(par -> {
      for (int i = 0; i < vector.size(); i++) {
        result.add(FixedNumeric.using(par).mult(vector.get(i), scalar));
      }
      return () -> result;
    });
    return result;
  }

  /**
   * Scale all values in the given vector by the scalar.
   *
   * @param vector  A secret vector.
   * @param scalar  A public scalar.
   * @param builder The builder to use.
   * @return A scaled vector.
   */
  public static List<DRes<SFixed>> scale(List<DRes<SFixed>> vector, double scalar,
      ProtocolBuilderNumeric builder) {
    List<DRes<SFixed>> result = new ArrayList<>();
    builder.par(par -> {
      for (int i = 0; i < vector.size(); i++) {
        result.add(FixedNumeric.using(par).mult(scalar, vector.get(i)));
      }
      return () -> result;
    });
    return result;
  }

  /**
   * Divide all values in the given vector by the scalar.
   *
   * @param vector  A secret vector.
   * @param scalar  A secret scalar.
   * @param builder The builder to use.
   * @return
   */
  public static List<DRes<SFixed>> div(List<DRes<SFixed>> vector, DRes<SFixed> scalar,
      ProtocolBuilderNumeric builder) {
    List<DRes<SFixed>> result = new ArrayList<>();
    builder.par(par -> {
      for (int i = 0; i < vector.size(); i++) {
        result.add(FixedNumeric.using(par).div(vector.get(i), scalar));
      }
      return () -> result;
    });
    return result;
  }

  /**
   * Add two secret vectors.
   *
   * @param a       A secret vector.
   * @param b       A secret scalar.
   * @param builder The builder to use.
   * @return a+b
   */
  public static List<DRes<SFixed>> add(List<DRes<SFixed>> a, List<DRes<SFixed>> b,
      ProtocolBuilderNumeric builder) {
    List<DRes<SFixed>> result = new ArrayList<>();
    builder.par(par -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vector size mismatch");
      }
      for (int i = 0; i < a.size(); i++) {
        result.add(FixedNumeric.using(par).add(a.get(i), b.get(i)));
      }
      return null;
    });
    return result;
  }

  /**
   * Subtract two secret vectors.
   *
   * @param a       A secret vector.
   * @param b       A secret scalar.
   * @param builder The builder to use.
   * @return a-b
   */
  public static List<DRes<SFixed>> sub(List<DRes<SFixed>> a, List<DRes<SFixed>> b,
      ProtocolBuilderNumeric builder) {
    List<DRes<SFixed>> result = new ArrayList<>();
    builder.par(par -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vector size mismatch");
      }
      for (int i = 0; i < a.size(); i++) {
        result.add(FixedNumeric.using(par).sub(a.get(i), b.get(i)));
      }
      return null;
    });
    return result;
  }
}
