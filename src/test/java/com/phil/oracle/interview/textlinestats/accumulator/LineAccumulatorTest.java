package com.phil.oracle.interview.textlinestats.accumulator;

import com.phil.oracle.interview.textlinestats.framework.TextLinesUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class LineAccumulatorTest {
    @Test
    public void shouldAccumulateConcurrently() {
        LineAccumulator accumulator = new LineAccumulator();

        List<String> lines = new ArrayList<>();
        int expectedLines = 10000;
        for (int i = 0; i < expectedLines; i++) {
            lines.add(TextLinesUtil.generateRandomSentence());
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
        assertEquals(expectedLines, accumulator.getTotalLineCount());  // we should tie out on the count
    }
}
