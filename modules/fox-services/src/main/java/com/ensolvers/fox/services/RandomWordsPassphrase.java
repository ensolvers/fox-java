package com.ensolvers.fox.services;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RandomWordsPassphrase {

    private static RandomWordsPassphrase instance;
    private final List<String> dictionary;
    private static Logger logger = LoggerFactory.getLogger(RandomWordsPassphrase.class);

    private RandomWordsPassphrase() throws IOException {
        // Load english dictionary to memory
        StopWatch stopWatch = StopWatch.createStarted();
        File dictionaryFile = new File("src/main/resources/words_alpha.txt");
        try (Stream<String> dictionary = Files.lines(dictionaryFile.toPath())) {
            this.dictionary = dictionary.collect(Collectors.toList());
            stopWatch.stop();
            logger.info("Dictionary was loaded to memory in {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        }
    }

    private static <T> T pickRandomItemFromList(List<T> originalList) {
        Random r = new Random();
        int idx = r.nextInt(originalList.size() - 1);
        T randomItem = null;
        if (!originalList.isEmpty()) {
            randomItem = originalList.get(idx);
        }
        return randomItem;
    }

    private String randomWords(int n)  {
        StringBuilder words = new StringBuilder();
        int i = 0;
        while (i < n) {
            String currentWord = pickRandomItemFromList(dictionary);
            if (currentWord.length() > 3 && currentWord.length() < 8) {
                words.append(currentWord);
                if (i < n - 1) words.append(" ");
                ++i;
            }
        }
        return words.toString();
    }

    public static RandomWordsPassphrase getInstance() throws IOException {
        if (instance == null) {
            instance = new RandomWordsPassphrase();
        }
        return instance;
    }
    public String generate() {
        return randomWords(4);
    }
    public String generate(int n) {
        return randomWords(n);
    }
}
