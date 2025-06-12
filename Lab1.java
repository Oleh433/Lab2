import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class MinSearch {

    private static int[] array;
    private static int globalMin = Integer.MAX_VALUE;
    private static int globalMinIndex = -1;
    private static final Object lock = new Object();
    private static CountDownLatch latch;

    public static void main(String[] args) {
        int arraySize = 10_000_000;
        int threadCount = Runtime.getRuntime().availableProcessors();

        array = generateArray(arraySize);
        latch = new CountDownLatch(threadCount);

        int chunkSize = arraySize / threadCount;

        for (int i = 0; i < threadCount; i++) {
            int start = i * chunkSize;
            int end = (i == threadCount - 1) ? arraySize : start + chunkSize;

            Thread thread = new Thread(new MinFinder(start, end));
            thread.start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nГлобальний мінімум: " + globalMin);
        System.out.println("Індекс мінімального елемента: " + globalMinIndex);
    }

    private static int[] generateArray(int size) {
        Random rnd = new Random();
        int[] arr = new int[size];

        for (int i = 0; i < size; i++) {
            arr[i] = rnd.nextInt(1_000_000) + 1;
        }

        int negativeIndex = rnd.nextInt(size);
        arr[negativeIndex] = -rnd.nextInt(1000) - 1;

        System.out.println("Від’ємний елемент вставлено на позицію " + negativeIndex + ": " + arr[negativeIndex]);

        return arr;
    }

    private static class MinFinder implements Runnable {
        private final int start;
        private final int end;

        public MinFinder(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            int localMin = Integer.MAX_VALUE;
            int localMinIndex = -1;

            for (int i = start; i < end; i++) {
                if (array[i] < localMin) {
                    localMin = array[i];
                    localMinIndex = i;
                }
            }

            synchronized (lock) {
                if (localMin < globalMin) {
                    globalMin = localMin;
                    globalMinIndex = localMinIndex;
                }
            }

            latch.countDown();
        }
    }
}
