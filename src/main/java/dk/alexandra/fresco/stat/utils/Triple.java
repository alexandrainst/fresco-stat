package dk.alexandra.fresco.stat.utils;

import dk.alexandra.fresco.framework.DRes;

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

}
