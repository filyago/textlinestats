package com.phil.oracle.interview.textlinestats.accumulator;

import com.phil.oracle.interview.textlinestats.framework.Accumulator;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract implementation of the Accumulator interface
 *
 * @param <T> - the type of the input item this accumulator will be processing
 *
 * @author Phil
 */
public abstract class AbstractAccumulator<T> implements Accumulator<T> {
    private final AtomicLong totalRunNanos = new AtomicLong(0); // to track execution time
    private final AtomicLong inputItemCount = new AtomicLong(0);  // to track input items processed

    /**
     * IMPORTANT (hopefully not a design smell...) This method will be called in parallel by multiple consumer threads
     * Implementing subclasses are required to make any stateful operations in this method COMPLETELY THREAD-SAFE
     * Use java.util.concurrent.atomic API for a safe/simple programming model without explicit locks/synchronization
     * See how the AtomicLong class-scope variables are updated by this class for an example
     *
     * @param inputItem - an input item to accumulate statistics from
     */
    protected abstract void accumulateItem(T inputItem);

    /**
     * This method will be called after the input has been fully processed; it does not need to be thread-safe
     * The intent here is for implementing subclasses to compute and output summarized stats (from the accumulated ones)
     *
     * @param className - simple class name passed in from the abstract class for convenience (logging/info purposes)
     */
    protected abstract void summarizeStats(String className);

    /**
     * Wraps the abstract method accumulateItem and keeps track of the total time taken
     *
     * @param inputItem - an input item to accumulate stats for
     */
    @Override
    public final void accumulate(T inputItem) {
        long start = System.nanoTime();
        inputItemCount.incrementAndGet();
        accumulateItem(inputItem);  // to be implemented by subclass
        incrementRunTimeNanos(System.nanoTime() - start);
    }

    /**
     * Prefixes the output of summarizeStats with performance and item counts collected here
     */
    @Override
    public final void summarize() {
        String className = getClass().getSimpleName();
        System.out.println("\n" + className + ": Total Run Time = " + TimeUnit.NANOSECONDS.toMillis(totalRunNanos.longValue()) + "ms");
        System.out.println(className + ": Total Items Processed = " + inputItemCount + "\n");
        summarizeStats(className);  // to be implemented by subclass
    }

    /**
     * For tracking total accumulator runtime. This method is called at the end of each "accumulate" invocation
     *
     * @param accumulateRunTimeNanos - nanoseconds taken for a single accumulation run
     */
    private void incrementRunTimeNanos(long accumulateRunTimeNanos) {
        totalRunNanos.addAndGet(accumulateRunTimeNanos);
    }

}
