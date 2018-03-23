package com.phil.oracle.interview.textlinestats.accumulator;

import com.phil.oracle.interview.textlinestats.framework.TextLinesUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class WordAccumulatorTest {
    @Test
    public void shouldAccumulateConcurrently() {
        WordAccumulator accumulator = new WordAccumulator();

        // build a set of test data and compute expectations separately
        int lineCount = 10000, expectedWordCount = 0;
        List<String> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            final String line = TextLinesUtil.generateRandomSentence();
            expectedWordCount += getWordCount(line);
            lines.add(line);
        }
        // add a non-ascii line
        String nonAscii = "я устал, мне надоело";
        lines.add(nonAscii);
        expectedWordCount += getWordCount(nonAscii);

        // simulate concurrent accumulation in a similar fashion to the app
        final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        lines.forEach(line -> executorService.execute(() -> {
            System.out.println("Accumulating item '" + line + "' on " + Thread.currentThread().getName());
            accumulator.accumulate(new String[]{line});
        }));
        executorService.shutdown();
        try {
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        accumulator.summarize();   // see what has been accumulated
        assertEquals(expectedWordCount, accumulator.getTotalWordCount());  // we should tie out on the count
    }

    @Test
    public void testVariousLines() {
        WordAccumulator accumulator = new WordAccumulator();
        String line1 = "abc.";
        accumulator.accumulateItem(new String[]{line1});
        assertEquals(1, accumulator.getTotalWordCount());

        String line2 = ".abc. dfg.";
        accumulator.accumulateItem(new String[]{line2});
        assertEquals(3, accumulator.getTotalWordCount());
    }

    private int getWordCount(String str) {
        // the whitespace split here is very different vs the actual implementation, but should accomplish the same thing
        // this is simpler, and would likely perform a lot worse (TODO ascertain)
        int wordCount = 0;
        // remove any non-ASCII characters and split on a whitespace regular expression
        String[] words = str.replaceAll("[^\\x20-\\x7e]", "").trim().split(getWhiteSpaceRegex());
        for (String word : words) {
            for (int i = 0; i < word.length(); i++) {
                if (Character.isLetter(word.charAt(i))) {
                    wordCount++;
                    break;
                }
            }
        }
        return wordCount;
    }

    private String getWhiteSpaceRegex() {
        String wsChars = ""
                + "\\u0009" // CHARACTER TABULATION
                + "\\u000A" // LINE FEED (LF)
                + "\\u000B" // LINE TABULATION
                + "\\u000C" // FORM FEED (FF)
                + "\\u000D" // CARRIAGE RETURN (CR)
                + "\\u0020" // SPACE
                + "\\u0085" // NEXT LINE (NEL)
                + "\\u00A0" // NO-BREAK SPACE
                + "\\u1680" // OGHAM SPACE MARK
                + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
                + "\\u2000" // EN QUAD
                + "\\u2001" // EM QUAD
                + "\\u2002" // EN SPACE
                + "\\u2003" // EM SPACE
                + "\\u2004" // THREE-PER-EM SPACE
                + "\\u2005" // FOUR-PER-EM SPACE
                + "\\u2006" // SIX-PER-EM SPACE
                + "\\u2007" // FIGURE SPACE
                + "\\u2008" // PUNCTUATION SPACE
                + "\\u2009" // THIN SPACE
                + "\\u200A" // HAIR SPACE
                + "\\u2028" // LINE SEPARATOR
                + "\\u2029" // PARAGRAPH SEPARATOR
                + "\\u202F" // NARROW NO-BREAK SPACE
                + "\\u205F" // MEDIUM MATHEMATICAL SPACE
                + "\\u3000" // IDEOGRAPHIC SPACE
                ;
        return "[" + wsChars + "]";
    }

    //@Test // exploratory
    public void exploreCharDetails() {
        for (int i = 0; i < WordAccumulator.MAX_CHAR_CODE; i++) {
            System.out.println((char) i + "(" + i + "): isLetter = " + Character.isLetter((char) i)
                    + ", isWhitespace = " + Character.isWhitespace((char) i) + ", name = " + Character.getName(i));
        }
    }
}
