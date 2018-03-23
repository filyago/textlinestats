package com.phil.oracle.interview.textlinestats.framework;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;

public class AbstractProducerTest {

    @Test
    public void shouldProduceToBufferAsExpected() throws URISyntaxException, InterruptedException {

        BlockingBuffer<String[]> buffer = BlockingBuffer.instance(Integer.MAX_VALUE);

        AbstractProducer<String[]> producer = new AbstractProducer<String[]>() {
            @Override
            protected int produceToBuffer(BlockingBuffer<String[]> buffer) throws InterruptedException {
                Path path = null;
                try {
                    path = Paths.get(ClassLoader.getSystemResource(TextLinesUtil.SAMPLE_TEXT_FILE_NAME).toURI());
                } catch(URISyntaxException e) {
                    fail("Shouldn't be here!");
                }
                int itemCount = 0;
                try (BufferedReader br = Files.newBufferedReader(path)) {
                    for (String line; (line = br.readLine()) != null; itemCount++) {
                        buffer.put(new String[]{line});
                    }
                } catch (IOException e) {
                    fail("Failed to read file '" + TextLinesUtil.SAMPLE_TEXT_FILE_NAME + "' - please ensure it's in the classpath");
                }
                return itemCount;
            }
        };
        producer.setBuffer(buffer);
        final String[] consumerPoison = new String[0];
        producer.setConsumerPoison(consumerPoison);
        producer.setConsumerThreadCount(1);
        // run the producer directly on this thread to fill up the buffer
        producer.run();

        // total the number of lines and characters within the buffer
        int bufferLineCount = 0, bufferCharCount = 0;
        while (!buffer.isEmpty()) {
            final String[] itemBatch = buffer.take();
            if(Arrays.equals(itemBatch, consumerPoison))
                break;

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

        // ensure expected contents match with the buffer contents
        assertTrue(expectedLineCount > 0 && expectedCharCount > 0);
        assertEquals(expectedLineCount, bufferLineCount);
        assertEquals(expectedCharCount, bufferCharCount);
    }

    //TODO testInterruptedScenario (needs more clarity on requirements)

}