package com.gmalandrakis.utils;

import java.util.Arrays;

public class Utils {

    public static long[] chaffingPositions(byte[] byteArray) {
        long[] positions = new long[32];


        for (int j = 0; j < 32; j++) {
            long a = (long) byteArray[j];
            if (a < 0) {
                if (j == 0) {
                    a = 1;
                } else {
                    a = positions[j - 1];
                }
            }
            positions[j] = a;
        }
        return positions;
    }

    public static byte[] flattenKey(byte[][] arrayOfArrays) {
        var result = new byte[32];
        int i = 0;
        for (byte[] array : arrayOfArrays) {
            for (byte b : array) {
                result[i] = b;
                ++i;
            }
        }
        return result;
    }

    public static byte[] flattenPerm(byte[][] arrayOfArrays) {
        assert (arrayOfArrays.length == 32);

        var result = new byte[1024];
        int i = 0;
        for (byte[] array : arrayOfArrays) {
            for (byte b : array) {
                result[i] = b;
                ++i;
            }
        }
        return result;
    }

    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
