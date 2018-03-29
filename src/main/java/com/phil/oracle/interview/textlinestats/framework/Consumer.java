package com.phil.oracle.interview.textlinestats.framework;

import java.util.concurrent.TimeUnit;

/**
 * The Consumer is a Runnable that shares a BlockingBuffer with the Producer and consumes from the Bufferr
 * It also needs to expose its thread count and a poison pill of choice to the Producer
  *
 * @param <T> - the type of objects that will be consumed from the Buffer
 *
 * @author Phil
 *
 */
public interface Consumer<T> extends Runnable {

    /**
     * @return - the Consumer's thread count
     */
    int getThreadCount();

    /**
     * @return - the Consumer's buffer
     */
    BlockingBuffer<T> getBuffer();

    /**
     * @return - the Consumer's stop signal (aka poison pill)
     */
    T getStopSignal();

    @Override
    default void run() {
        long start = System.nanoTime();
        System.out.println(getClass().getSimpleName() + ": Started on " + Thread.currentThread().getName());
        try {
            long itemsConsumed = consumeFromBuffer(getBuffer());
            System.out.println(getClass().getSimpleName() + ": Finished on " + Thread.currentThread().getName() +
                    ", total items consumed = " + itemsConsumed +
                    ", total time taken = " + TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - start)) + "ms");
        } catch(InterruptedException e) {
            System.out.println(getClass().getSimpleName() + ": Interrupted on " + Thread.currentThread().getName() +
                    ", total time taken = " + TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - start)) + "ms");
            Thread.currentThread().interrupt();     // restore the interrupt
        }
    }

    /**
     * For concrete realizations to implement
     *
     * @return - number of items consumed
     * @throws InterruptedException
     */
    long consumeFromBuffer(BlockingBuffer<T> buffer) throws InterruptedException;
}
