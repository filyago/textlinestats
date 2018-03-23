package com.phil.oracle.interview.textlinestats;

import com.phil.oracle.interview.textlinestats.framework.AbstractAccumulatingConsumer;
import com.phil.oracle.interview.textlinestats.framework.Accumulator;
import com.phil.oracle.interview.textlinestats.framework.BlockingBuffer;

import java.util.List;

/**
 * Takes arrays of Strings from the Buffer, and feeds each to all Accumulators in the list
 *
 * @author Phil
 */
public class TextLinesConsumer extends AbstractAccumulatingConsumer<String[]> {

    static final String[] STOP_SIGNAL = new String[0];

    public TextLinesConsumer(BlockingBuffer<String[]> buffer, List<Accumulator<String[]>> accumulators, int threadCount) {
        super(buffer, accumulators, threadCount);
    }

    @Override
    protected String[] getPoisonPill() {
        return STOP_SIGNAL;
    }
}
