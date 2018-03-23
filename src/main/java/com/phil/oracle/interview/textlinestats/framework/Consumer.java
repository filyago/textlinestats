package com.phil.oracle.interview.textlinestats.framework;

/**
 * In this design, the Consumer is a Runnable that shares a BlockingBuffer with the Producer
 * It also needs to expose its thread count and a poison pill of choice to the Producer
  *
 * @param <T> - the type of objects that will be consumed from the Buffer
 *
 * @author Phil
 *
 */
public interface Consumer<T> extends Runnable {

    int getThreadCount();   // Allows the producer to initialize shared values from the Consumer
    BlockingBuffer<T> getBuffer(); // Allows the producer to initialize shared values from the Consumer
    T getStopSignal();  // Allows the producer to initialize shared values from the Consumer

}
