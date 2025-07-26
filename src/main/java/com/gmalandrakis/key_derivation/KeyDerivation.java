package com.gmalandrakis.key_derivation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.gmalandrakis.utils.Utils.concatAll;
import static com.gmalandrakis.utils.Utils.flattenKey;
import static java.lang.Math.*;

/**
 * The original implementation of the katakerm hash function in Java.
 *
 * @author George Malandrakis (malandrakisgeo@gmail.com)
 */
public class KeyDerivation {
    public static byte[] deduceKey(byte[] oldByteSequence) {
        return flattenKey(deduceKeyInternal(oldByteSequence));
    }

    /**
     * Takes an arbitrary byte array and returns 32-byte (256bit) key as byte[4][8].
     */
    static byte[][] deduceKeyInternal(byte[] oldByteSequence) {
        var originallength = oldByteSequence.length;
        var byteSequence = new byte[32];


        if (originallength <= 32) {
            for (int i = 0; i < 32; ++i) {
                if (i < originallength) {
                    byteSequence[i] = oldByteSequence[i];
                } else {
                    byteSequence[i] = 0; //NOT '0'!!!
                }
            }
        }

        if (originallength > 32) {
            int parts_of_32_bytes = originallength / 32;
            byteSequence = deduceKey(Arrays.copyOfRange(oldByteSequence, 0, 32));
            handleLargeInput(parts_of_32_bytes, byteSequence, oldByteSequence);
            for (int j = 0; j < originallength % 32; j++) {
                byteSequence[j] ^= oldByteSequence[32 * parts_of_32_bytes + j];
                byteSequence[j] ^= (byte) (j & 0xFF);
            }
        }

        byte xored = xoredLength(originallength);
        byte[][] arrayOfArrays = new byte[4][8];
        successiveXor(arrayOfArrays, byteSequence, xored);

        byte[] trigonometric_1 = doubleToBytes(round(cos(toSignedLong(arrayOfArrays[0])), 12) + round(sin(toSignedLong(arrayOfArrays[1]) + toSignedLong(arrayOfArrays[2])), 12));
        byte[] trigonometric_2 = doubleToBytes(round(cos(toSignedLong(arrayOfArrays[1]) + toSignedLong(arrayOfArrays[2])), 12) + round(sin(toSignedLong(arrayOfArrays[3])), 12));
        byte[] tox = multiplyElementsByPower(arrayOfArrays);
        for (int j = 0; j < 8; ++j) {
            tox[j] = (byte) (tox[j] ^ trigonometric_1[j] ^ trigonometric_2[j]);
        }

        for (int i = 0; i < 4; ++i) {
            byte position_leftmostbyte_cubed = (byte) ((i * i * i) & 0xFF);
            for (int j = 0; j < 8; ++j) {
                arrayOfArrays[i][j] = (byte) (arrayOfArrays[i][j] ^ tox[j] ^ position_leftmostbyte_cubed);
            }
        }

        return arrayOfArrays;
    }


    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        if (places == 0) {
            return value;
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static long toSignedLong(byte[] bytes) {
        assert (bytes.length <= 8);
        //We originally skipped the LITTLE ENDIAN
        try {
            // return ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).put(bytes).getLong(0);
            return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(x);
        return buffer.array();
    }


    private static byte[] doubleToBytes(double x) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putDouble(x);
        return buffer.array();
    }

    private static void handleLargeInput(int parts_of_32_bytes, byte[] byteSequence, byte oldByteSequence[]) {
        for (int i = 1; i < parts_of_32_bytes; i++) {
            var tb = Arrays.copyOfRange(byteSequence, 0, 8);
            var a = toSignedLong(tb) + 1 * i;
            var b = toSignedLong(Arrays.copyOfRange(byteSequence, 8, 16)) + 4 * i;
            var c = toSignedLong(Arrays.copyOfRange(byteSequence, 16, 24)) + 9 * i;
            var d = toSignedLong(Arrays.copyOfRange(byteSequence, 24, 32)) + 16 * i;
            int l = 0;
            for (int j = i * 32; j < i * 32 + 32; j++) {
                byte byty = (byte) (i % 255);
                byte temp = (byte) (byteSequence[l] ^ byty);
                oldByteSequence[j] ^= temp;
                ++l;
            }

            var bt1 = longToBytes((toSignedLong(Arrays.copyOfRange(oldByteSequence, i * 32, i * 32 + 8)) + 1) * (a));
            var bt2 = longToBytes((  (toSignedLong(Arrays.copyOfRange(oldByteSequence, i * 32 + 8, i * 32 + 16)) + 4) * (b) ));
            var bt3 = longToBytes((toSignedLong(Arrays.copyOfRange(oldByteSequence, i * 32 + 16, i * 32 + 24)) + 9) * (c));
            var bt4 = longToBytes((toSignedLong(Arrays.copyOfRange(oldByteSequence, i * 32 + 24, i * 32 + 32)) + 16) * (d));
            byte[] by = concatAll(bt1, bt2, bt3, bt4);
            for (int j = 0; j < 32; j++) {
                byteSequence[j] ^= by[j];
            }
        }
    }

    private static void successiveXor(byte[][] arrayOfArrays, byte[] byteSequence, byte xored) {
        int pointer = 0;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 8; ++j) {
                var a = (byte) ((i + 1) * (j + 1));
                arrayOfArrays[i][j] = (byte) (a ^ byteSequence[pointer] ^ (xored));
                if (j > 0) {
                    arrayOfArrays[i][j] ^= (byte) (arrayOfArrays[i][j - 1]);
                } else {
                    if (i > 0) {
                        arrayOfArrays[i][j] ^= (byte) (arrayOfArrays[i - 1][7]);
                    }
                }
                ++pointer;
            }
        }
        arrayOfArrays[0][0] ^= arrayOfArrays[3][7];
    }

    private static byte[] multiplyElementsByPower(byte[][] arrayOfArrays) {

        byte[] bitos = new byte[4];

        for (int i = 0; i < 4; ++i) {
            bitos[i] = (byte) (arrayOfArrays[i][0] ^ arrayOfArrays[i][1] ^ arrayOfArrays[i][6] ^ arrayOfArrays[i][7]);
        }

        long first = (long) pow(bitos[0], 1) == 0 ? 1 : (long) pow(bitos[0], 1);
        long second = (long) bitos[1] == 0 ? 2 : (long) pow(bitos[1], 2);
        long third = (long) bitos[2] == 0 ? 4 : (long) pow(bitos[2], 3);
        long fourth = (long) bitos[3] == 0 ? 27 : (long) pow(bitos[3], 4);
        long ginomeno = first * second * third * fourth;


        return longToBytes(ginomeno);

    }

    private static byte xoredLength(int originalLength) {
        assert (originalLength >= 0);
        if (originalLength <= 127) {
            return (byte) originalLength;
        }
        return originalLength % 128 != 0 ? (byte) (originalLength % 128) : 127;
    }

}

















