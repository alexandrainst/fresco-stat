package dk.alexandra.fresco.stat.utils;

public class MathUtils {

  public static boolean isPowerOfTwo(int x) {
    return (x != 0) && ((x & (x - 1)) == 0);
  }

}
