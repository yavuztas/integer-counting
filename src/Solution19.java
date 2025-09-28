import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Solution 1: Single-thread with micro-optimizations
 * <p>
 * 0. First try, single thread approach using HashMap =~ 125ms
 * 1. Change HashMap by a custom integer array        =~ 85ms
 * 2. Compile into native (25-graal)                  =~ 15ms
 */
public class Solution19 {

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  /**
   * Finds the linebreak pos in an integer
   * @param word 4 bytes of chunk in integer
   * @return 0 to 3. If non found returns 4
   */
  private static int linebreakPos(int word) {
    // hasvalue & haszero
    // adapted from https://graphics.stanford.edu/~seander/bithacks.html#ZeroInWord
    final int hasVal = word ^ 0xA0A0A0A; // hasvalue, 0xA0A0A0A is "\n\n\n\n" in text
    final int hasZero = ((hasVal - 0x01010101) & ~hasVal & 0x80808080); // haszero
    return Integer.numberOfLeadingZeros(hasZero) >>> 3;
  }

  private static int linebreakPosShort(short word) {
    final int hasVal = word ^ 0xA0A; // hasvalue, 0xA0A0A0A is "\n\n\n\n" in text
    final int hasZero = ((hasVal - 0x0101) & ~hasVal & 0x8080); // haszero
    return Integer.numberOfTrailingZeros(hasZero) >>> 3;
  }

  public static void main(String[] args) throws Exception {

    final String inputFile = args[0];

    try (final var file = new RandomAccessFile(inputFile, "r")) {
      final FileChannel channel = file.getChannel();
      final long fileSize = channel.size();
      final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize); // we don't care channel size here since can be max ~= 4M < Integer.MAX_VALUE

      final long time = System.currentTimeMillis();

      int current = 0; // current number
      int pos = 0;
      final long limit = fileSize - 4; // safe ending for 4 byte chunks
      while (pos < limit) {
        final short chunk1 = buffer.getShort(pos);
        final short chunk2 = buffer.getShort(pos+2);
        final int linebreakPos1 = linebreakPosShort(chunk1) + pos;
        final int linebreakPos2 = linebreakPosShort(chunk2) + pos + 2;

        System.out.println(linebreakPos1 + " " + linebreakPos2);

        /*
        for (int i = linebreakPos; i > 0; i--) {
          current = 10 * current + ((byte) (chunk >> (3-linebreakPos+i)*8))-'0';
        }*/
        // int n = 100 * (b3-'0') + 10 * (b2-'0') + b1-'0';

        // NUMBER_MAP[current]++; // increment
        current = 0;
        pos += 4;
      }

      byte b;
      while (pos < fileSize) { // read the leftovers, byte by byte
        if ((b = buffer.get(pos++)) == '\n') { // read & check each byte
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

      System.out.println("took " + (System.currentTimeMillis() - time) + "ms");

      // print result
      System.out.println("Found " + found + ", max: " + maxOccurance);

    }
  }
}
