package com.gmalandrakis.permutation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static com.gmalandrakis.key_derivation.KeyDerivation.deduceKey;
import static com.gmalandrakis.permutation.BlockPermutation.permuteBlock;
import static com.gmalandrakis.permutation.BlockPermutation.unpermuteBlock;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockPermutationTest {


    @Test
    public void testMe() throws Throwable {
        byte arr[] = new byte[1024];
        new Random().nextBytes(arr);
        var keyDigest = deduceKey("123".getBytes());

        var permuted = permuteBlock(keyDigest, arr);
        byte unperm[] = unpermuteBlock(keyDigest, permuted);

        assertTrue(Arrays.equals(unperm, arr));

    }
}
