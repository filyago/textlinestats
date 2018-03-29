package com.phil.oracle.interview.textlinestats.framework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Utility class facilitating the execution of asynchronous workflows
 *
 * @author Phil
 */
public final class AsyncFlowOrchestrator {

    private AsyncFlowOrchestrator() {/* No instantiation or extension for utility class */ }

    /**
     * Runs a Producer and Consumer asynchronously on multiple threads, and waits up to the specified timeout for completion
     *
     * @param maxMillisToRun - maximum milliseconds to wait for completion before interrupting
     * @param producer - the (Runnable) Producer
     * @param consumer - the (Runnable) Consumer
     * @return - total milliseconds taken for the run
     */
    public static long runProducerConsumer(long maxMillisToRun, Producer producer, Consumer consumer) {
        ExecutorService consumerExecutor = runAsync(consumer, consumer.getThreadCount()); // run the consumer (non-blocking)
        ExecutorService producerExecutor = runAsync(producer, producer.getThreadCount()); // run the producer (non-blocking)
        return awaitCompletion(maxMillisToRun, producerExecutor, consumerExecutor);       // wait for completion as configured
    }

    /**
     * Executes a Runnable asynchronously on a newFixedThreadPool with the specified threadCount
     *
     * @param runnable    - the Runnable to execute
     * @param threadCount - how many threads to execute the Runnable on
     * @return - ExecutorService for this execution
     */
    public static ExecutorService runAsync(Runnable runnable, int threadCount) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        IntStream.range(0, threadCount).forEach(i -> executorService.execute(runnable));
        return executorService;
    }

    /**
     * Awaits for completion of a producer/consumer workflow, starting with the producer
     *
     * @param maxMillisToRun   - the maximum duration to run for both, in milliseconds
     * @param producerExecutor - producer thread pool
     * @param consumerExecutor - consumer thread pool
     * @return - total milliseconds taken for the run
     */
    private static long awaitCompletion(long maxMillisToRun, ExecutorService producerExecutor, ExecutorService consumerExecutor) {
        long producerMillisWaited = awaitCompletion(maxMillisToRun, producerExecutor);  // block on producer first

        long remainingMillis = Math.max(0, maxMillisToRun - producerMillisWaited);
        long consumerMillisWaited = awaitCompletion(remainingMillis, consumerExecutor); // block on consumer for remainder
        return producerMillisWaited + consumerMillisWaited;
    }

    /**
     * Blocking call to await execution of a given service
     *
     * @param maxMillisToRun  - milliseconds to run before interrupting
     * @param executorService - the running executor service
     * @return
     */
    public static long awaitCompletion(long maxMillisToRun, ExecutorService executorService) {
        long start = System.nanoTime();
        executorService.shutdown();
        try {
            executorService.awaitTermination(maxMillisToRun, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println(AsyncFlowOrchestrator.class.getSimpleName() + " was interrupted!");
            Thread.currentThread().interrupt();  // restore the interrupt
        } finally {
            executorService.shutdownNow();
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
