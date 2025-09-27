public class Test {

  public static long nBytesFromXRestY(int n, long x, long y) {
    long mask = ~0L << (n * 8);
    return (x & mask) | ( y & ~mask);
  }

  public static void main(String[] args) {

    int occurance = 1000000;
    int value = 999;

    final long compact = ((long) occurance << 32) | value;
    System.out.println("compact = " + compact);

    long mask = ~0L << (4 * 8);
    System.out.println("occurance: " + ((compact & mask) >> 32));
    System.out.println("value: " + (compact & ~mask));
  }

}
