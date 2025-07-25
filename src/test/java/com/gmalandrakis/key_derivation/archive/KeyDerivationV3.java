package com.gmalandrakis.key_derivation.archive;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static com.gmalandrakis.key_derivation.archive.KeyDerivation_V3.*;

public class KeyDerivationV3 {

    static ConcurrentHashMap<Long, Long> testoNew = new ConcurrentHashMap<>();

    static ConcurrentHashMap<String, String> testo = new ConcurrentHashMap<>();
    static List<String> collisions_detected = Collections.synchronizedList(new ArrayList<>());
    static Set<Long> prime_col = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) throws RuntimeException {
        var longn = getRandomStringLong();
        var time = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis());

        var result = deduceKey(longn.getBytes(), longn.length());
        System.out.println(Arrays.deepToString(result));
        System.out.println(System.currentTimeMillis()- time);

        longn = getRandomStringLong();
         result = deduceKey(longn.getBytes(), longn.length());

        System.out.println(Arrays.deepToString(result));

        longn = getRandomStringLong();
         result = deduceKey(longn.getBytes(), longn.length());
        System.out.println(Arrays.deepToString(result));

        longn = getRandomStringLong();
         result = deduceKey(longn.getBytes(), longn.length());
        System.out.println(Arrays.deepToString(result));

        var threadPool = Executors.newCachedThreadPool();

        for (int i = 0; i < 40; ++i) {
            threadPool.execute(() -> concurrently());
        }
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //recursionTest();
        System.out.println("Total collisions: " + collisions_detected.size());
        System.out.println("Total keys calculated: " + testoNew.size());
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Total collisions: " + collisions_detected.size());
        System.out.println("Total keys calculated: " + testoNew.size());

    }

    /**
     A recursion of a depth of 1000.
     The resulting key of a random string is used to produce a new key, which in turn is used for a new key, etc.
     */
    static void recursionTest() {
        var randomPassword = getRandomString();
        var result = deduceKey(randomPassword.getBytes(), randomPassword.length());
        addIfAbsent(flatten(result), randomPassword.getBytes());

        for (int i = 0; i < 1000; i++) {
            var newResult = deduceKey(arrayFlattening(result), result.length);
            addIfAbsent(flatten(newResult), flatten(result));
            result = newResult;
        }

    }

   /* static void preImageTest() {
        var prePassword= "test_me_test_me_test_me_test_me"; //30 bytes
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=+abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < SALTCHARS.length(); i++) {
            var next = new Random().nextInt(SALTCHARS.length());
            var pass = prePassword + SALTCHARS.charAt(next);
            var newResult = deduceKey(pass.getBytes());
            addIfAbsent(keyToString((newResult)), pass);
        }
        for (int i = 0; i < 2*SALTCHARS.length(); i++) {
            var next = new Random().nextInt(SALTCHARS.length());
            var nextnext = new Random().nextInt(SALTCHARS.length());
            var pass = prePassword + SALTCHARS.charAt(next)+ SALTCHARS.charAt(nextnext);
            var newResult = deduceKey(pass.getBytes());
            addIfAbsent(keyToString((newResult)), pass);
        }
        for (int i = 0; i < 3*SALTCHARS.length(); i++) {
            var next = new Random().nextInt(SALTCHARS.length());
            var nextnext = new Random().nextInt(SALTCHARS.length());
            var nextnextnext = new Random().nextInt(SALTCHARS.length());

            var pass = prePassword + SALTCHARS.charAt(next)+ SALTCHARS.charAt(nextnext) + SALTCHARS.charAt(nextnextnext);
            var newResult = deduceKey(pass.getBytes());
            addIfAbsent(keyToString((newResult)), pass);

        }

    }*/

    static void concurrently() {
        for (int i = 0; i < 2500000; ++i) {
            var randomPassword = getRandomString();

            var key = deduceKey(randomPassword.getBytes(), randomPassword.length());
            addIfAbsent(flatten(key), randomPassword.getBytes());

        }
        for (int i = 0; i < 100000; ++i) {
            var randomPassword = getFixedLengthRandomString();

            var key = deduceKey(randomPassword.getBytes(), randomPassword.length());
            addIfAbsent(flatten(key),randomPassword.getBytes());

        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassLength = new Random().nextInt();
            var pass = String.valueOf(randomPassLength).getBytes();
            var key = deduceKey(pass, pass.length);
            addIfAbsent(flatten(key), String.valueOf(randomPassLength).getBytes());

        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextDouble();
            var pass = String.valueOf(randomPassword).getBytes();

            var key = deduceKey(pass, pass.length);
            addIfAbsent(flatten(key), String.valueOf(randomPassword).getBytes());
        }
    }

    /**
     *
     * @return A random string of a length anywhere between one and twenty
     */
    static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=+abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        var length = new Random().nextInt(20);
        //var length = 255;
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
    static String getRandomStringLong() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=+abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        //var length = new Random().nextInt(20);
        var length = 1024*1024*500;
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    static String getFixedLengthRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=+abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }


    static String keyToString(byte[][] key) {
        String result = "";
        for (int i = 0; i < 4; ++i) {
            try {
                result += new String(key[i], "UTF-16");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }




    static void addIfAbsent(byte[] key, byte[] originalPassword) {
        long l =  ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN).getLong();
        if(originalPassword.length<8){
            originalPassword = Arrays.copyOf(originalPassword, 8);
        }
        long l2 = ByteBuffer.wrap(originalPassword).order(ByteOrder.LITTLE_ENDIAN).getLong();

        var parousatimh = testoNew.get(l);
        if (parousatimh != null && !parousatimh.equals(l2)) {
            verifyByteCollision(originalPassword, key);
        }
        testoNew.put(l, l2);

    }

    static void verifyByteCollision(byte[] previousPassword, byte[] newPassword) { //different byte sequences may be mapped to same strings, such as '?????????'
        var key = deduceKey(previousPassword, previousPassword.length);
        var key2 = deduceKey(newPassword, newPassword.length);
        if (Arrays.equals(key[0], key2[0]) && Arrays.equals(key[1], key2[1]) && Arrays.equals(key[2], key2[2]) && Arrays.equals(key[3], key2[3])) {
            System.out.println("key: " + key + " for password: " + newPassword + " existed for " + previousPassword);
            collisions_detected.add(Arrays.toString(newPassword));
            collisions_detected.add(Arrays.toString(previousPassword));
        }

    }

}
