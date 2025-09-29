import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Solution 2: Concurrent processing with syncronization (lockless using CAS)
 * <p>
 * 1. Parse concurrently, synchronized by AtomicInteger =~ 70ms
 * 2. Compile into native (25-graal)                    =~ 9.8ms (still not better than the single thread version)
 */
public class Solution2 {

  static final int THREAD_COUNT = Integer.getInteger("threads", 2 * Runtime.getRuntime().availableProcessors());

  // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
  // mapped one-to-one with indices is possible since our data set is limited [0-999] Therefore, this eleminates hash calculation because no clashes possible
  static final AtomicInteger[] NUMBER_MAP = new AtomicInteger[1000];

  static int findSegmentStart(ByteBuffer segment) {
    if (segment == null)
      return 0;
    // read the segment to backwards until find the first '\n'
    int pos = segment.limit() - 1;
    while (segment.get(pos) != '\n') {
      pos--;
    }
    return pos + 1;
  }

  static long findSegmentSize(long fileSize, long start, int segmentSize) {
    // find a safe ending when segments aren't evenly distributed
    if ((start + segmentSize) > fileSize)
      return segmentSize - (start + segmentSize - fileSize);
    if ((start + segmentSize) > (fileSize - segmentSize))
      return fileSize - start;
    return segmentSize;
  }

  public static void main(String[] args) throws Exception {

    // populate number map
    for (int i = 0; i < NUMBER_MAP.length; i++) {
      NUMBER_MAP[i] = new AtomicInteger(0);
    }

    final String inputFile = args[0];
    final FileChannel channel;
    final long fileSize;
    final Thread[] threads = new Thread[THREAD_COUNT];

    try (final var file = new RandomAccessFile(inputFile, "r")) {
      channel = file.getChannel();
      fileSize = channel.size();
      final int segmentSize = Math.toIntExact(fileSize / THREAD_COUNT); // each segment must be lower than Integer.MAX_VALUE
      long start = 0;
      MappedByteBuffer prev = null;
      for (int i = 0; i < THREAD_COUNT; i++) {
        start += findSegmentStart(prev); // update the next start pos
        final long size = findSegmentSize(fileSize, start, segmentSize);
        final MappedByteBuffer segment = channel.map(MapMode.READ_ONLY, start, size);
        final var t = (threads[i] = new Thread(() -> {
          byte b;
          int pos = 0; // relative to segment
          int current = 0; // current number
          while (pos++ < segment.limit()) {
            if ((b = segment.get()) == '\n') { // read and check each byte
              NUMBER_MAP[current].incrementAndGet(); // atomic increment
              current = 0; // reset number
              continue;
            }
            current = 10 * current + (b-'0');
          }
        }));
        t.start(); // run thread
        prev = segment; // keep the segment to calculate other's start index
      }
    }

    for (Thread thread : threads) {
       thread.join(); // wait all threads to complete
    }

    int found = 0;
    int maxOccurance = 0;
    // find max occured number out of hotspot, it's O(k) where k = 1000, ignorable
    for (int i = 0; i < NUMBER_MAP.length; i++) {
      final int occurance = NUMBER_MAP[i].get();
      if (occurance > maxOccurance) {
        maxOccurance = occurance;
        found = i;
      }
    }

    // print result
    System.out.println("Found " + found + ", max: " + maxOccurance);
  }

}
