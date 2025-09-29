import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

/**
 * Concurrent Processing, Lock-free via actor-model approach
 * <p>
 * 1. Process concurrently            =~ 67ms
 *    - Interestingly slower than the single thread version
 *    - Spawning platform threads costs a lot (~20ms) and our dataset (1M) is still relatively small.
 *      However, if we assume the dataset is arbitrarily large (like 1 billion) then multi-thread processing should outperform.
 *    - Using virtual threads didn't help, worse in performance ~88ms
 * 2. Compile into native (25-graal)  =~ 5.8ms (same performance with the single thread version)
 */
public class Solution3 {

  static final int THREAD_COUNT = Integer.getInteger("threads",2 * Runtime.getRuntime().availableProcessors());
  static final int[] NUMBER_MAP = new int[1000];

  static class Actor extends Thread {

    // collecting numbers into an array eleminates a lot of operations like auto-boxing, hash calculation, boundary checks, etc.
    // mapped one-to-one with indices is possible since our data set is limited [0 - 999] Therefore, this eleminates hash calculation because no clashes possible
    final int[] segmentMap = new int[1000];

    final ByteBuffer segment;

    Actor(ByteBuffer segment) {
      this.segment = segment;
    }

    @Override
    public void run() {
      byte b;
      int pos = 0; // relative to segment
      int current = 0; // current number
      final int limit = this.segment.limit();
      while (pos++ < limit) {
        if ((b = this.segment.get()) == '\n') { // read & check each byte
          this.segmentMap[current]++; // increment
          current = 0; // reset current
          continue;
        }
        current = 10 * current + (b-'0');
      }
    }

  }

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

    final String inputFile = args[0];
    final FileChannel channel;
    final long fileSize;
    final Actor[] actors = new Actor[THREAD_COUNT];
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
        final var actor = (actors[i] = new Actor(segment));
        actor.start(); // run the actor
        prev = segment; // keep the segment to calculate other's start index
      }
    }

    for (Actor actor : actors) {
      actor.join(); // wait all threads to complete
    }

    int found = 0;
    int maxOccurance = 0;
    // merge partial results into the global and find the max occured number
    for (int i = 0; i < NUMBER_MAP.length; i++) {
      for (int j = 0; j < THREAD_COUNT; j++) {
        final var actor = actors[j];
        final var sum = (NUMBER_MAP[i] += actor.segmentMap[i]);
        if (sum > maxOccurance) {
          maxOccurance = sum;
          found = i;
        }
      }
    }

    // print result
    System.out.println("Found " + found + ", max: " + maxOccurance);
  }

}
