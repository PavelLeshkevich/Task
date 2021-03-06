package test.filter.api;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a small app that demonstrates how a {@link Filter} can be used.
 *
 * If you want to score some extra points you can implement JUnit tests for your implementation.
 */
public class FilterTest {
    private static final int numberOfSignalsPerProducer = 100;
    private static final int numberOfSignalsProducers = 3;

    private static class MyFilter implements Filter {

        private int deltaN;
        private LinkedBlockingQueue<Long> queue = new LinkedBlockingQueue<>();

        private MyFilter (int N) {
            deltaN = N - numberOfSignalsProducers;
        }

        @Override
        public boolean isSignalAllowed() {
            Long time = System.currentTimeMillis();
            if (queue.size() <= deltaN) {
                queue.offer(time);
                return true;
            }
            else {
                if(time - queue.peek() >= 6e4) {
                    queue.poll();
                }
            }
            return false;
        }
    }

    private static class TestProducer extends Thread {
        private final Filter filter;
        private final AtomicInteger totalPassed;

        private TestProducer(Filter filter, AtomicInteger totalPassed) {
            this.filter = filter;
            this.totalPassed = totalPassed;
        }

        @Override
        public void run() {
            Random rnd = new Random ();
            try {
                for (int j = 0; j < numberOfSignalsPerProducer; j++) {
                    if (filter.isSignalAllowed())
                        totalPassed.incrementAndGet();
                    Thread.sleep(rnd.nextInt(2000));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main (String ... args) throws InterruptedException {
        final int N = 100;
        Filter filter = new MyFilter(N);

        AtomicInteger totalPassed = new AtomicInteger();
        Thread [] producers = new Thread[numberOfSignalsProducers];
        for (int i=0; i < producers.length; i++)
            producers[i] = new TestProducer(filter, totalPassed);

        for (Thread producer : producers)
            producer.start();

        for (Thread producer : producers)
            producer.join();

        System.out.println("Filter allowed " + totalPassed + " signals out of " + (numberOfSignalsPerProducer * numberOfSignalsProducers));
    }

}
