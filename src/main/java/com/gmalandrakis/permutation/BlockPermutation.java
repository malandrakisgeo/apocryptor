package com.gmalandrakis.permutation;

import java.util.Arrays;
import java.util.Random;

import static com.gmalandrakis.permutation.PermutationAlgorithm.*;
import static com.gmalandrakis.utils.Utils.flattenPerm;

/**
 * @author George Malandrakis (malandrakisgeo@gmail.com)
 */
public class BlockPermutation {
    private static final int CHUNK_SIZE_IN_BYTES = 32;

    public static byte[] permuteBlock(byte[] key, byte[] data) {
        assert (data.length == 1024); //TODO: handle other sizes

        byte[][] blocks = new byte[data.length / CHUNK_SIZE_IN_BYTES][CHUNK_SIZE_IN_BYTES];

        for (int i = 0; i < CHUNK_SIZE_IN_BYTES; i++) {
            blocks[i] = Arrays.copyOfRange(data, i * 32, i * 32 + 32);
        }
        var permutedArray = permuteArraysByKey(blocks, key);


        return flattenPerm(permutedArray);
    }

    public static byte[] unpermuteBlock(byte[] key, byte[] data) {
        assert (data.length == 1024); //TODO: handle other sizes

        byte[][] blocks = new byte[data.length / CHUNK_SIZE_IN_BYTES][CHUNK_SIZE_IN_BYTES];

        for (int i = 0; i < CHUNK_SIZE_IN_BYTES; i++) {
            blocks[i] = Arrays.copyOfRange(data, i * 32, i * 32 + 32);
        }
        var unpermutedArray = unpermuteArraysByKey(blocks, key);


        return flattenPerm(unpermutedArray);
    }


}