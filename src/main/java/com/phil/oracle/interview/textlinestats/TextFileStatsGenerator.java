package com.phil.oracle.interview.textlinestats;

import com.phil.oracle.interview.textlinestats.accumulator.LetterAccumulator;
import com.phil.oracle.interview.textlinestats.accumulator.LineAccumulator;
import com.phil.oracle.interview.textlinestats.accumulator.WordAccumulator;
import com.phil.oracle.interview.textlinestats.framework.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * The main application - generates statistics from a text file
 *
 * @author Phil
 */
public final class TextFileStatsGenerator {

    // ideally all configuration exist in separate configuration artifacts (Spring, properties, etc.)
    static final int DEFAULT_BUFFER_SIZE = 1000000;
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int DEFAULT_CONSUMER_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_MAX_SECONDS_TO_RUN = 30;

    private List<Accumulator<String[]>> accumulators;
    private Consumer<String[]> consumer;
    private Producer<String[]> producer;

    /**
     * Main entry point
     * @param args - [name of file in classpath or absolute path/name on disk] [optional maximum seconds to run]
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Command-line: java -jar textlinestats.jar [classpath filename or disk file] " +
                    "[optional max runtime duration in seconds (default " + DEFAULT_MAX_SECONDS_TO_RUN + ")]\n" +
                    "Examples: 'java -jar textlinestats.jar war_and_peace.txt' or 'java -jar textlinestats.jar c:/giant.log 300'");
            return;
        }
        String fileName = args[0];

        int maxSecondsToRun = DEFAULT_MAX_SECONDS_TO_RUN;
        if(args.length > 1) {
            try {
                maxSecondsToRun = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("The second parameter is expected to be an integer between 1 and " + Integer.MAX_VALUE);
                System.out.println("Continuing run with maximum runtime duration set to " + maxSecondsToRun + " seconds");
            }
        }

        // we could initialize the rest of the configuration in a similar fashion...let's default for now
        TextFileStatsGenerator app = new TextFileStatsGenerator();
        app.initialize(fileName, DEFAULT_BUFFER_SIZE, DEFAULT_BATCH_SIZE, DEFAULT_CONSUMER_THREAD_COUNT) ;
        app.run(maxSecondsToRun);
    }

    /**
     * Initializes everything we will need
     * Ideally bean creation and wiring would be managed in an IOC container, e.g. Spring
     */
    private void initialize(String textFileName, int bufferSize, int batchSize, int consumerThreadCount) {
        // initialize any new Accumulators participating in the workflow here
        final WordAccumulator wordAccumulator = new WordAccumulator();
        final LineAccumulator lineAccumulator = new LineAccumulator();
        final LetterAccumulator letterAccumulator = new LetterAccumulator();
        // Ensure any new Accumulators are in the list here if they are to participate in the flow
        setAccumulators(Arrays.asList(wordAccumulator, lineAccumulator, letterAccumulator));

        // initialize the consumer
        setConsumer(new TextLinesConsumer(BlockingBuffer.instance(bufferSize), accumulators, consumerThreadCount));

        // initialize the producer
        setProducer(new TextLinesProducer(textFileName, batchSize, consumer));
    }

    /**
     * Run the application
     *
     */
    void run(int maxSecondsToRun) {
        final long start = System.currentTimeMillis();  // to capture wall clock elapsed time for the run
        final long maxMillisToRun = ((long)maxSecondsToRun) * 1000;
        AsyncFlowOrchestrator.runProducerConsumer(maxMillisToRun, producer, consumer);

        // output the summarized statistics for each accumulator
        accumulators.forEach(Accumulator::summarize);

        // I decided not to implement LetterPerWordAccumulator since we already computed everything needed for this stat
        outputAverageLettersPerWord();

        System.out.println("\nWall clock total time elapsed: " + (System.currentTimeMillis() - start) + "ms");
    }

    private void outputAverageLettersPerWord() {
        System.out.println("\nLetterPerWordAccumulator is redundant: we already computed total word and letter counts");
        long wordCount = getWordAccumulator().getTotalWordCount();
        long letterCount = getLetterAccumulator().getTotalLetterCount();
        if (wordCount != 0) {
            String avg = String.valueOf(BigDecimal.valueOf(letterCount).divide(BigDecimal.valueOf(wordCount), 1, RoundingMode.HALF_UP));
            System.out.println("Total letter count " + letterCount + " / total word count " + wordCount + " = "
                    + avg + " average letters per word");
        }
    }

    void setProducer(Producer<String[]> producer) {
        this.producer = producer;
    }

    void setConsumer(Consumer<String[]> consumer) {
        this.consumer = consumer;
    }

    void setAccumulators(List<Accumulator<String[]>> accumulators) {
        this.accumulators = accumulators;
    }

    // TODO clean this up
    private WordAccumulator getWordAccumulator() {
        for(Accumulator a : accumulators) {
            if(a instanceof  WordAccumulator) {
                return (WordAccumulator)a;
            }
        }
        throw new IllegalStateException("Missing WordAccumulator!");
    }

    private LetterAccumulator getLetterAccumulator() {
        for(Accumulator a : accumulators) {
            if(a instanceof LetterAccumulator) {
                return (LetterAccumulator)a;
            }
        }
        throw new IllegalStateException("Missing LetterAccumulator!");
    }
}
