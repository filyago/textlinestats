package com.phil.oracle.interview.textlinestats.framework;

import com.phil.oracle.interview.textlinestats.accumulator.AbstractAccumulator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractAccumulatorTest {
    @Test
    public void testAccumulate() {
        AbstractAccumulator<String> accumulatorStub = new AbstractAccumulator<String>() {
            int itemCount = 0;

            @Override
            public void accumulateItem(String inputItem) {
                System.out.println("Accumulating " + inputItem);
                itemCount++;
            }

            @Override
            public void summarizeStats(String className) {
                assertEquals(itemCount, 2);
            }
        };

        accumulatorStub.accumulate("a");
        accumulatorStub.accumulate("b");
        accumulatorStub.summarize();
    }

}