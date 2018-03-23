package com.phil.oracle.interview.textlinestats.framework;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A simple wrapper (aka decorator) of a FIFO BlockingQueue containing objects of any given type T
 * Buffer provides a thread-safe communications and throttling medium between concurrent Producer(s) and Consumers
 *
 * @author Phil
 */
public class BlockingBuffer<T> {
    private final BlockingQueue<T> queue;

    // should not be instantiated by clients directly
    private BlockingBuffer(int size) {
        queue = new LinkedBlockingQueue<>(size);  // LinkedBlockingQueue has been proven to perform well in this context
    }

    /**
     * Factory method for an instance of the Buffer
     *
     * @param size - maximum number of items in the buffer (threads attempting to add more will be blocked)
     * @param <T>  - type of items the buffer holds
     * @return - an instance of the buffer
     */
    public static <T> BlockingBuffer<T> instance(int size) {
        if (size <= 0)
            throw new UnsupportedOperationException("Buffer size has to be greater than zero!");
        // just return a new instance for now (any further optimizations here would be client-transparent)
        return new BlockingBuffer<>(size);
    }

    /**
     * Put an item into the buffer. If the buffer is at maximum capacity, the call will block
     *
     * @param item - the item to add (to the tail of the queue)
     */
    public void put(T item) throws InterruptedException {
        queue.put(item);
    }

    /**
     * Take the first item out of the buffer. If the buffer is empty, the call will block
     *
     * @return - the item taken out (from the head of the queue)
     */
    public T take() throws InterruptedException {
        return queue.take();

    }

    /**
     * @return - current buffer size
     */
    public int size() {
        return queue.size();
    }

    /**
     * @return - buffer is empty or not
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
