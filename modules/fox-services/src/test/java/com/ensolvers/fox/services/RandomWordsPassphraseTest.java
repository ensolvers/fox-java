package com.ensolvers.fox.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomWordsPassphraseTest {

    @Test
    void generationTest() {
        String passphrase = RandomWordsPassphrase.generate();
        String[] words = passphrase.split(" ");
        Assertions.assertEquals(4, words.length);
        for (String word: words) {
            Assertions.assertTrue(word.length() < 8 && word.length() > 3);
        }
    }

}
