package com.phil.oracle.interview.textlinestats.accumulator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts the number of words
 *
 * @author Phil
 */
public class WordAccumulator extends AbstractAccumulator<String[]> {
    public static final int MAX_CHAR_CODE = 127;  //limiting evaluated charset to US ASCII - TODO validate assumption!

    // stateful field(s)
    private final AtomicLong wordCount = new AtomicLong(0);

    /**
     * Splits the input item (line) on whitespace and updates wordCount atomically as per the requirements
     *
     * @param inputItem - an input item to accumulate statistics from
     */
    @Override
    public void accumulateItem(String[] inputItem) {
        for (String line : inputItem) {
            int lastCharIndex = line.length() - 1;
            boolean wordObserved = false;

            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if ((int) ch > MAX_CHAR_CODE)
                    continue;  // disregard characters outside our evaluation range
                if (Character.isLetter(ch) && i != lastCharIndex) { // if the char is a letter, there is definitely a word
                    wordObserved = true;
                } else if ((Character.isWhitespace(ch) || i == lastCharIndex) && wordObserved) {
                    // if char is whitespace preceded by a letter, or if it's the last character preceded by a letter
                    wordCount.incrementAndGet();
                    wordObserved = false;
                }
            }
        }
    }

    /**
     * Prints out the stats in a user-friendly way
     *
     * @param className - passed in for convenience
     */
    @Override
    public void summarizeStats(String className) {
        System.out.println(className + ": Total Word Count = " + wordCount);
    }

    /**
     * Convenience method - expected to be called after accumulation is done, but safe regardless
     *
     * @return - long value of wordCount
     */
    public long getTotalWordCount() {
        return wordCount.get();
    }

}
