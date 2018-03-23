package com.phil.oracle.interview.textlinestats.accumulator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts the number of lines
 *
 * @author Phil
 */
public class LineAccumulator extends AbstractAccumulator<String[]> {
    // stateful field(s)
    private final AtomicLong lineCount = new AtomicLong(0);

    /**
     * Each item is an array of lines, so increment the count for the array's length
     *
     * @param inputItem - an input item to accumulate statistics from
     */
    @Override
    public void accumulateItem(String inputItem[]) {
        lineCount.addAndGet(inputItem.length);
    }

    /**
     * Prints out the stats in a user-friendly way
     *
     * @param className - passed in for convenience
     */
    @Override
    public void summarizeStats(String className) {
        System.out.println(className + ": Total Line Count = " + lineCount);
    }

    /**
     * Convenience method - expected to be called after accumulation is done, but safe regardless
     *
     * @return - the total line count
     */
    public long getTotalLineCount() {
        return lineCount.get();
    }

}
