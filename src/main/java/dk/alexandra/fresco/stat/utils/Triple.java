package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;
import java.util.Objects;

/** Instances of this class holds three values of arbitrary type. */
public class Triple<A, B, C> {

  private final A a;
  private final B b;
  private final C c;

  public Triple(A a, B b, C c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public static <E, F, G> Triple<E, F, G> of(E e, F f, G g) {
    return new Triple<>(e, f, g);
  }

  public static <E, F, G> DRes<Triple<E, F, G>> lazy(E e, F f, G g) {
    return DRes.of(new Triple<>(e, f, g));
  }

  public A getFirst() {
    return a;
  }

  public B getSecond() {
    return b;
  }

  public C getThird() {
    return c;
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

    if (!Objects.equals(a, triple.a)) {
      return false;
    }
    if (!Objects.equals(b, triple.b)) {
      return false;
    }
    return Objects.equals(c, triple.c);
  }

  @Override
  public int hashCode() {
    int result = a != null ? a.hashCode() : 0;
    result = 31 * result + (b != null ? b.hashCode() : 0);
    result = 31 * result + (c != null ? c.hashCode() : 0);
    return result;
  }
}
