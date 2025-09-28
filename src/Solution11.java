import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Solution 1: Single-thread with {@link MemorySegment} api
 * <p>
 * 0. First try, single thread approach using HashMap =~ 125ms
 * 1. Change HashMap by a custom integer array        =~ 85ms
 */
public class Solution11 {

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  public static void main(String[] args) throws Exception {

    final Path inputFile = Path.of(args[0]);

    try (final FileChannel channel = (FileChannel) Files.newByteChannel(inputFile, StandardOpenOption.READ)) {
      final long fileSize = channel.size();
      final MemorySegment segment = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize, Arena.global());

      int current = 0; // current number
      int maxOccurance = 0;
      int found = 0;
      byte b;
      int pos = 0;
      while (pos < fileSize) {
        if ((b = segment.get(ValueLayout.JAVA_BYTE, pos++)) == '\n') { // read & check each byte
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
