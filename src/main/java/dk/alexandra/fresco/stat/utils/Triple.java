package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.TransposeUtils;
import java.util.Objects;

public class Triple<A, B, C> {

  private final A first;
  private final B second;
  private final C third;

  public Triple(A first, B second, C third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public A getFirst() {
    return first;
  }

  public B getSecond() {
    return second;
  }

  public C getThird() {
    return third;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(first, triple.first) &&
        Objects.equals(second, triple.second) &&
        Objects.equals(third, triple.third);
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second, third);
  }

  @Override
  public String toString() {
    return "Triple{" +
        "first=" + first +
        ", second=" + second +
        ", third=" + third +
        '}';
  }

  public static <S, T, U> DRes<Triple<S, T, U>> lazy(S first, T second, U third) {
    return DRes.of(new Triple<>(first, second, third));
  }
}
