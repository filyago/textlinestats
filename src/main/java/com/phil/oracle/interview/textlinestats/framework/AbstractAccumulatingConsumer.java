package com.phil.oracle.interview.textlinestats.framework;

import java.util.List;

/**
 * Abstracts common boilerplate away from concrete AccumulatingConsumer implementations
 *
 * @author Phil
 */
public abstract class AbstractAccumulatingConsumer<T> implements Consumer<T> {

    private final BlockingBuffer<T> buffer;
    private final List<Accumulator<T>> accumulators;
    private final int threadCount;

    public AbstractAccumulatingConsumer(BlockingBuffer<T> buffer, List<Accumulator<T>> accumulators, int threadCount) {
        this.buffer = buffer;
        this.accumulators = accumulators;
        this.threadCount = threadCount;
    }

    public AbstractAccumulatingConsumer(BlockingBuffer<T> buffer, List<Accumulator<T>> accumulators) {
        this(buffer, accumulators, 1);  // single-threaded unless otherwise specified
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
    public long consumeFromBuffer(BlockingBuffer<T> buffer) throws InterruptedException {

        long itemCount = 0;
        while (true) {
            T item = buffer.take();
            if (item.equals(getStopSignal())) {
                break;
            }

            // feed the item to all the Accumulators
            for (Accumulator<T> accumulator : accumulators) {
                accumulator.accumulate(item);
            }
            itemCount++;
        }
        return itemCount;
    }
}
