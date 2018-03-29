package com.phil.oracle.interview.textlinestats.framework;

import java.util.concurrent.ThreadLocalRandom;

public class TestUtil {
    public static final String SAMPLE_TEXT_FILE_NAME = "war_and_peace.txt";
    private static final String DELIMITER = " ";

    public static String generateRandomSentence() {
        // random ASCII string - 3 to 9 words made of 3 to 12 lowercase letters (all max numbers here are exclusive)
        int minWords = 1, maxWords = 10, minLetters = 2, maxLetters = 13, minCharCode = 97, maxCharCode = 123;
        int numWords = ThreadLocalRandom.current().nextInt(minWords, maxWords);
        int numLetters = ThreadLocalRandom.current().nextInt(minLetters, maxLetters);

        StringBuilder inputBuilder = new StringBuilder();
        for (int wordIndex = 0; wordIndex < numWords; wordIndex++) {
            StringBuilder word = new StringBuilder();
            for (int letterIndex = 0; letterIndex < numLetters; letterIndex++) {
                int charCode = ThreadLocalRandom.current().nextInt(minCharCode, maxCharCode);
                word.append((char) charCode);
            }
            inputBuilder.append(word).append(DELIMITER);
        }
        return inputBuilder.substring(0, inputBuilder.length() - DELIMITER.length());
    }





}