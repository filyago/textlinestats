package com.phil.oracle.interview.textlinestats.framework;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
  * Abstracts common boilerplate away from concrete AccumulatingConsumer implementations
 *
 * @author Phil
 */
public abstract class AbstractAccumulatingConsumer<T> implements Consumer<T> {

    private final BlockingBuffer<T> buffer;
    private final List<Accumulator<T>> accumulators;
    private final int threadCount;

    protected AbstractAccumulatingConsumer(BlockingBuffer<T> buffer, List<Accumulator<T>> accumulators, int threadCount) {
        this.buffer = buffer;
        this.accumulators = accumulators;
        this.threadCount = threadCount;
    }

    protected AbstractAccumulatingConsumer(BlockingBuffer<T> buffer, List<Accumulator<T>> accumulators) {
        this(buffer, accumulators, 1);
    }

    @Override
    public int getThreadCount() {
        return threadCount;
    }

    @Override
    public BlockingBuffer<T> getBuffer() {
        return buffer;
    }

    /**
     * The stop signal should be an item of type T but unlike any other item of type T the consumer may process
     * Known as "poison pill" in the producer/consumer pattern, it is used to signal a stop to the consumer thread(s).
     * @return - the consumer's poison pill of choice
     */
    @Override
    public T getStopSignal() {
        return getPoisonPill();
    }

    /**
     * For implementations to determine.
     * @return - the consumer's poison pill of choice
     */
    protected abstract T getPoisonPill();

    @Override
    public void run() {
        long start = System.nanoTime();
        String className = this.getClass().getSimpleName();
        String threadName = Thread.currentThread().getName();
        System.out.println(className + ": Started on " + threadName);

        while (true) {
            T item;
            try {
                item = buffer.take();
            } catch (InterruptedException e) {
                System.out.println(className + ": Interrupted on " + threadName +
                        ", total time taken = " + TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - start)) + "ms");
                break;
            }

            if (item.equals(getStopSignal())) {
                // we are done
                System.out.println(className + ": Finished on " + threadName +
                        ", total time taken = " + TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - start)) + "ms");
                break;
            }

            // feed the item to all the Accumulators
            for (Accumulator<T> accumulator : accumulators) {
                accumulator.accumulate(item);
            }
        }
    }
}
