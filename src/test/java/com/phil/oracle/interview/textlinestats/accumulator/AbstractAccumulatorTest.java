package com.phil.oracle.interview.textlinestats.accumulator;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractAccumulatorTest {
    @Test
    public void testAccumulate() throws InterruptedException {
        // simulate concurrent accumulation with an AtomicInteger and a non thread-safe int primitive
        int threadCount = Runtime.getRuntime().availableProcessors(), itemsPerThread = 10000;
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        AbstractAccumulator<Integer> accumulatorStub = new AbstractAccumulator<Integer>() {
            final AtomicInteger atomicItemCount = new AtomicInteger(0);
            int nonAtomicItemCount = 0;

            @Override
            public void accumulateItem(Integer inputItem) {
                atomicItemCount.incrementAndGet();
                nonAtomicItemCount++;
            }

            @Override
            public void summarizeStats(String className) {
                int expectedItemCount = threadCount * itemsPerThread;
                System.out.println("Expected count = " + expectedItemCount + ", Atomic count = " + atomicItemCount.get()
                        + ", Non-Atomic count = " + nonAtomicItemCount);
                assertEquals(expectedItemCount, atomicItemCount.get());  // Atomics are safe
                assertTrue(expectedItemCount > nonAtomicItemCount);  // non-atomics will not be accurate
            }
        };

        for(int i = 0; i < threadCount; i++) {
            executorService.execute(() -> IntStream.range(0, itemsPerThread).forEach(ii -> accumulatorStub.accumulate(-1)));
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        executorService.shutdownNow();
        accumulatorStub.summarize();
    }

}