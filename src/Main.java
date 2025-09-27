import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 0. First try, single thread approach using HashMap =~ 90ms
 * 1. Change HashMap by a custom integer array        =~ 36ms
 */
public class Main {

  private static final int ELEMENT_COUNT = Integer.getInteger("elements", 100);
  private static final int THREAD_COUNT = Integer.getInteger("threads", Runtime.getRuntime().availableProcessors());

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
  private static final int[] NUMBER_MAP = new int[1000];

  // hasvalue & haszero
  // adapted from https://graphics.stanford.edu/~seander/bithacks.html#ZeroInWord
  private static long hasLinebreak(long word) {
    // semicolon pattern
    final long hasVal = word ^ 0xA0A0A0A0A0A0A0AL; // hasvalue
    return ((hasVal - 0x0101010101010101L) & ~hasVal & 0x8080808080808080L); // haszero
  }

  private static int linebreakPos(long hasVal) {
    return Long.numberOfTrailingZeros(hasVal) >>> 3;
  }

  private static String toBinaryString(long i) {
    return "0".repeat(Long.numberOfLeadingZeros(i != 0 ? i : 1)) + Long.toBinaryString(i);
  }

  public static boolean hasZero(long v) {
    return ((v - 0x0101010101010101L) & ~v & 0x8080808080808080L) != 0;
  }

  public static boolean hasValue(long x, byte n) {
    final long mask = x ^ compilePattern(n);
    return hasZero(mask);
  }

  static long SEMICOLON_PATTERN = compilePattern((byte) ';');
  static long compilePattern(byte byteToFind) {
    final long pattern = byteToFind & 0xFFL;
    return pattern
        | (pattern << 8)
        | (pattern << 16)
        | (pattern << 24)
        | (pattern << 32)
        | (pattern << 40)
        | (pattern << 48)
        | (pattern << 56);
  }

  static long parse_eight_digits_unrolled(long val) {
    long mask = 0x000000FF000000FFL;
    long mul1 = 0x000F424000000064L; // 100 + (1000000ULL << 32)
    long mul2 = 0x0000271000000001L; // 1 + (10000ULL << 32)
    val -= 0x3030303030303030L;
    val = (val * 10) + (val >> 8); // val = (val * 2561) >> 8;
    val = (((val & mask) * mul1) + (((val >> 16) & mask) * mul2)) >> 32;
    return val;
  }

  public static void main(String[] args) {

//    System.out.println(Long.toHexString(compilePattern((byte) '\n')));

    System.out.println(parse_eight_digits_unrolled(ByteBuffer.wrap("12345678".getBytes()).order(ByteOrder.LITTLE_ENDIAN).getLong()));
    System.out.println(parse_eight_digits_unrolled(ByteBuffer.wrap("12340000".getBytes()).order(ByteOrder.LITTLE_ENDIAN).getLong()));
    System.out.println(parse_eight_digits_unrolled(ByteBuffer.wrap("00005678".getBytes()).order(ByteOrder.LITTLE_ENDIAN).getLong()));
    System.out.println(parse_eight_digits_unrolled(ByteBuffer.wrap("00000008".getBytes()).order(ByteOrder.LITTLE_ENDIAN).getLong()));


    System.out.println("-------------------");
    final long aLong = ByteBuffer.wrap("66502310".getBytes()).order(ByteOrder.LITTLE_ENDIAN).getLong();
    System.out.println(parse_eight_digits_unrolled(aLong));
    System.out.println(parse_eight_digits_unrolled((aLong << (5 * 8)) | 0x3030303030L)); // 0x30 = '0', fill the lower 5 bytes with '0'
    System.out.println(parse_eight_digits_unrolled(((aLong >> 8) | 0x3000000000000000L)) / 10); // 0x30 = '0', fill the lower 5 bytes with '0'

    System.out.println("-------------------");
    long test = 66502310;
    System.out.println(test + ": ------------------>");
    System.out.println(test % 10); // 0
    test /= 10; // 6650231
    System.out.println(test % 10000); // 231
    test /= 10000; // 665
    System.out.println(test);

    System.out.println("-------------------");
    long test2 = 60502010;
    System.out.println(test2 + ": ------------------>");
    System.out.println(test2 % 10); // 0
    test2 /= 10; // 6050201
    System.out.println(test2 % 100); // 1
    test2 /= 100; // 60502
    System.out.println(test2 % 100); // 2
    test2 /= 100; // 605
    System.out.println(test2 % 100); // 5
    test2 /= 100; // 6
    System.out.println(test2);

    System.out.println("-------------------");
    long test3 = 66042012;
    System.out.println(test3 + ": ------------------>");
    System.out.println(test3 % 1000); // 12
    test3 /= 1000; // 66042
    System.out.println(test3 % 1000); // 42
    test3 /= 1000; // 66
    System.out.println(test3);

  }
}
