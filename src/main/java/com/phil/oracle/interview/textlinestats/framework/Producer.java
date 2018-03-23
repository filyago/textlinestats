package com.phil.oracle.interview.textlinestats.framework;

/**
 * Producer is a Runnable that needs to signal completion to Consumer threads when it has finished
 *
 * @param <T> - the type of objects that will be produced to the Buffer
 *              this should match the Buffer as well as the Consumer
 *
 * @author Phil
  */
public interface Producer<T> extends Runnable {

    /**
     * Convenience method for sharing Consumer properties
     * @param consumer
     * @return
     */
    default Producer<T> linkWithConsumer(Consumer<T> consumer) {
        setBuffer(consumer.getBuffer());
        setConsumerThreadCount(consumer.getThreadCount());
        setConsumerPoison(consumer.getStopSignal());
        return this;
    }

    /**
     * Set to the Producer from the Consumer
     * @param consumerThreadCount
     */
    void setConsumerThreadCount(int consumerThreadCount);

    /**
     * Set to the Producer from the Consumer
     * @param buffer
     */
    void setBuffer(BlockingBuffer<T> buffer);

    /**
     * Set to the Producer from the Consumer
     * @param poison
     */
    void setConsumerPoison(T poison);

}
