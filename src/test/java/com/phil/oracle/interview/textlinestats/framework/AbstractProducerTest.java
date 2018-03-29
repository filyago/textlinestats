package com.phil.oracle.interview.textlinestats.framework;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AbstractProducerTest {

    @Test
    public void shouldProduceToBufferAsExpected() throws InterruptedException {
        BlockingBuffer<Integer> buffer = BlockingBuffer.instance(1000);
        final int consumerThreadCount = 2, consumerStopSignal = -1;
        Consumer<Integer> consumerStub = new ConsumerStub<>(consumerThreadCount, buffer, consumerStopSignal);

        final int itemsToProduce = 101;
        Producer<Integer> producerStub = new AbstractProducer<Integer>(consumerStub) {
            @Override
            public long produceToBuffer(BlockingBuffer<Integer> buffer) throws InterruptedException {
                int item;
                for (item = 1; item <= itemsToProduce; item++) {
                    buffer.put(item); // a sequence of integers 1 to the number of items
                }
                return item;
            }
        };
        assertEquals(1, producerStub.getThreadCount());
        assertEquals(buffer, producerStub.getBuffer());
        // run the producer directly on this thread to fill up the buffer
        producerStub.run();

        // it should add all the items, and a poison pill per consumer thread
        assertEquals(itemsToProduce + consumerThreadCount, buffer.size());

        // remove all the actual items from the buffer, and tie out with what we expect
        long bufferSum = 0;
        for (int i = 0; i < itemsToProduce; i++) {
            bufferSum += buffer.take();
        }
        long expectedSum = itemsToProduce * (itemsToProduce + 1) / 2; // Gauss formula
        assertEquals(expectedSum, bufferSum);

        // the remaining items should be the poison pills
        for (int i = 0; i < consumerThreadCount; i++) {
            assertEquals(consumerStub.getStopSignal(), buffer.take());
        }
        assertTrue(buffer.isEmpty());
    }

    @Test
    public void testInterruptedScenario() throws InterruptedException {
        BlockingBuffer<Integer> buffer = BlockingBuffer.instance(Integer.MAX_VALUE);
        final int consumerThreadCount = 2, consumerStopSignal = -1;
        Consumer<Integer> consumer = new ConsumerStub<>(consumerThreadCount, buffer, consumerStopSignal);

        final int itemsToProduce = 101, sleepMillis = 50;
        Producer<Integer> producer = new AbstractProducer<Integer>(consumer) {
            @Override
            public long produceToBuffer(BlockingBuffer<Integer> buffer) throws InterruptedException {
                int item;
                for (item = 1; item <= itemsToProduce; item++) {
                    buffer.put(item); // a sequence of integers 1 to the number of items
                }
                Thread.sleep(sleepMillis);
                return item;
            }
        };
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(producer);
        executorService.shutdown();
        executorService.awaitTermination(20, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();

        // as it stands now, the poison pills won't get added due to the restored interrupt
        assertEquals(itemsToProduce, buffer.size());
        // TODO determine/assert proper flow in this scenario
    }

    private static class ConsumerStub<T> implements Consumer<T> {
        private final int threadCount;
        private final BlockingBuffer<T> buffer;
        private final T stopSignal;

        ConsumerStub(int threadCount, BlockingBuffer<T> buffer, T stopSignal) {
            this.threadCount = threadCount;
            this.buffer = buffer;
            this.stopSignal = stopSignal;
        }

        @Override
        public int getThreadCount() {
            return threadCount;
        }

        @Override
        public BlockingBuffer<T> getBuffer() {
            return buffer;
        }

        @Override
        public T getStopSignal() {
            return stopSignal;
        }

        @Override
        public long consumeFromBuffer(BlockingBuffer<T> buffer) {
            fail("Shouldn't be here!");
            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {
            fail("Shouldn't be here!");
            throw new UnsupportedOperationException();
        }
    }

}