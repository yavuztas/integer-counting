import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

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

  static final int THREAD_COUNT = Integer.getInteger("threads", Runtime.getRuntime().availableProcessors());
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
      while (pos++ < this.segment.limit()) {
        if ((b = this.segment.get()) == '\n') { // read & check each byte
          this.segmentMap[current]++; // increment
          current = 0; // reset current
          continue;
        }
        current = 10 * current + (b-'0');
      }
    }

  }

  static int findSegmentStart(ByteBuffer segment, int pos) {
    if (pos == 0)
      return 0;
    // read the segment to backwards until find the first '\n'
    while (segment.get(pos) != '\n') {
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

  public static void main(String[] args) throws Exception {

    final String inputFile = args[0];
    final MappedByteBuffer buffer;
    try (final var file = new RandomAccessFile(inputFile, "r")) {
      final FileChannel channel = file.getChannel();
      buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()); // we don't care about channel size here since can be max ~= 4M < Integer.MAX_VALUE
    }

    final Actor[] actors = new Actor[THREAD_COUNT];
    final int segmentSize = buffer.capacity() / THREAD_COUNT;

    int start = 0;
    for (int i = 0; i < THREAD_COUNT; i++) {
      start = findSegmentStart(buffer, start);
      final int size = findSegmentSize(buffer, start, segmentSize);
      final MappedByteBuffer segment = buffer.slice(start, size);
      final var actor = (actors[i] = new Actor(segment));
      start += size; // update the next start pos
      actor.start(); // run the actor
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
