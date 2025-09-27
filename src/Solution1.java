import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Solution 1: Single-thread with micro-optimizations
 * <p>
 * 0. First try, single thread approach using HashMap =~ 125ms
 * 1. Change HashMap by a custom integer array        =~ 85ms
 */
public class Solution1 {

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  public static void main(String[] args) throws Exception {

    final String inputFile = args[0];

    try (final var file = new RandomAccessFile(inputFile, "r")) {
      final FileChannel channel = file.getChannel();
      final long fileSize = channel.size();
      final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize); // we don't care channel size here since can be max ~= 4M < Integer.MAX_VALUE

      int current = 0; // current number
      int maxOccurance = 0;
      int found = 0;
      byte b;
      int pos = 0;
      while (pos < fileSize) {
        if ((b = buffer.get(pos++)) == '\n') { // read & check each byte
          if (++NUMBER_MAP[current] > maxOccurance) { // increment and check
            maxOccurance = NUMBER_MAP[current];
            found = current;
          }
          current = 0;
          continue;
        }
        current = 10 * current + (b-'0');
      }
      // print result
      System.out.println("Found " + found + ", max: " + maxOccurance);
    }
  }
}
