import java.nio.file.Path;

/**
 * Solution 1: Single-thread with micro-optimizations
 * <p>
 * 0. First try, single thread approach using HashMap =~ 125ms
 * 1. Change HashMap by a custom integer array        =~ 85ms
 */
public class Solution0 {

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  public static void main(String[] args) throws Exception {

    final Path inputFile = Path.of(args[0]);

    // no-op

  }
}
