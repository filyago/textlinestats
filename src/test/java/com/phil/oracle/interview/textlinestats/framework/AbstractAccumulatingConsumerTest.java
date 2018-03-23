package com.phil.oracle.interview.textlinestats.framework;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class AbstractAccumulatingConsumerTest {
    @Test
    public void shouldConsumeFromBufferAsExpected() throws URISyntaxException, InterruptedException {
        BlockingBuffer<String[]> buffer = BlockingBuffer.instance(Integer.MAX_VALUE);

        // fill the buffer with contents of a test file
        int expectedLineCount = 0, expectedCharCount = 0;
        Path path = Paths.get(ClassLoader.getSystemResource(TextLinesUtil.SAMPLE_TEXT_FILE_NAME).toURI());
        try (BufferedReader br = Files.newBufferedReader(path)) {
            for (String line; (line = br.readLine()) != null; expectedLineCount++) {
                expectedCharCount += line.length();
                buffer.put(new String[]{line});  // batch size shouldn't matter for the test here
            }
        } catch (IOException e) {
            fail("Failed to read file '" + TextLinesUtil.SAMPLE_TEXT_FILE_NAME + "' - please ensure it's in the classpath");
        }
        assertTrue(expectedLineCount > 0 && expectedCharCount > 0);

        // we also need a poison pill for the consumer to stop
        final String[] poison = new String[0];
        buffer.put(poison);

        // a little hack to get around "local variables referenced from an inner class must be final or effectively final"
        final int[] consumedLineCount = new int[1];
        final int[] consumedCharCount = new int[1];
        Accumulator<String[]> accumulatorStub = new Accumulator<String[]>() {
            @Override
            public void accumulate(String[] inputItem) {
                consumedLineCount[0] = consumedLineCount[0] + inputItem.length;
                for(String line : inputItem) {
                    consumedCharCount[0] = consumedCharCount[0] + line.length();
                }
            }

            @Override
            public void summarize() {
                // irrelevant to the consumer
            }
        };
        List<Accumulator<String[]>> accumulators = new ArrayList<>(Collections.singletonList(accumulatorStub));
        AbstractAccumulatingConsumer consumerStub = new AbstractAccumulatingConsumer<String[]>(buffer, accumulators) {
            @Override
            protected String[] getPoisonPill() {
                return poison;
            }
        };
        consumerStub.run();

        assertEquals(expectedLineCount, consumedLineCount[0]);
        assertEquals(expectedCharCount, consumedCharCount[0]);
    }
}