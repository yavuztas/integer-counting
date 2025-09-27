import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Solution 2: Concurrent processing with syncronization (lockless using CAS)
 * <p>
 * 1. Split the buffer and parse concurrently, synchronized by AtomicInteger =~ 120ms
 */
public class Solution2 {

  static final int THREAD_COUNT = Integer.getInteger("threads", Runtime.getRuntime().availableProcessors());

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
  static final AtomicInteger[] NUMBER_MAP = new AtomicInteger[1000];

  // Stores 2 integers in a long (occurance + number) to set 2 values atomically at the same time
  // lowest 32 bits   = number
  // highest 32 bits  = occurance
  static final AtomicLong maxOccuranceTuple = new AtomicLong(0);
  static final long OCCURANCE_MASK = ~0L << 32;
  static final long VALUE_MASK = ~OCCURANCE_MASK;

  /**
   * Store 2 integers into one long
   * @param hi integer to store in the higher 32 bits
   * @param lo integer to store in the lower 32 bits
   * @return compact long
   */
  static long compact(int hi, int lo) {
    return ((long) hi << 32) | lo;
  }

  static int getHigherBits(long compact) {
    return (int) ((compact & OCCURANCE_MASK) >> 32);
  }

  static int getLowerBits(long compact) {
    return (int) (compact & VALUE_MASK);
  }

  static int findSegmentStart(ByteBuffer buffer, int pos) {
    if (pos == 0)
      return 0;
    // read segment to backwards until find the first '\n'
    while (buffer.get(pos) != '\n') {
      pos--;
    }
    return pos + 1;
  }

  static int findSegmentSize(ByteBuffer buffer, int start, int segmentSize) {
    // find a safe ending when segments aren't evenly distributed
    if ((start + segmentSize) > buffer.capacity())
      return segmentSize - (start + segmentSize - buffer.capacity());
    if ((start + segmentSize) > (buffer.capacity() - segmentSize))
      return buffer.capacity() - start;
    return segmentSize;
  }

  static void trySetMaxOccuranceTuple(int occurance, int current) {
    final long tuple = maxOccuranceTuple.get(); // volatile get of tuple (occurance + value)
    if (occurance > getHigherBits(tuple)) { // get the higher bits of tuple, means occurance
      if (!maxOccuranceTuple.compareAndSet(tuple, compact(occurance, current))) {
        trySetMaxOccuranceTuple(occurance, current);
      }
    }
  }

  public static void main(String[] args) throws Exception {

    System.out.println("Threads: " + THREAD_COUNT);

    // populate number map
    for (int i = 0; i < NUMBER_MAP.length; i++) {
      NUMBER_MAP[i] = new AtomicInteger(0);
    }

    final String inputFile = args[0];
    final MappedByteBuffer buffer;
    try (final var file = new RandomAccessFile(inputFile, "r")) {
      final FileChannel channel = file.getChannel();
      buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()); // we don't care channel size here since can be max ~= 4M < Integer.MAX_VALUE
    }

    final Thread[] threads = new Thread[THREAD_COUNT];
    final int segmentSize = buffer.capacity() / THREAD_COUNT;

//    System.out.println("capacity: " +  buffer.capacity());
//    System.out.println("segmentSize: " + segmentSize);

    int start = 0;
    for (int i = 0; i < THREAD_COUNT; i++) {
      start = findSegmentStart(buffer, start);
      final int size = findSegmentSize(buffer, start, segmentSize);
      final MappedByteBuffer segment = buffer.slice(start, size);

//      byte[] out = new byte[segment.capacity()];
//      buffer.get(start, out);
//      System.out.println("Thread["+i+"] start: " + start + ", end: " + (start + size) + ", content: " + new String(out).replaceAll("\n", "-"));

      start += size; // update the next start pos

      final var t = (threads[i] = new Thread(() -> {
        byte b;
        int pos = 0; // relative to segment
        int current = 0; // current number
        while (pos++ < segment.limit()) {
          if ((b = segment.get()) == '\n') { // read & check each byte
            final int occurance = NUMBER_MAP[current].incrementAndGet(); // atomic increment
            trySetMaxOccuranceTuple(occurance, current);
            current = 0; // reset number
            continue;
          }
          current = 10 * current + (b-'0');
        }
      }));
      t.start();
    }

    for (Thread thread : threads) {
       thread.join(); // wait all threads to complete
    }

    // print result
    final long foundTuple = maxOccuranceTuple.get();
    System.out.println("Found " + getLowerBits(foundTuple) + ", max: " + getHigherBits(foundTuple));
  }

}
