package com.phil.oracle.interview.textlinestats;

import com.phil.oracle.interview.textlinestats.framework.AbstractProducer;
import com.phil.oracle.interview.textlinestats.framework.BlockingBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Reads lines of text from a file and puts them into a buffer. String arrays are used for batching.
 *
 * @author Phil
 */
public class TextLinesProducer extends AbstractProducer<String[]> {

    private final String textFileName;       // the file name to read
    private final int itemsBatchSize;        // how many text lines to batch up into each array put into the buffer

    public TextLinesProducer(String textFileName, int itemsBatchSize) {
        if(itemsBatchSize <= 0) {
            throw new UnsupportedOperationException("Items batch size has to be greater than zero!");
        }
        this.textFileName = textFileName;
        this.itemsBatchSize = itemsBatchSize;
    }

    /**
     * Streams lines from a file on disk (or in classpath) to the buffer
     *
     * @return number of items streamed to the buffer (if batchSize is 1, it will be the # of lines)
     */
    @Override
    public int produceToBuffer(BlockingBuffer<String[]> buffer) throws InterruptedException {

        int lineCount = 0;
        InputStream inputStream = getInputFileStream();

        if (inputStream != null) {
            String[] batchItem = new String[itemsBatchSize];
            int itemIndex = 0;
            // performance might be better with BufferedReader...not the focus area for this exercise
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineCount++;
                if (itemIndex < itemsBatchSize) {
                    batchItem[itemIndex] = line;
                    itemIndex++;
                } else {
                    buffer.put(batchItem);
                    batchItem = new String[itemsBatchSize];
                    batchItem[0] = line;
                    itemIndex = 1;
                }
            }
            // put the remainder
            String[] last = new String[itemIndex];
            System.arraycopy(batchItem, 0, last, 0, itemIndex);
            buffer.put(last);
        }
        return lineCount;
    }

    /**
     * @return an InputStream to the input file, covering disk as well as classpath
     */
    private InputStream getInputFileStream() {
        InputStream rv;

        try {
            File initialFile = new File(textFileName); // look on disk
            rv = new FileInputStream(initialFile);
            System.out.println("File '" + textFileName + "' found on disk");
        } catch (FileNotFoundException e) {
            // look in classpath
            rv = getClass().getClassLoader().getResourceAsStream(textFileName);
            if (rv != null) {
                System.out.println("File '" + textFileName + "' found in classpath");
            } else {
                System.out.println("ERROR - Couldn't find the file '" + textFileName + "' anywhere!");
            }
        }
        return rv;
    }

}
