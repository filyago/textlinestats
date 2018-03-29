package com.phil.oracle.interview.textlinestats.framework;

/**
 * A very simple interface for accumulating and summarizing various statistics over a list of of input items
 *
 * @param <T> - the type of input items this accumulator will be processing
 * @author Phil
 */
public interface Accumulator<T> {
    /**
     * This method will be called for each input item to accumulate stats for, and it is expected to be thread-safe
     * Implementations will be required to accumulate statistics in a thread-safe fashion
     *
     * @param inputItem - an input item to accumulate stats for
     */
    void accumulate(T inputItem);

    /**
     * Implementations should compute and output summarized text statistics (from the accumulated ones) here
     * There are no concurrency considerations here (furthermore, there is no good reason to mutate any state here)
     */
    void summarize();
}
