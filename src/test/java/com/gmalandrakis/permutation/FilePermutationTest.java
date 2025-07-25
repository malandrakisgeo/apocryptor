package com.gmalandrakis.permutation;

import com.gmalandrakis.APOCRYPTOR;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.gmalandrakis.key_derivation.archive.KeyDerivation_V1.getKeyFromInput;
import static com.gmalandrakis.permutation.PermutationAlgorithm.permuteArraysByKey;
import static com.gmalandrakis.permutation.PermutationAlgorithm.unpermuteArraysByKey;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilePermutationTest {

    private static int CHUNK_SIZE_IN_BYTES = 192 / 8;

    @Test
    public void filePermutationTest() throws Throwable {
        File fil = Paths.get(APOCRYPTOR.class.getClassLoader().getResource("permute_me").toURI()).toFile();

        FileInputStream fileInputStream = new FileInputStream(fil);
        var size = fileInputStream.getChannel().size();
        //fileInputStream.getChannel().read(buffer, 0);
        // buffer.flip();

        var chunks = size / CHUNK_SIZE_IN_BYTES;
        if (size > chunks * CHUNK_SIZE_IN_BYTES) {
            ++chunks;
        }
        var iterations = chunks / 32;
        if (chunks > iterations * 32) {
            ++iterations;
        }
        byte[][] fileContents = new byte[32][];

        for (int i = 0; i < iterations; i++) {

            for (int j = 0; j < 32; j++) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(CHUNK_SIZE_IN_BYTES);

                var pos = (long) CHUNK_SIZE_IN_BYTES * j + ((long) i * 32 * CHUNK_SIZE_IN_BYTES);
                fileInputStream.getChannel().read(buffer, pos);
                buffer.flip();
                fileContents[j] = new byte[buffer.remaining()];
                buffer.get(fileContents[j]);
                buffer.flip();

            }
        }
        var k1 = getKeyFromInput("12".getBytes()); //12 gia duo cons //TODO: Me ton kwdiko 12 kapoia emfanizontai null. Giati?

        var permutedArray = permuteArraysByKey(fileContents, k1);

        var originalArray = unpermuteArraysByKey(permutedArray, k1);

        for (int i = 0; i < originalArray.length; i++) {
            assertTrue(Arrays.equals(originalArray[i], fileContents[i] ));
        }
    }
}
