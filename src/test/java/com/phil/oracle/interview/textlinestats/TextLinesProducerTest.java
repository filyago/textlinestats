package com.phil.oracle.interview.textlinestats;

import com.phil.oracle.interview.textlinestats.TextLinesProducer;
import com.phil.oracle.interview.textlinestats.framework.BlockingBuffer;
import com.phil.oracle.interview.textlinestats.framework.TextLinesUtil;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class TextLinesProducerTest {

    @Test
    public void shouldProduceToBufferAsExpected() throws URISyntaxException, InterruptedException {
        final int batchSizePerItem = 1000;

        BlockingBuffer<String[]> buffer = BlockingBuffer.instance(Integer.MAX_VALUE);
        TextLinesProducer producer = new TextLinesProducer(TextLinesUtil.SAMPLE_TEXT_FILE_NAME, batchSizePerItem);
        producer.setBuffer(buffer);
        // run the producer directly on this thread to fill up the buffer
        producer.run();

        // total the number of lines and characters within the buffer
        int bufferLineCount = 0, bufferCharCount = 0;
        while (!buffer.isEmpty()) {
            final String[] itemBatch = buffer.take();
            for (String item : itemBatch) {
                bufferLineCount++;
                bufferCharCount += item.length();
            }
        }

        // now read the test file separately to determine the expected number of lines and characters
        int expectedLineCount = 0, expectedCharCount = 0;
        Path path = Paths.get(ClassLoader.getSystemResource(TextLinesUtil.SAMPLE_TEXT_FILE_NAME).toURI());
        try (BufferedReader br = Files.newBufferedReader(path)) {
            for (String line; (line = br.readLine()) != null; expectedLineCount++) {
                expectedCharCount += line.length();
            }
        } catch (IOException e) {
            fail("Failed to read file '" + TextLinesUtil.SAMPLE_TEXT_FILE_NAME + "' - please ensure it's in the classpath");
        }
        assertTrue(expectedLineCount > 0 && expectedCharCount > 0);

        // ensure expected contents match with the buffer contents
        assertEquals(expectedLineCount, bufferLineCount);
        assertEquals(expectedCharCount, bufferCharCount);
    }

    @Test
    public void testEdgeCases() {

        try {
            new TextLinesProducer(TextLinesUtil.SAMPLE_TEXT_FILE_NAME, 0);
            fail("Shouldn't be here!");
        } catch (UnsupportedOperationException e) {
            e.printStackTrace(); //ok
        }
    }
}