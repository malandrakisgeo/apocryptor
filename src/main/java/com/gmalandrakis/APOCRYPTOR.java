package com.gmalandrakis;

import com.gmalandrakis.permutation.BlockPermutation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.gmalandrakis.chaffing.KeyBasedChaffing.insert;
import static com.gmalandrakis.chaffing.KeyBasedWinnowing.winnow;
import static com.gmalandrakis.key_derivation.KeyDerivation.deduceKey;
import static com.gmalandrakis.utils.Utils.*;
/**
 *  Copyright (C) 2025 Georgios Malandrakis <malandrakisgeo@gmail.com>
 */
public class APOCRYPTOR {
    final static String sourceFilename = "ENCRYPT_ME";
    final static String encryptedWithoutChaffing = "ENCRYPTED_NO_CHAFFING";
    final static String finalEncryptedFile = "ENCRYPTED_WITH_CHAFFING";
    final static String filename3 = "ENCRYPTED_CHAFFING_REMOVED";
    final static String finalDecryptedFile = "DECRYPTED";
    final static String password = "123";


    public static void main(String args[]) throws Throwable {
        File ENCRYPT_ME = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(sourceFilename).toURI()).toFile();
        File ENCRYPTED_NO_CHAFFING = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(encryptedWithoutChaffing).toURI()).toFile();
        File ENCRYPTED_WITH_CHAFFING = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(finalEncryptedFile).toURI()).toFile();
        File ENCRYPTED_CHAFFING_REMOVED = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(filename3).toURI()).toFile();
        File DECRYPTED = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(finalDecryptedFile).toURI()).toFile();

        wipePrevious();

        byte[] finalKey = encrypt(ENCRYPT_ME, ENCRYPTED_NO_CHAFFING, password);
        byte[] fileDigest = fileDigest(ENCRYPT_ME);
        var chafPos = chaffingPositions(finalKey);
        //var chafPos = adjustChaffingPositions(chaffingPositions(finalKey), );
        insert(ENCRYPTED_NO_CHAFFING, ENCRYPTED_WITH_CHAFFING, fileDigest, chafPos);

        var retrievedFileDigest = winnow(ENCRYPTED_WITH_CHAFFING, ENCRYPTED_CHAFFING_REMOVED, chafPos);
        if (!Arrays.equals(retrievedFileDigest, fileDigest)) {
            throw new RuntimeException("Digest retrieval failed");
        }

        decrypt(ENCRYPTED_CHAFFING_REMOVED, DECRYPTED, fileDigest, password);
        FileInputStream originalfileInputStream = new FileInputStream(ENCRYPT_ME);
        FileInputStream finalEncryption = new FileInputStream(ENCRYPTED_WITH_CHAFFING);

        FileInputStream decryptedfileInputStream = new FileInputStream(DECRYPTED);

        var original = originalfileInputStream.readAllBytes();
        var decrypted = decryptedfileInputStream.readAllBytes();

        if (!Arrays.equals(original, decrypted)) {
            throw new RuntimeException("Decryption failed");
        }
        var encrypted = finalEncryption.readAllBytes();

        System.out.println("Original file contents: " + Arrays.toString(original));

        System.out.println("Encrypted file contents: " + Arrays.toString(encrypted));
        System.out.println("Recovered file contents: " + Arrays.toString(decrypted));

        finalEncryption.close();
        originalfileInputStream.close();
        decryptedfileInputStream.close();

        System.out.println("Success!");
    }
    /*
        1. Vres message digest tou arxeiou M
        2. Vres message digest tou kwdikou K
        3. Vres deutero message digest tou K+M, trito message digest tou M+K, meta tou MKM, telos tou KMK
        4. Xor tou prwtou block me ta tou tritou vhmatos. To kratame sth mnhmh atofio ws R, enw vazoume to permutation tou sto arxeio.
        5. Xor R kai deuterou block, kai meta diadoxika XOR me to deutero message digest tou K. To kratame sth mnhmh, permute sto arxeio
        kok

     */


    public static byte[] fileDigest(File filename) throws Throwable {
        FileInputStream fileInputStream = new FileInputStream(filename);

        return deduceKey(fileInputStream.readAllBytes());
    }

    public static byte[] deduceFirstXor(String keyString, String fileDigestString) {
        var KM = keyString + fileDigestString;
        var MK = fileDigestString + keyString;
        var MKM = fileDigestString + keyString + fileDigestString;
        var KMK = keyString + fileDigestString + keyString;
        var key1 = deduceKey(deduceKey(KM.getBytes()));
        var key2 = deduceKey(deduceKey(MK.getBytes()));
        var key3 = deduceKey(deduceKey(MKM.getBytes()));
        var key4 = deduceKey(deduceKey(KMK.getBytes()));

        byte[] concatArray = new byte[0];
        for (int i = 0; i < 8; i++) {
            if (concatArray.length == 0) {
                concatArray = concatAll(key1, key2, key3, key4);
            } else {
                concatArray = concatAll(concatArray, key1, key2, key3, key4);

            }
            key1 = deduceKey(key1);
            key2 = deduceKey(key2);
            key3 = deduceKey(key3);
            key4 = deduceKey(key4);
        }


        return concatArray;

    }

    public static byte[] deducePad(byte[] previousEncryptedBlock, byte[] nThDigestOfKey) {
        /*
            IDEARA GAMHSTERH:
                Kanoume permute to previousEncryptedBlock me vash to nthDigest, kai epistrefoume auto!
                Sthn ousia, mono prin thn apothikeush tou prwtou block tha xreiastei na to permutaroume.
         */
        var basePad = BlockPermutation.permuteBlock(nThDigestOfKey, previousEncryptedBlock);


        /*
            The permutation algorithm is very simple minded (shuffling parts of 32 bytes), so it would allow for an attack:
            if one block was made up of repeated data (e.g. zeros only), the attacker would not have a tough time re-xoring with the
            previous block. We introduce some extra XORs here as a patch. TODO: Come up with a more elaborate block shuffling algorithm.
         */
        int l = 0;
        for (int i = 0; i < basePad.length; i++) {
            basePad[i] ^= nThDigestOfKey[l++];
            if (l == 32) {
                l = 0;
            }
        }
        return basePad;
    }

    public static byte[] deduceLastPad(byte[] previousEncryptedBlock, byte[] nThDigestOfKey, int length) {
        var basePad = BlockPermutation.permuteBlock(nThDigestOfKey, previousEncryptedBlock);
        var pad = new byte[length];
        int l = 0;
        for (int i = 0; i < length; i++) {
            pad[i] ^= basePad[i];
            pad[i] ^= nThDigestOfKey[l++];
            if (l == 32) {
                l = 0;
            }
        }
        return pad;
    }

    public static byte[] xorWithPad(byte[] content, byte[] pad) {
        byte[] result = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            result[i] ^= content[i];
            result[i] ^= pad[i];
        }

        return result;
    }


    public static byte[] encrypt(File inputFile, File outputFile, String key) throws Throwable {

        var fileDigest = fileDigest(inputFile);
        var keyDigest = deduceKey(key.getBytes());
        FileInputStream fileInputStream = new FileInputStream(inputFile);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        var firstXor = deduceFirstXor(Arrays.toString(keyDigest), Arrays.toString(fileDigest));
        var permutedFirstXor = BlockPermutation.permuteBlock(keyDigest, firstXor);
        assert (Arrays.equals(BlockPermutation.unpermuteBlock(keyDigest, permutedFirstXor), permutedFirstXor));
        int chunks = fileInputStream.available() / 1024;
        int extra = fileInputStream.available() % 1024;

        byte[] cipher = chunks != 0 ? firstXor : new byte[1024];
        byte[] nthKey = keyDigest;
        byte originalText[];

        for (int i = 0; i < chunks; i++) {
            var pad = deducePad(cipher, nthKey);
            originalText = fileInputStream.readNBytes(1024);
            cipher = xorWithPad(originalText, pad);
            nthKey = deduceKey(nthKey);
            fileOutputStream.write(cipher);
        }
        //correct up to this point
        if (extra > 0) {
            originalText = fileInputStream.readNBytes(extra);

            var pad = deduceLastPad(cipher, nthKey, extra);
            for (int i = 0; i < extra; i++) {
                originalText[i] ^= pad[i];
            }
            fileOutputStream.write(originalText);
        }

        fileOutputStream.flush();
        fileOutputStream.close();
        fileInputStream.close();
        return nthKey;

    }


    public static byte[] decrypt(File inputFile, File outputFile, byte[] digestByWinnowing, String password) throws Throwable {

        var keyDigest = deduceKey(password.getBytes());
        FileInputStream fileInputStream = new FileInputStream(inputFile);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        var firstXor = deduceFirstXor(Arrays.toString(keyDigest), Arrays.toString(digestByWinnowing));
        var permutedFirstXor = BlockPermutation.permuteBlock(keyDigest, firstXor);
        assert (Arrays.equals(BlockPermutation.unpermuteBlock(keyDigest, permutedFirstXor), permutedFirstXor));
        int chunks = (fileInputStream.available()) / 1024;
        int extra = (fileInputStream.available()) % 1024;
        /*
            1. Vres ta prwta 1024 bytes ARX kai to arxiko pad tous. Kane ta xor. Estw A to apotelesma (arxiko keimeno).
            Apothikeveis A sto arxeio.
            2. Ta deutera 1024 ginontai XOR me to ARX, kai to deutero digest. kok
         */
        byte[] nthKey = keyDigest;
        byte[] tempArray = new byte[1024];
        byte[] cipher = new byte[1024];
        byte[] pad = deducePad(firstXor, nthKey);
        tempArray = cipher;
        for (int i = 0; i < chunks; i++) {
            if (i >= 1) {
                pad = deducePad(tempArray, nthKey);
            }
            cipher = fileInputStream.readNBytes(1024);
            var originalText = xorWithPad(cipher, pad);
            fileOutputStream.write(originalText);
            tempArray = cipher;
            nthKey = deduceKey(nthKey);
        }
        //correct up to this point
        if (extra > 0) {
            pad = deduceLastPad(cipher, nthKey, extra);
            cipher = fileInputStream.readNBytes(extra);
            for (int i = 0; i < extra; i++) {
                cipher[i] ^= pad[i];
            }
            fileOutputStream.write(cipher);
        }

        fileOutputStream.flush();
        fileOutputStream.close();
        fileInputStream.close();
        return nthKey;
    }

    static void wipePrevious() throws Throwable {
        File ENCRYPTED_NO_CHAFFING = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(encryptedWithoutChaffing).toURI()).toFile();
        File ENCRYPTED_WITH_CHAFFING = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(finalEncryptedFile).toURI()).toFile();
        File ENCRYPTED_CHAFFING_REMOVED = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(filename3).toURI()).toFile();
        File DECRYPTED = Paths.get(APOCRYPTOR.class.getClassLoader().getResource(finalDecryptedFile).toURI()).toFile();

        FileOutputStream fileOutputStream = new FileOutputStream(ENCRYPTED_NO_CHAFFING);
        fileOutputStream.close();
        fileOutputStream = new FileOutputStream(ENCRYPTED_WITH_CHAFFING);
        fileOutputStream.close();
        fileOutputStream = new FileOutputStream(ENCRYPTED_CHAFFING_REMOVED);
        fileOutputStream.close();
        fileOutputStream = new FileOutputStream(DECRYPTED);
        fileOutputStream.close();
    }


}
