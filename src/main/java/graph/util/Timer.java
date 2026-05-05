package graph.util;

public class Timer {

  public static void run(Runnable block) {
    long start = System.currentTimeMillis();
    block.run();
    System.out.printf(" (✓ %d ms)\n", System.currentTimeMillis() - start);
  }

  public static String formatTime(long ms) {
    if (ms < 1000) {
      return ms + " ms";
    } else if (ms < 60_000) {
      return String.format("%.1f s", ms / 1000.0);
    } else if (ms < 3_600_000) {
      return String.format("%.1f min", ms / 60_000.0);
    } else {
      return String.format("%.1f h", ms / 3_600_000.0);
    }
  }
}
