package dk.alexandra.fresco.stat.utils;

@FunctionalInterface
public interface TriFunction<A,B,C,D> {

  D apply(A a, B b, C c);

}
