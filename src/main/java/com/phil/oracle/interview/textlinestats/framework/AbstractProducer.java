package com.phil.oracle.interview.textlinestats.framework;

import java.util.concurrent.TimeUnit;

/**
 * Defines an operational framework for the produce/signalCompletion sequence within run()
 * Abstracts common boilerplate away from concrete implementations
 *
 * @author Phil
  */

public abstract class AbstractProducer<T> implements Producer<T> {
    private BlockingBuffer<T> buffer; // shared buffer
    private int consumerThreadCount;  // number of poison pills to put into buffer at the end
    private T consumerPoison;         // special object for the consumer to stop

    @Override
    public void setConsumerThreadCount(int consumerThreadCount) {
        this.consumerThreadCount = consumerThreadCount;
    }

    @Override
    public void setBuffer(BlockingBuffer<T> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void setConsumerPoison(T poison) {
        this.consumerPoison = poison;
    }

    /**
     * Runs the producer (typically on a separate thread)
     */
    @Override
    public void run() {
        System.out.println(getClass().getSimpleName() + " started on " + Thread.currentThread().getName());
        long start = System.nanoTime();
        try {
            int itemCount = produceToBuffer(buffer);
            System.out.println(getClass().getSimpleName() + " finished on " + Thread.currentThread().getName()
                    + ", items produced = " + itemCount + ", took " +
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
        } catch (InterruptedException e) {
            System.out.println(getClass().getSimpleName() + " interrupted on " + Thread.currentThread().getName()
                    + ", took " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
        } finally {
            signalCompletion();
        }
    }

    /**
     * Subclasses must implement this method
     * @return - number of items produced
     * @throws InterruptedException
     */
    protected abstract int produceToBuffer(BlockingBuffer<T> buffer) throws InterruptedException;

    /**
     * Must be called at the end of run()
     */
    private void signalCompletion() {
        // add a poison pill per consumer thread to the buffer
        for (int i = 0; i < consumerThreadCount; i++) {
            try {
                buffer.put(consumerPoison);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println(getClass().getSimpleName() + " interrupted on " + Thread.currentThread().getName() +
                        " while signalling completion to Consumer threads!");
                // the consumer will never stop now
                throw new IllegalStateException("Cannot continue in this state!");
            }
        }
    }

}
