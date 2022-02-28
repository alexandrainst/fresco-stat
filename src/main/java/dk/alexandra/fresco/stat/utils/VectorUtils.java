package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.FixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
  public static <T> ArrayList<T> listBuilder(int size, IntFunction<T> generator) {

    ArrayList<T> list = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      list.add(generator.apply(i));
    }
    return list;
  }

  /**
   * Scale all values in the given vector by the scalar.
   *
   * @param vector  A secret vector.
   * @param scalar  A secret scalar.
   * @param builder The builder to use.
   * @return A scaled vector.
   */
  public static ArrayList<DRes<SFixed>> scale(List<DRes<SFixed>> vector, DRes<SFixed> scalar,
      ProtocolBuilderNumeric builder) {
    return entrywiseUnaryOp(vector, (x, bld) -> FixedNumeric.using(bld).mult(scalar, x), builder);
  }

  /**
   * Scale all values in the given vector by the scalar.
   *
   * @param vector  A secret vector.
   * @param scalar  A public scalar.
   * @param builder The builder to use.
   * @return A scaled vector.
   */
  public static ArrayList<DRes<SFixed>> scale(List<DRes<SFixed>> vector, double scalar,
      ProtocolBuilderNumeric builder) {
    return entrywiseUnaryOp(vector, (x, b) -> FixedNumeric.using(b).mult(scalar, x), builder);
  }

  /**
   * Scale all values in the given vector by the scalar.
   *
   * @param vector  A secret vector.
   * @param scalar  A public scalar.
   * @param builder The builder to use.
   * @return A scaled vector.
   */
  public static ArrayList<DRes<SInt>> scaleInt(List<DRes<SInt>> vector, DRes<SInt> scalar,
      ProtocolBuilderNumeric builder) {
    return entrywiseUnaryOp(vector, (x, b) -> b.numeric().mult(scalar, x), builder);
  }


  /**
   * Divide all values in the given vector by the scalar.
   *
   * @param vector  A secret vector.
   * @param scalar  A secret scalar.
   * @param builder The builder to use.
   * @return
   */
  public static ArrayList<DRes<SFixed>> div(List<DRes<SFixed>> vector, DRes<SFixed> scalar,
      ProtocolBuilderNumeric builder) {
    return entrywiseUnaryOp(vector, (x, b) -> FixedNumeric.using(b).div(x, scalar), builder);
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
    return entrywiseBinaryOp(a, b, (x, y, bld) -> FixedNumeric.using(bld).add(x, y), builder);
  }

  /**
   * Add a list of secret vectors.
   *
   * @param a       A list of secret vectors.
   * @param builder The builder to use.
   * @return sum(a)
   */
  public static List<DRes<SFixed>> sum(List<List<DRes<SFixed>>> a,
      ProtocolBuilderNumeric builder) {
    int dimension = a.get(0).size();
    for (int i = 1; i < a.size(); i++) {
      if (a.get(i).size() != dimension) {
        throw new IllegalArgumentException("Vector size mismatch");
      }
    }
    List<DRes<SFixed>> output = new ArrayList<>(dimension);
    builder.par(par -> {
      AdvancedFixedNumeric advancedFixedNumeric = AdvancedFixedNumeric.using(par);
      for (int i = 0; i < dimension; i++) {
        int finalI = i;
        output.add(advancedFixedNumeric.sum(a.stream().map(entry -> entry.get(finalI)).collect(
            Collectors.toList())));
      }
      return null;
    });
    return output;
  }


  /**
   * Subtract two secret vectors.
   *
   * @param a       A secret vector.
   * @param b       A secret scalar.
   * @param builder The builder to use.
   * @return a-b
   */
  public static ArrayList<DRes<SFixed>> sub(List<DRes<SFixed>> a, List<DRes<SFixed>> b,
      ProtocolBuilderNumeric builder) {
    return entrywiseBinaryOp(a, b, (x, y, bld) -> FixedNumeric.using(bld).sub(x, y), builder);
  }

  /**
   * Compute the entry-wise binary negation of a secret vector
   *
   * @param a       A secret 0-1-vector.
   * @param builder The builder to use.
   * @return not a
   */
  public static ArrayList<DRes<SInt>> negate(List<DRes<SInt>> a,
      ProtocolBuilderNumeric builder) {
    return entrywiseUnaryOp(a, (x, b) -> b.numeric().sub(1, x), builder);
  }

  public static ArrayList<DRes<BigInteger>> open(List<DRes<SInt>> a,
      ProtocolBuilderNumeric builder) {
    return entrywiseUnaryOp(a, (x, b) -> b.numeric().open(x), builder);
  }


  /**
   * Subtract two secret vectors.
   *
   * @param a                      A secret vector.
   * @param b                      A secret scalar.
   * @param protocolBuilderNumeric The builder to use.
   * @return a.*b
   */
  public static List<DRes<SInt>> mult(List<DRes<SInt>> a, List<DRes<SInt>> b,
      ProtocolBuilderNumeric protocolBuilderNumeric) {
    return entrywiseBinaryOp(a, b, (x, y, builder) -> builder.numeric().mult(x, y),
        protocolBuilderNumeric);
  }

  public static <A, B, C> ArrayList<C> entrywiseBinaryOp(List<A> a, List<B> b,
      EntrywiseBinaryOp<A, B, C> op, ProtocolBuilderNumeric builder) {
    ArrayList<C> result = new ArrayList<>();
    builder.par(par -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vector size mismatch");
      }
      for (int i = 0; i < a.size(); i++) {
        result.add(op.apply(a.get(i), b.get(i), par));
      }
      return null;
    });
    return result;
  }


  public static <A, B, C> ArrayList<C> entrywiseBinaryOp(List<A> a, List<B> b,
      BiFunction<A, B, C> op) {
    ArrayList<C> result = new ArrayList<>();
    if (a.size() != b.size()) {
      throw new IllegalArgumentException("Vector size mismatch");
    }
    for (int i = 0; i < a.size(); i++) {
      result.add(op.apply(a.get(i), b.get(i)));
    }
    return result;
  }

  public static <A, C> ArrayList<C> entrywiseUnaryOp(List<A> a, EntrywiseUnaryOp<A, C> op,
      ProtocolBuilderNumeric builder) {
    ArrayList<C> result = new ArrayList<>();
    builder.par(par -> {
      for (int i = 0; i < a.size(); i++) {
        result.add(op.apply(a.get(i), par));
      }
      return null;
    });
    return result;
  }

  public static ArrayList<DRes<SInt>> input(List<BigInteger> list, int party,
      ProtocolBuilderNumeric builder) {
    ArrayList<DRes<SInt>> result = new ArrayList<>();
    builder.par(par -> {
      for (BigInteger x : list) {
        result.add(builder.numeric().input(x, party));
      }
      return null;
    });
    return result;
  }

  @FunctionalInterface
  public interface EntrywiseUnaryOp<A, C> {

    C apply(A a, ProtocolBuilderNumeric builder);
  }

  @FunctionalInterface
  public interface EntrywiseBinaryOp<A, B, C> {

    C apply(A a, B b, ProtocolBuilderNumeric builder);
  }

}
