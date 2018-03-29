package com.phil.oracle.interview.textlinestats.framework;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AsyncFlowOrchestratorTest {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();  // for all tests
    private static final String PRODUCER_NAME = "Producer", CONSUMER_NAME = "Consumer";


    @Test
    public void testRunProducerConsumer() {
        int producerWorkMillis = 10, consumerWorkMillis = 100;  //e.g. fast producer & slow consumer
        FakeProducer producer = new FakeProducer(THREAD_COUNT, PRODUCER_NAME, producerWorkMillis);
        FakeConsumer consumer = new FakeConsumer(THREAD_COUNT, CONSUMER_NAME, consumerWorkMillis);

        // given 90ms with a simultaneous shutdown mode, only the producer will finish
        // total allowed runtime is 90ms for producer, greater than 10ms working time
        // total allowed runtime is 90ms for consumer, less than 100ms working time
        AsyncFlowOrchestrator.runProducerConsumer(90, producer, consumer);
        assertEquals(producer.getThreadCount(), producer.completedCount.get());
        assertEquals(producer.getThreadCount() * producerWorkMillis, producer.actualMillisTaken.get());

        assertEquals(0, consumer.completedCount.get()); // consumer did not finish
        // the interrupted consumer should have done at least 80% of the work in this case
        // (it had a total of 90ms for a task of 100ms, and contended with the producer only during the first 10ms)
        assertTrue(consumer.getThreadCount() * consumerWorkMillis * .8 <= consumer.actualMillisTaken.get());

        // it should take both producer and consumer just a little longer than 110ms to finish
        // there is contention during the first 10ms, and unrelated processes could create contention as well
        producer = new FakeProducer(THREAD_COUNT, PRODUCER_NAME, producerWorkMillis);
        consumer = new FakeConsumer(THREAD_COUNT, CONSUMER_NAME, consumerWorkMillis);
        AsyncFlowOrchestrator.runProducerConsumer(150, producer, consumer);
        assertEquals(producer.getThreadCount(), producer.completedCount.get());
        assertEquals(consumer.getThreadCount(), consumer.completedCount.get()); // consumer finished
    }

    @Test
    public void testProducerConsumerInterrupted() throws InterruptedException {
        FakeProducer producer = new FakeProducer(THREAD_COUNT, PRODUCER_NAME, 100);
        FakeConsumer consumer = new FakeConsumer(THREAD_COUNT, CONSUMER_NAME, 100);

        Runnable runnable = () -> AsyncFlowOrchestrator.runProducerConsumer(100, producer, consumer);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(runnable);
        executorService.shutdown();
        executorService.awaitTermination(50, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();

        System.out.println("Producer rm " + producer.actualMillisTaken + ", consumer rm = " + consumer.actualMillisTaken);
    }

    static class FakeRunnable implements Runnable {
        final int threadCount;
        final String name;
        final int millisToRun;

        final AtomicInteger actualMillisTaken;
        final AtomicInteger completedCount;

        FakeRunnable(int threadCount, String name, int millisToRun) {
            this.threadCount = threadCount;
            this.name = name;
            this.millisToRun = millisToRun;

            this.completedCount = new AtomicInteger(0);
            this.actualMillisTaken = new AtomicInteger(0);
        }

        @Override
        public void run() {
            int increment = 5;
            try {
                for (int i = 1; i <= millisToRun; i++) {
                    if (i % increment == 0) {
                        Thread.sleep(increment);
                        actualMillisTaken.addAndGet(increment);
                    } else if (i == millisToRun && i % increment != 0) {
                        int remainder = i % increment;
                        Thread.sleep(remainder);
                        actualMillisTaken.addAndGet(remainder);
                    }
                }
                //System.out.println(name + " finished, actualMillisTaken = " + actualMillisTaken);
                completedCount.incrementAndGet();
            } catch (InterruptedException e) {
                System.out.println(name + " was interrupted, actualMillisTaken = " + actualMillisTaken);
                Thread.currentThread().interrupt();  // restore the interrupted state
            }
        }
    }

    public static class FakeProducer extends FakeRunnable implements Producer {
        FakeProducer(int threadCount, String name, int millisToRun) {
            super(threadCount, name, millisToRun);
        }

        @Override
        public int getThreadCount() {
            return threadCount;
        }

        @Override
        public BlockingBuffer getBuffer() {
            fail("Shouldn't be here");
            throw new UnsupportedOperationException();
        }

        @Override
        public long produceToBuffer(BlockingBuffer buffer) {
            fail("Shouldn't be here");
            throw new UnsupportedOperationException();
        }

        @Override
        public void signalCompletion() {
            fail("Shouldn't be here");
            throw new UnsupportedOperationException();
        }
    }

    public static class FakeConsumer extends FakeRunnable implements Consumer {
        FakeConsumer(int threadCount, String name, int millisToRun) {
            super(threadCount, name, millisToRun);
        }

        @Override
        public int getThreadCount() {
            return threadCount;
        }

        @Override
        public BlockingBuffer getBuffer() {
            fail("Shouldn't be here");
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getStopSignal() {
            fail("Shouldn't be here");
            throw new UnsupportedOperationException();
        }

        @Override
        public long consumeFromBuffer(BlockingBuffer buffer) {
            fail("Shouldn't be here");
            throw new UnsupportedOperationException();
        }
    }

}
