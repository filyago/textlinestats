package com.phil.oracle.interview.textlinestats.framework;

import java.util.concurrent.TimeUnit;

/**
 * The Producer is a Runnable that produces items to a BlockingBuffer(shared data structure with the Consumer)
 *
 * @param <T> - the type of objects that will be produced to the Buffer
 *
 * @author Phil
 */
public interface Producer<T> extends Runnable {

    /**
     * @return - the Producer's thread count
     */
    int getThreadCount();

    /**
     * @return - the Producer's buffer
     */
    BlockingBuffer<T> getBuffer();

    @Override
    default void run() {
        long start = System.nanoTime();
        try {
            System.out.println(getClass().getSimpleName() + ": Started on " + Thread.currentThread().getName());
            long itemCount = produceToBuffer(getBuffer());
            System.out.println(getClass().getSimpleName() + ": Finished on " + Thread.currentThread().getName()
                    + ", total items produced = " + itemCount + ", total time taken = " +
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
        } catch (InterruptedException e) {
            System.out.println(getClass().getSimpleName() + ": Interrupted on " + Thread.currentThread().getName()
                    + ", total time taken = " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " ms");
            Thread.currentThread().interrupt();     // restore the interrupt (passing it on to signalCompletion)
        } finally {
            try {
                signalCompletion();
            } catch (InterruptedException e) {
                System.out.println(getClass().getSimpleName() + ": Interrupted on " + Thread.currentThread().getName() +
                " while signalling completion to Consumer threads!");
                Thread.currentThread().interrupt();  // restore the interrupt
            }
        }
    }

    /**
     * For concrete realizations to implement
     *
     * @return - number of items produced
     * @throws InterruptedException - gets caught and restored in run()
     */
    long produceToBuffer(BlockingBuffer<T> buffer) throws InterruptedException;

    /**
     * For concrete realizations to implement - poison pill per consumer thread
     */
    void signalCompletion() throws InterruptedException;


 }
