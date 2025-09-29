import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Solution 5: Single-thread branchless version, scan 4 bytes at a time
 * <p>
 * 1. Normal execution                =~ 55ms
 * 2. Compile into native (25-graal)  =~ 7.9ms
 */
public class Solution5 {

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0-999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  /**
   * Finds the linebreak pos in an integer
   * @param word 4 bytes of chunk in integer
   * @return 0 to 3. If non found returns 4
   */
  static int linebreakPos(int word) {
    // hasvalue & haszero
    // adapted from https://graphics.stanford.edu/~seander/bithacks.html#ZeroInWord
    final int hasVal = word ^ 0xA0A0A0A; // hasvalue, 0xA0A0A0A is "\n\n\n\n" in text
    final int hasZero = ((hasVal - 0x01010101) & ~hasVal & 0x80808080); // haszero
    return Integer.numberOfLeadingZeros(hasZero) >>> 3;
  }

  static int parseFourDigits(int val) {
    int x = val - 0x30303030; // [1,2,3,4]

    int d0 =  x & 0xFF; // first char LBS
    int d1 = (x >>> 8) & 0xFF;
    int d2 = (x >>> 16) & 0xFF;
    int d3 = (x >>> 24) & 0xFF; // first in HBS
    // calculate the number based on HBS
    return (d3 * 1000 + d2 * 100) + (d1 * 10 + d0); // hoping each side in parantheses instructed in parallel :)
  }

  static int maskTopNBytes(int n) {
    // no safety checks by intention, assuming n is always 0 < n < 4
    return -(1 << ((n) * 8));
  }

  static int zeroCharMask(int n) {
    // no safety checks by intention, assuming n is always 0 < n < 4
    return 0x30303030 & (1 << (n * 8)) - 1;
  }

  public static void main(String[] args) throws Exception {

    final String inputFile = args[0];

    try (final var file = new RandomAccessFile(inputFile, "r")) {
      final FileChannel channel = file.getChannel();
      final long fileSize = channel.size();
      final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize); // we don't care about channel size here since can be max ~= 4M < Integer.MAX_VALUE

      int pos = 0;
      int chunk;
      int linebreakPos;
      final long limit = fileSize - 4; // safe ending for 4 byte chunks
      while (pos < limit) {
        chunk = buffer.getInt(pos);
        linebreakPos = linebreakPos(chunk);
        pos += linebreakPos + 1;

        final int n = 4 - linebreakPos; // zero count
        int number = (chunk & maskTopNBytes(n)) + zeroCharMask(n); // swap all '\n' and the rest with '0'
        number = Integer.rotateRight(number, n*8); // rotate zeros to collect them in higher bits
        number = parseFourDigits(number); // parsing ignores zeros since they're in higher bits, ex: "0062" => 2*1 + 6*10 + 0*100 + 0*1000 = 62
        NUMBER_MAP[number]++; // increment
      }

      byte b;
      int number = 0;
      while (pos < fileSize) { // read the leftovers, byte by byte
        if ((b = buffer.get(pos++)) == '\n') { // read and check each byte
          NUMBER_MAP[number]++; // increment
          number = 0;
          continue;
        }
        number = 10 * number + (b-'0');
      }

      int found = 0;
      int maxOccurance = 0;
      // find max occured number out of hotspot, it's O(n) where n is constant, ignorable
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
