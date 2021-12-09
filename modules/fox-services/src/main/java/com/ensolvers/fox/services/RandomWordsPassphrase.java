package com.ensolvers.fox.services;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomWordsPassphrase {

    private static <T> T pickRandomItemFromList(List<T> originalList) {
        int idx = (int)(Math.random() * originalList.size());
        T randomItem = null;
        if (!originalList.isEmpty()) {
            randomItem = originalList.get(idx);
        }
        return randomItem;
    }

    private static String randomWords(int n)  {
        try {
            // Load english dictionary to memory
            File dictionaryFile = new File("src/main/resources/words_alpha.txt");
            Stream<String> dictionary = Files.lines(dictionaryFile.toPath());
            List<String> wordList = dictionary.collect(Collectors.toList());
            StringBuilder words = new StringBuilder();

            int i = 0;
            while (i < n) {
                String currentWord = pickRandomItemFromList(wordList);
                if (currentWord.length() > 3 && currentWord.length() < 8) {
                    words.append(currentWord);
                    if (i < n - 1) words.append(" ");
                    ++i;
                }
            }

            return words.toString();
        } catch (IOException e) {
            return RandomStringUtils.randomAlphanumeric(18);
        }
    }

    public static String generate() {
        return randomWords(4);
    }

    public static String generate(int n) {
        return randomWords(n);
    }
}
