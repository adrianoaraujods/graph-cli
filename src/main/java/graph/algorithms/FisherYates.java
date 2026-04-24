package graph.algorithms;

import java.util.Random;

public class FisherYates {

  public static int[] shuffle(int[] array, Random random) {
    if (array == null) {
      return null;
    }

    if (random == null) {
      random = new Random();
    }

    for (int i = array.length - 1; i >= 0; i--) {
      int j = random.nextInt(i + 1);

      int temp = array[i];
      array[i] = array[j];
      array[j] = temp;
    }

    return array;
  }
}
