/**
 * Interestingly, even an empty no-op class takes ~=30ms to spin up (JVM version)
 * Possibly, we can cut this cost via compiling into native
 */
public class Solution0 {

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0-999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  public static void main(String[] args) throws Exception {

    // no-op, just to measure java initialization time

  }
}
