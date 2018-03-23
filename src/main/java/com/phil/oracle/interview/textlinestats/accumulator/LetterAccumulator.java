package com.phil.oracle.interview.textlinestats.accumulator;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts the number of letters (with an individual count for each distinct letter)
 *
 * @author Phil
 */
public class LetterAccumulator extends AbstractAccumulator<String[]> {
    public static final int MAX_CHAR_CODE = 127;  //limiting evaluated charset to US ASCII - TODO validate assumption!

    // stateful field(s)
    private final AtomicLong[] letterCounts = new AtomicLong[MAX_CHAR_CODE + 1];

    /**
     * Initializes array of AtomicLongs
     */
    public LetterAccumulator() {
        for (int i = 0; i < letterCounts.length; i++) {
            letterCounts[i] = new AtomicLong(0);
        }
    }

    /**
     * Counts letters in the input item (array of lines) and updates letterCounts array atomically as per the requirements
     *
     * @param inputItem - an input item to accumulate statistics from
     */
    @Override
    public void accumulateItem(String[] inputItem) {
        for(String line : inputItem) {
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if (Character.isLetter(ch) && (int) ch <= MAX_CHAR_CODE) {
                    letterCounts[(int) ch].incrementAndGet();
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
        long maxLetters = 0, maxCharCode = 0;
        for (int i = 0; i < letterCounts.length; i++) {
            if (letterCounts[i].get() > maxLetters) {
                maxLetters = letterCounts[i].get();
                maxCharCode = i;
            }
        }
        System.out.println(className + ": The most common letter is '" + (char) maxCharCode
                + "' with " + maxLetters + " occurrences.");
    }

    /**
     * Convenience method - expected to be called after accumulation is done, but safe regardless
     *
     * @return - total number of letters
     */
    public long getTotalLetterCount() {
        return Arrays.stream(letterCounts).mapToLong(AtomicLong::get).sum();
    }

}
