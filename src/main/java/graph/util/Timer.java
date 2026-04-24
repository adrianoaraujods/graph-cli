package graph.util;

public class Timer {

  public static void run(Runnable block) {
    long start = System.currentTimeMillis();
    block.run();
    System.out.printf(" (✓ %d ms)\n", System.currentTimeMillis() - start);
  }
}
