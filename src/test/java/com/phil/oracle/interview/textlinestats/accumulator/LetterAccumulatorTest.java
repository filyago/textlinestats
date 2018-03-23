package com.phil.oracle.interview.textlinestats.accumulator;

import com.phil.oracle.interview.textlinestats.framework.TextLinesUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class LetterAccumulatorTest {
    @Test
    public void shouldAccumulateConcurrently() {
        LetterAccumulator accumulator = new LetterAccumulator();

        // build a set of test data and compute expectations separately
        int lineCount = 10000, expectedLetterCount = 0;
        List<String> lines = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++) {
            final String line = TextLinesUtil.generateRandomSentence();
            expectedLetterCount += getLetterCount(line);
            lines.add(line);
        }

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

        accumulator.summarize();  // see what has been accumulated
        assertEquals(expectedLetterCount, accumulator.getTotalLetterCount());  // we should tie out on the count
    }

     private int getLetterCount(String inputItem) {
        int rv = 0;
        for (int i = 0; i < inputItem.length(); i++) {
            char ch = inputItem.charAt(i);
            if (Character.isLetter(ch) && (int) ch <= LetterAccumulator.MAX_CHAR_CODE) {
                rv++;
            }
        }
        return rv;
    }

}
