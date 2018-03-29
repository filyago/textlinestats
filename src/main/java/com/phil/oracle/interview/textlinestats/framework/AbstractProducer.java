package com.phil.oracle.interview.textlinestats.framework;

/**
 * Defines an operational framework for the produce/signalCompletion sequence within run()
 * Abstracts common boilerplate away from concrete implementations
 *
 * @author Phil
 */

public abstract class AbstractProducer<T> implements Producer<T> {
    private final int threadCount;          // the producer's thread count

    // these 3 are set from the consumer
    private final BlockingBuffer<T> buffer; // shared buffer with the consumer
    private final int consumerThreadCount;  // number of stop signals (poison pills) to put into buffer at the end
    private final T consumerStopSignal;     // special object for the consumer to stop

    /**
     * Shared values from the consumer are initialized in the constructor
     *
     * @param threadCount - the Producer's thread count
     * @param consumer    - the initialized Consumer
     */
    public AbstractProducer(int threadCount, Consumer<T> consumer) {
        this.threadCount = threadCount;
        this.buffer = consumer.getBuffer();
        this.consumerThreadCount = consumer.getThreadCount();
        this.consumerStopSignal = consumer.getStopSignal();
    }

    /**
     * Producer is single-threaded unless otherwise specified
     *
     * @param consumer
     */
    public AbstractProducer(Consumer<T> consumer) {
        this(1, consumer);  // single-threaded unless otherwise specified
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
    public void signalCompletion() throws InterruptedException {
        // add a poison pill per consumer thread to the buffer
        for (int i = 0; i < consumerThreadCount; i++) {
            buffer.put(consumerStopSignal);
        }
    }
}
