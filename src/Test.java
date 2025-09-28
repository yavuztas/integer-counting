import java.nio.ByteBuffer;

public class Test {

  static long compilePattern(byte byteToFind) {
    final long pattern = byteToFind & 0xFF;
    return pattern
        | (pattern << 8);
  }

  private static int hasLinebreakShort(short word) {
    final int hasVal = word ^ 0xA0A; // hasvalue, 0xA0A0A0A is "\n\n\n\n" in text
    final int hasZero = ((hasVal - 0x0101) & ~hasVal & 0x8080); // haszero
    return Integer.numberOfTrailingZeros(hasZero) >>> 3;
  }

  // hasvalue & haszero
  // adapted from https://graphics.stanford.edu/~seander/bithacks.html#ZeroInWord
  private static int hasLinebreak(int word) {
    final int hasVal = word ^ 0xA0A0A0A; // hasvalue, 0xA0A0A0A is "\n\n\n\n" in text
    return ((hasVal - 0x01010101) & ~hasVal & 0x80808080); // haszero
  }

  private static int linebreakPos(int hasVal) {
    return Integer.numberOfLeadingZeros(hasVal) >>> 3;
  }

  private static int toInt(byte[] b) {
    return (b[0] & 255) << 16 | (b[1] & 255) << 8 | (b[2] & 255);
  }

  private static int toInt2( byte[] b ) {
    return (b[2] & 255) << 16 | (b[1] & 255) << 8 | (b[0] & 255);
  }

  public static void main(String[] args) {

    final long linebreakPattern = compilePattern((byte) '\n');
    System.out.println(Long.toHexString(linebreakPattern));

    final int word = ByteBuffer.wrap("\n234".getBytes()).getInt();
    System.out.println(linebreakPos(hasLinebreak(word)));

    // Lookup table without parsing digits
//    System.out.println(toInt(ByteBuffer.wrap("000".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("009".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("010".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("099".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("100".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("101".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("102".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("110".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("120".getBytes()).array()));
//    System.out.println(toInt(ByteBuffer.wrap("999".getBytes()).array()));

    int chunk = ByteBuffer.wrap("462\n".getBytes()).getInt();
    int index = linebreakPos(hasLinebreak(chunk));
    System.out.println("index: " + index);
    System.out.println((byte) (chunk >> (3-index)*8)-'0');
    System.out.println((byte) (chunk >> (3-index+3)*8)-'0');
    System.out.println((byte) (chunk >> (3-index+2)*8)-'0');
    System.out.println((byte) (chunk >> (3-index+1)*8)-'0');

    chunk = ByteBuffer.wrap("46\n2".getBytes()).getInt();
    index = linebreakPos(hasLinebreak(chunk));
    System.out.println("index: " + index);
    System.out.println((byte) (chunk >> (3-index)*8)-'0');
    System.out.println(((byte) (chunk >> (3-index+2)*8))-'0');
    System.out.println(((byte) (chunk >> (3-index+1)*8))-'0');

    chunk = ByteBuffer.wrap("4\n62".getBytes()).getInt();
    index = linebreakPos(hasLinebreak(chunk));
    System.out.println("index: " + index);
    System.out.println((byte) (chunk >> (3-index)*8)-'0');
    System.out.println(((byte) (chunk >> (3-index+1)*8))-'0');

//    System.out.println((byte) (chunk >> 8)); // 3
//    System.out.println((byte) (chunk >> 16)); // 2
//    System.out.println((byte) (chunk >> 24)); // 1
//    System.out.println((byte) (chunk >> index*8)); // 1

    System.out.println("------------------------------------------");
    System.out.println(ByteBuffer.wrap("0\n".getBytes()).getShort()/256); // 48
    System.out.println(ByteBuffer.wrap("1\n".getBytes()).getShort()/256); // 49
    System.out.println(ByteBuffer.wrap("2\n".getBytes()).getShort()/256);
    System.out.println(ByteBuffer.wrap("3\n".getBytes()).getShort()/256);
    System.out.println(ByteBuffer.wrap("4\n".getBytes()).getShort()/256);
    System.out.println(ByteBuffer.wrap("5\n".getBytes()).getShort()/256);
    System.out.println(ByteBuffer.wrap("6\n".getBytes()).getShort()/256);
    System.out.println(ByteBuffer.wrap("7\n".getBytes()).getShort()/256);
    System.out.println(ByteBuffer.wrap("8\n".getBytes()).getShort()/256);
    System.out.println(ByteBuffer.wrap("9\n".getBytes()).getShort()/256); // 57

    System.out.println("------------------------------------------");
    System.out.println(ByteBuffer.wrap("\n0".getBytes()).getShort()%2048); // 2608
    System.out.println(ByteBuffer.wrap("\n1".getBytes()).getShort()%2048);
    System.out.println(ByteBuffer.wrap("\n2".getBytes()).getShort()%2048);
    System.out.println(ByteBuffer.wrap("\n3".getBytes()).getShort());
    System.out.println(ByteBuffer.wrap("\n4".getBytes()).getShort());
    System.out.println(ByteBuffer.wrap("\n5".getBytes()).getShort());
    System.out.println(ByteBuffer.wrap("\n6".getBytes()).getShort());
    System.out.println(ByteBuffer.wrap("\n7".getBytes()).getShort());
    System.out.println(ByteBuffer.wrap("\n8".getBytes()).getShort());
    System.out.println(ByteBuffer.wrap("\n9".getBytes()).getShort());

    System.out.println("------------------------------------------");
    short sh = ByteBuffer.wrap("42".getBytes()).getShort();
    System.out.println("index: " + hasLinebreakShort(sh));


  }

}
