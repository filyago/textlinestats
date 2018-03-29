package com.phil.oracle.interview.textlinestats;

import com.phil.oracle.interview.textlinestats.framework.BlockingBuffer;
import com.phil.oracle.interview.textlinestats.framework.Consumer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;


public class TextLinesConsumerTest {

    // not much to test here
    @Test
    public void testPoisonPill() {
        Consumer<String[]> consumer = new TextLinesConsumer(BlockingBuffer.instance(1), new ArrayList<>(), 1);
        assertTrue(Arrays.equals(TextLinesConsumer.STOP_SIGNAL, consumer.getStopSignal()));
    }
}