package com.gmalandrakis.permutation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static com.gmalandrakis.permutation.PermutationAlgorithm.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GenericPermutationTest {


    @Test
    public void genericTest() {
        String str = "123456ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        byte[] key = str.getBytes();
        var tmp = key[0];
        key[0] = key[1];
        key[1] = tmp;
        String input = "abcdefghijklmnopqrstuvwxyz000000";
        var result = permuteArrayByKey(input.getBytes(), key);
        var inputBytes = input.getBytes();
        assertEquals(result[0],inputBytes[1]);
        assertEquals(result[1], inputBytes[0]);


        for (int i = 2; i < inputBytes.length; ++i) {
            assertEquals(result[i], inputBytes[i]);
        }

        var original = unpermuteArrayByKey(result, key);
        for (int i = 0; i < inputBytes.length; ++i) {
            assertEquals(original[i], inputBytes[i]);


        }
    }
    @Test
    public void yetAnotherGenericTest() {
        String str = "123456ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        byte[] key = str.getBytes();
        var tmp = key[30];
        key[30] = key[31];
        key[31] = tmp;
        String input = "abcdefghijklmnopqrstuvwxyz000000";
        var result = permuteArrayByKey(input.getBytes(), key);
        var inputBytes = input.getBytes();
        for (int i = 1; i < 30; ++i) {
            assertEquals(result[i], inputBytes[i]);
        }
        assertEquals(result[30], inputBytes[31]);

    }

    @Test
    public void test2Consecutive() {
        String str = "123455ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        byte[] key = str.getBytes();
        String input = "abcdefghijklmnopqrstuvwxyz000000";
        var result = permuteArrayByKey(input.getBytes(), key);
        var inputBytes = input.getBytes();
        assertEquals(result[4], inputBytes[5]);

        for (int i = 1; i < inputBytes.length; ++i) {
            if (i == 4 || i == 5) {
                continue;
            }
            assertEquals(result[i], inputBytes[i]);
        }

        var original = unpermuteArrayByKey(result, key);
        for (int i = 0; i < inputBytes.length; ++i) {
            assertEquals(original[i], inputBytes[i]);
        }
    }

    @Test
    public void genericTestMultiArray() {
        String str = "123456ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        byte[] key = str.getBytes();
        var tmp = key[0];
        key[0] = key[1];
        key[1] = tmp;
        tmp = key[2];
        key[2] = key[31];
        key[31] = tmp;
        byte[][] input = new byte[32][24];
        for (int i = 0; i < input.length; i++) {
            new Random().nextBytes(input[i]);
        }
        var result = permuteArraysByKey(input, key);
        assertEquals(result[0], input[1]);
        assertEquals(result[1], input[0]);


        for (int i = 3; i < input.length - 1; ++i) {
            assertEquals(result[i], input[i]);
        }

        var original = unpermuteArraysByKey(result, key);
        for (int i = 0; i < input.length; ++i) {
            assertEquals(original[i], input[i]);
        }
    }

    @Test
    public void genericTestMultiArrayRandomKey() {
        byte[] key = new byte[32];
        System.out.println(Stream.of(key).sorted());
        byte[][] input = new byte[32][5];
        for (int i = 0; i < 32; i++) {
            new Random().nextBytes(input[i]);
        }
        for (int i = 0; i < key.length; i++) {
            new Random().nextBytes(key);
        }
        var result = permuteArraysByKey(input, key);

        var original = unpermuteArraysByKey(result, key);
        for (int i = 0; i < input.length; ++i) {
            assertEquals(original[i], input[i]);

        }
    }
}
