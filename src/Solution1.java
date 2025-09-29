import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Solution 1: Single-thread, MappedByteBuffer, byte-by-byte process
 * <p>
 * 0. First try, single thread approach using HashMap =~ 81ms
 * 1. Change HashMap by a custom integer array        =~ 52ms
 * 2. Compile into native (25-graal)                  =~ 6.9ms
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
      byte b;
      int pos = 0;
      while (pos < fileSize) {
        if ((b = buffer.get(pos++)) == '\n') { // read and check each byte
          NUMBER_MAP[current]++; // increment
          current = 0;
          continue;
        }
        current = 10 * current + (b-'0');
      }

      int found = 0;
      int maxOccurance = 0;
      // find max occured number out of hotspot, it's O(k) where k = 1000, ignorable
      for (int i = 0; i < NUMBER_MAP.length; i++) {
        final var sum = NUMBER_MAP[i];
        if (sum > maxOccurance) {
          maxOccurance = sum;
          found = i;
        }
      }

      // print result
      System.out.println("Found " + found + ", max: " + maxOccurance);
    }
  }
}
