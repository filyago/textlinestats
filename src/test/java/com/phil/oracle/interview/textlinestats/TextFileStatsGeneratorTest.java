package com.phil.oracle.interview.textlinestats;

import com.phil.oracle.interview.textlinestats.accumulator.LetterAccumulator;
import com.phil.oracle.interview.textlinestats.accumulator.LineAccumulator;
import com.phil.oracle.interview.textlinestats.accumulator.WordAccumulator;
import com.phil.oracle.interview.textlinestats.framework.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;

public class TextFileStatsGeneratorTest {

    @Test
    public void testMainMethod() {  // Functional Test
        // cover bad args
        TextFileStatsGenerator.main(new String[0]);
        TextFileStatsGenerator.main(new String[]{"a", "b"});

        // run the sample file in classpath
        TextFileStatsGenerator.main(new String[]{TestUtil.SAMPLE_TEXT_FILE_NAME});

        // run a sample file on disk
        TextFileStatsGenerator.main(new String[]{"c:/windows/logs/cbs/cbs.log"});
    }

    @Test
    public void testHeavyConsumerWorkloadScenario() {
        /* As the little program stands right now with 3 simple Accumulators, our Consumer is much faster than the
           text file streaming Producer, so the benefits of scaling the Consumer vertically on multicore are not evident

           This test simulates a workflow requiring much heavier processing on the Consumer side, which would happen
           if we added many other Accumulators. Currently the LetterAccumulator is the heaviest out of the existing 3.
        */
        final TextFileStatsGenerator app = new TextFileStatsGenerator();
        List<Accumulator<String[]>> accumulators = new ArrayList<>();
        accumulators.add(new WordAccumulator());
        accumulators.add(new LineAccumulator());
        /* Let's tweak the app in this test by adding an extra 50 LetterAccumulator instances in the Consumer
           This creates lots of extra Consumer overhead, making Consumer a whole lot slower than the Producer
           In the context of performance, it is irrelevant whether the Consumer does meaningful work or not
        */
        IntStream.range(0, 51).forEach(i -> accumulators.add(new LetterAccumulator()));

        app.setAccumulators(accumulators);

        // the buffer should not be a bottleneck in this test
        int itemsToProduce = 50000;  // ensure all items produced will fit into the buffer without blocking
        int bufferSize = itemsToProduce + Runtime.getRuntime().availableProcessors();
        final BlockingBuffer<String[]> buffer = BlockingBuffer.instance(bufferSize);

        // initialize the consumer
        int consumerThreadCount = 1;  // consumer will be single-threaded in the first run
        Consumer<String[]> consumer = new TextLinesConsumer(buffer, accumulators, consumerThreadCount);
        app.setConsumer(consumer);

        // stub out the producer - it needs to be as quick as possible to avoid being a factor
        // it also needs to constrain on the number of items produced to ensure no blocking
        app.setProducer(new DummyTextArraysProducer(itemsToProduce, consumer));

        // run, and time
        int maxSecondsToRun = 5;
        long start = System.nanoTime();
        app.run(maxSecondsToRun);
        long singleThreadedNanos = System.nanoTime() - start;

        assertTrue(buffer.isEmpty());

        // run and time the same exact test with a multithreaded consumer
        consumerThreadCount = Runtime.getRuntime().availableProcessors();  // a reasonable value - no significant gains beyond it
        consumer = new TextLinesConsumer(buffer, accumulators, consumerThreadCount);
        app.setConsumer(consumer);
        app.setProducer(new DummyTextArraysProducer(itemsToProduce, consumer));
        start = System.nanoTime();
        app.run(maxSecondsToRun);
        long multiThreadedNanos = System.nanoTime() - start;

        System.out.println("Load scenario took " + TimeUnit.NANOSECONDS.toMillis(singleThreadedNanos) + "ms single-threaded");
        System.out.println("Load scenario took " + TimeUnit.NANOSECONDS.toMillis(multiThreadedNanos) +
                "ms with " + consumerThreadCount + " threads");

        // My results (Intel i7 6700K quad-core hyperthreaded CPU) - producer takes around 15ms in both cases
        // Load scenario took 596ms single-threaded
        // Load scenario took 161ms with 8 threads
        assertTrue("This test fails only when 'Run With Coverage'", multiThreadedNanos < singleThreadedNanos);
        // TODO figure out why (possible IntelliJ bug?)
    }

    private static class DummyTextArraysProducer extends AbstractProducer<String[]> {

        private final int arraysToProduce;

        DummyTextArraysProducer(int arraysToProduce, Consumer<String[]> consumer) {
            super(1, consumer);
            this.arraysToProduce = arraysToProduce;
        }

        @Override
        public long produceToBuffer(BlockingBuffer<String[]> buffer) {
            int i = 1;
            for (; i <= arraysToProduce; i++) {
                try {
                    buffer.put(new String[]{"abcd efg", "hijk lmnop", "qrs", "tuv", "wx y z"});
                } catch (InterruptedException e) {
                    System.out.println(getClass().getSimpleName() + " was interrupted!");
                    break;
                }
                //buffer.put(new String[]{TextLinesUtil.generateRandomSentence()});
                if (i % 10000 == 0) {
                    System.out.println(getClass().getSimpleName() + " produced " + i + " arrays");
                }
            }
            return i;
        }
    }

    @Test
    public void testInterruptedScenario() {
        final TextFileStatsGenerator app = new TextFileStatsGenerator();
        List<Accumulator<String[]>> accumulators = Arrays.asList(new WordAccumulator(), new LineAccumulator(), new LetterAccumulator());
        app.setAccumulators(accumulators);

        final BlockingBuffer<String[]> buffer = BlockingBuffer.instance(TextFileStatsGenerator.DEFAULT_BUFFER_SIZE);

        int consumerThreadCount = 1;  // doesn't matter for the purposes of this test
        final Consumer<String[]> consumer = new TextLinesConsumer(buffer, accumulators, consumerThreadCount);
        app.setConsumer(consumer);

        final Producer<String[]> producer = new DummyTextArraysProducer(Integer.MAX_VALUE, consumer);
        app.setProducer(producer);

        // run, and watch it get interrupted
        int maxSecondsToRun = 1;
        app.run(maxSecondsToRun);

    }

    @Test  // of extremely limited value :)
    public void testTask20180313() {
        System.out.println("Please send reply with  your program within seven calendar days, " +
                "and supply instructions that tell us how to compile and use it. " +
                "Include the text “Task 20180313” somewhere in your submission.");

    }
}