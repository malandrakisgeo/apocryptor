package com.gmalandrakis.key_derivation;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static com.gmalandrakis.key_derivation.KeyDerivation_V2.*;

public class KeyDerivationCollisionTest {
    static ConcurrentHashMap<Long, Long> testoNew = new ConcurrentHashMap<>();

    static ConcurrentHashMap<String, String> testo = new ConcurrentHashMap<>();
    static List<String> collisions_detected = Collections.synchronizedList(new ArrayList<>());
    static Set<Long> prime_col = Collections.synchronizedSet(new HashSet<>());

    @Test
    public void testMe() throws RuntimeException {
        var threadPool = Executors.newCachedThreadPool();
        var longn = getRandomStringLong();
        var time = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis());
        var result = deduceKey(longn.getBytes());
        System.out.println(System.currentTimeMillis() - time);

        System.out.println(Arrays.toString(result));
        longn = getRandomStringLong();
        result = deduceKey(longn.getBytes());
        System.out.println(Arrays.toString(result));

        longn = getRandomStringLong();
        result = deduceKey(longn.getBytes());
        System.out.println(Arrays.toString(result));

        longn = getRandomStringLong();
        result = deduceKey(longn.getBytes());
        System.out.println(Arrays.toString(result));

        //toSignedLong(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0});
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
     * A recursion of a depth of 1000.
     * The resulting key of a random string is used to produce a new key, which in turn is used for a new key, etc.
     */
    static void recursionTest() {
        var randomPassword = getRandomString();
        var result = deduceKey(randomPassword.getBytes());
        addIfAbsent(result, randomPassword.getBytes());

        for (int i = 0; i < 1000; i++) {
            var newResult = deduceKey(result);
            addIfAbsent(newResult, result);
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

            var key = deduceKey((randomPassword.getBytes()));
            addIfAbsent(key, randomPassword.getBytes());

        }
        for (int i = 0; i < 100000; ++i) {
            var randomPassword = getFixedLengthRandomString();

            var key = deduceKey((randomPassword.getBytes()));
            addIfAbsent(key, randomPassword.getBytes());

        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextInt();

            var key = deduceKey(String.valueOf(randomPassword).getBytes());
            addIfAbsent(key, String.valueOf(randomPassword).getBytes());

        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextDouble();

            var key = deduceKey(String.valueOf(randomPassword).getBytes());
            addIfAbsent(key, String.valueOf(randomPassword).getBytes());
        }
    }

    /**
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

    static String getRandomStringLong() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=+abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        //var length = new Random().nextInt(20);
        var length = 1024 * 1024 * 500;
        while (salt.length() < length) {
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
        long l = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN).getLong();
        if (originalPassword.length < 8) {
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
        var key = deduceKey(previousPassword);
        var key2 = deduceKey(newPassword);
        // if (Arrays.equals(key[0], key2[0]) && Arrays.equals(key[1], key2[1]) && Arrays.equals(key[2], key2[2]) && Arrays.equals(key[3], key2[3])) {
        if (Arrays.equals(key, key2)) {
            System.out.println("key: " + key + " for password: " + newPassword + " existed for " + previousPassword);
            collisions_detected.add(Arrays.toString(newPassword));
            collisions_detected.add(Arrays.toString(previousPassword));
        }

    }

}
