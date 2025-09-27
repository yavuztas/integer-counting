import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * Solution 1: Single-thread with micro-optimizations
 * <p>
 * 0. First try, single thread approach using HashMap =~ 90ms
 * 1. Change HashMap by a custom integer array        =~ 36ms
 */
public class Solution12 {

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  public static void main(String[] args) throws Exception {

    final long time = System.currentTimeMillis();

    final String inputFile = args[0];

    try (final var file = new RandomAccessFile(inputFile, "r")) {
      final FileChannel channel = file.getChannel();
      final long fileSize = channel.size();
      final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize); // we don't care channel size here since can be max ~= 4M < Integer.MAX_VALUE

      int current = 0; // current number
      int maxOccurance = 0;
      int found = 0;
      long word1;
      long word2;
      int pos = 0;
      while (pos < 128) {
        word1 = buffer.getLong(pos);
        word2 = buffer.getLong(pos + 8);

        System.out.println(printBinary(word1).replaceAll("\n", "-"));
        System.out.println(printBinary(word2).replaceAll("\n", "-"));

//        final int next = 10 * current + (b1-'0');
//        final int next2 = 10 * next + (b2-'0');
        pos += 16;
      }
      // print result
      System.out.println("Took: " + (System.currentTimeMillis() - time) + "ms");
      System.out.println("Found: " + found + ", max: " + maxOccurance);
    }
  }

  private static String printBinary(long l) {
    final byte[] result = ByteBuffer.allocate(8).putLong(l).array();
    return new String(result, StandardCharsets.UTF_8);
  }

}
