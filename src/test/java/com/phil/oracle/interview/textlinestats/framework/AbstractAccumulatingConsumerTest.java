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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AbstractAccumulatingConsumerTest {
    @Test
    public void shouldConsumeFromBufferAsExpected() throws URISyntaxException, InterruptedException {
        BlockingBuffer<String[]> buffer = BlockingBuffer.instance(Integer.MAX_VALUE);

        // fill the buffer with contents of a test file, and compute expectations for line and character counts separately
        int expectedLineCount = 0, expectedCharCount = 0;
        Path path = Paths.get(ClassLoader.getSystemResource(TestUtil.SAMPLE_TEXT_FILE_NAME).toURI());
        try (BufferedReader br = Files.newBufferedReader(path)) {
            for (String line; (line = br.readLine()) != null; expectedLineCount++) {
                expectedCharCount += line.length();
                buffer.put(new String[]{line});  // batch size doesn't matter for the test here
            }
        } catch (IOException e) {
            fail("Failed to read file '" + TestUtil.SAMPLE_TEXT_FILE_NAME + "' - please ensure it's in the classpath");
        }
        assertTrue(expectedLineCount > 0 && expectedCharCount > 0);

        // also need a poison pill for the consumer to stop
        final String[] stopSignal = new String[0];
        buffer.put(stopSignal);

        final AtomicInteger consumedLineCount = new AtomicInteger(0);
        final AtomicInteger consumedCharCount = new AtomicInteger(0);
        Accumulator<String[]> accumulatorStub = new Accumulator<String[]>() {
            @Override
            public void accumulate(String[] inputItem) {
                consumedLineCount.addAndGet(inputItem.length);
                for(String line : inputItem) {
                    consumedCharCount.addAndGet(line.length());
                }
            }

            @Override
            public void summarize() {
                // not expected to be called by the Consumer
                fail("Shouldn't be here!");
            }
        };
        List<Accumulator<String[]>> accumulators = new ArrayList<>(Collections.singletonList(accumulatorStub));
        Consumer consumerStub = new AbstractAccumulatingConsumer<String[]>(buffer, accumulators) {
            @Override
            public String[] getStopSignal() {
                return stopSignal;
            }
        };
        assertEquals(buffer, consumerStub.getBuffer());
        assertEquals(1, consumerStub.getThreadCount());

        consumerStub.run();     // run directly on this thread
        assertEquals(expectedLineCount, consumedLineCount.get());
        assertEquals(expectedCharCount, consumedCharCount.get());
    }

    @Test
    public void testInterruptedScenario() throws InterruptedException {
        BlockingBuffer<Integer> buffer = BlockingBuffer.instance(Integer.MAX_VALUE);
        List<Accumulator<Integer>> accumulators = new ArrayList<>();

        AbstractAccumulatingConsumer consumerStub = new AbstractAccumulatingConsumer<Integer>(buffer, accumulators) {
            @Override
            public Integer getStopSignal() {
                return -1;
            }
        };

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(consumerStub);
        executorService.shutdown();
        executorService.awaitTermination(20, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();

        // as it stands now, run() simply exits out
        // TODO determine/assert proper flow in this scenario
    }

}