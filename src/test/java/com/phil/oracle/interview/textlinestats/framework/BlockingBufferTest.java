package com.phil.oracle.interview.textlinestats.framework;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class BlockingBufferTest {

    @Test
    public void testProducerConsumerUsage() throws InterruptedException {

        final int maxBufferSize = 1000, overMax = maxBufferSize + 1, timeoutMillis = 100;
        final BlockingBuffer<String> buffer = BlockingBuffer.instance(maxBufferSize);

        // let's have a producer try to overfill the buffer on a separate thread
        final Runnable producer = () -> {
            long start = System.nanoTime();
            System.out.println("Producer putting " + overMax + " items into a Buffer of size " + maxBufferSize);
            for (int i = 0; i < overMax; i++) {
                try {
                    buffer.put(TestUtil.generateRandomSentence());
                } catch (InterruptedException e) {
                    fail("Unexpected interrupt!");
                }
            }
            long millisTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            // the time taken cannot possibly be less than our timeout, due to the producer thread being blocked
            assertTrue(timeoutMillis <= millisTaken);
            System.out.println("Producer finished, took about " + millisTaken + "ms");
        };
        final ExecutorService producerExecutor = Executors.newSingleThreadExecutor();
        producerExecutor.execute(producer);
        producerExecutor.shutdown();

        // we expect the producer thread to block on adding the last item (max+1) for the length of our specified timeout
        // let's hold up the current thread's execution for the same duration
        Thread.sleep(timeoutMillis);
        // the buffer should be at full capacity at this point, with the producer thread blocked
        assertEquals(maxBufferSize, buffer.size());

        // now let's take all items off the queue on a separate thread
        final Runnable consumer = () -> {
            long start = System.nanoTime();
            System.out.println("Consumer removing " + overMax + " items from the Buffer");
            for (int i = 0; i < overMax; i++) {
                try {
                    buffer.take();
                } catch (InterruptedException e) {
                    fail("Unexpected interrupt!");
                }
            }

            long millisTaken = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            System.out.println("Consumer finished, took about " + millisTaken + "ms");
        };

        final ExecutorService consumerExecutor = Executors.newSingleThreadExecutor();
        consumerExecutor.execute(consumer);
        consumerExecutor.shutdown();
        consumerExecutor.awaitTermination(50, TimeUnit.MILLISECONDS);
        consumerExecutor.shutdownNow();
        // the producer is definitely done at this point
        producerExecutor.shutdownNow();

        // buffer should be empty at this point
        assertTrue(buffer.isEmpty());
    }

    @Test
    public void testEdgeCases() throws InterruptedException {
        // cover bad constructor args
        try {
            BlockingBuffer.instance(0);
            fail("Shouldn't be here!");
        } catch (UnsupportedOperationException e) {
            e.printStackTrace(); //ok
        }

        // cover interrupted scenarios in put or take
        final BlockingBuffer<String> buffer = BlockingBuffer.instance(1);
        final Runnable producer = () -> {
            try {
                buffer.put("1");
                buffer.put("2");  // stuck
                fail("Shouldn't be here!");
            } catch (InterruptedException e) {
                // expected
            }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(producer);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MILLISECONDS); // interrupt after 10ms
        executorService.shutdownNow();

        final Runnable consumer = () -> {
            try {
                buffer.take();
                buffer.take();  // stuck
                fail("Shouldn't be here!");
            } catch (InterruptedException e) {
                // expected
            }
        };

        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(consumer);
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MILLISECONDS); // interrupt after 10ms
        executorService.shutdownNow();
    }

}