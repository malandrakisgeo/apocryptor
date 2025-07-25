package com.gmalandrakis.key_derivation;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static com.gmalandrakis.key_derivation.KeyDerivation_V2.*;

public class CollisionTestOld {
    /*
        This test has a "small" problem: the concurrenthashmap can never hold more than 50m string values, no matter
        what xmx is run with. The reason seems to be that Java simply runs out of possible hashes for those strings,
        so new values overwrite the previous ones thanks to hash collisions.
     */
    static ConcurrentHashMap<String, String> testo = new ConcurrentHashMap<>();
    static List<String> collisions_detected = Collections.synchronizedList(new ArrayList<>());
    static Set<Long> prime_col = Collections.synchronizedSet(new HashSet<>());

    @Test
    public void testme() throws RuntimeException {
        //preImageTest();
        var time = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis());

        recursionTest();
        System.out.println(System.currentTimeMillis() - time);

        var threadPool = Executors.newCachedThreadPool();
        addIfAbsent(keyToString(deduceKey(new byte[]{0, 0})), "00");
        addIfAbsent(keyToString(deduceKey(new byte[]{0})), "0");

        for (int i = 0; i < 2500000; ++i) {
            var randomPassword = getRandomString();

            var key = deduceKey((randomPassword.getBytes()));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }

        for (int i = 0; i < 40; ++i) {
            threadPool.execute(() -> concurrently());
        }
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Total collisions: " + collisions_detected.size());
        System.out.println("Total keys calculated: " + testo.size());
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Total collisions: " + collisions_detected.size());
        System.out.println("Total keys calculated: " + testo.size());
/// 64000000
    }

    /**
     * A recursion of a depth of 1000.
     * The resulting key of a random string is used to produce a new key, which in turn is used for a new key, etc.
     */
    static void recursionTest() {
        var randomPassword = getRandomString();
        var result = deduceKey(randomPassword.getBytes());
        addIfAbsent(keyToString(result), randomPassword);

        for (int i = 0; i < 2000000; i++) {
            var newResult = deduceKey(result);
            throwIfExists(keyToString(newResult), keyToString(result));
            result = newResult;
        }

    }

    static void preImageTest() {
        var prePassword = "test_me_test_me_test_me_test_me"; //30 bytes
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=+abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < SALTCHARS.length(); i++) {
            var next = new Random().nextInt(SALTCHARS.length());
            var pass = prePassword + SALTCHARS.charAt(next);
            var newResult = deduceKey(pass.getBytes());
            addIfAbsent(keyToString((newResult)), pass);
        }
        for (int i = 0; i < 2 * SALTCHARS.length(); i++) {
            var next = new Random().nextInt(SALTCHARS.length());
            var nextnext = new Random().nextInt(SALTCHARS.length());
            var pass = prePassword + SALTCHARS.charAt(next) + SALTCHARS.charAt(nextnext);
            var newResult = deduceKey(pass.getBytes());
            addIfAbsent(keyToString((newResult)), pass);
        }
        for (int i = 0; i < 3 * SALTCHARS.length(); i++) {
            var next = new Random().nextInt(SALTCHARS.length());
            var nextnext = new Random().nextInt(SALTCHARS.length());
            var nextnextnext = new Random().nextInt(SALTCHARS.length());

            var pass = prePassword + SALTCHARS.charAt(next) + SALTCHARS.charAt(nextnext) + SALTCHARS.charAt(nextnextnext);
            var newResult = deduceKey(pass.getBytes());
            addIfAbsent(keyToString((newResult)), pass);
        }
        for (int i = 0; i < 4 * SALTCHARS.length(); i++) {
            var next = new Random().nextInt(SALTCHARS.length());
            var nextnext = new Random().nextInt(SALTCHARS.length());
            var nextnextnext = new Random().nextInt(SALTCHARS.length());
            var nextnextnextnext = new Random().nextInt(SALTCHARS.length());

            var pass = prePassword + SALTCHARS.charAt(next) + SALTCHARS.charAt(nextnext) + SALTCHARS.charAt(nextnextnext) + SALTCHARS.charAt(nextnextnextnext);
            var newResult = deduceKey(pass.getBytes());
            addIfAbsent(keyToString((newResult)), pass);
        }

    }


    static void concurrently() {
        for (int i = 0; i < 2500000; ++i) {
            var randomPassword = getRandomString();

            var key = deduceKey((randomPassword.getBytes()));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }
        for (int i = 0; i < 100000; ++i) {
            var randomPassword = getFixedLengthRandomString();

            var key = deduceKey((randomPassword.getBytes()));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextInt();

            var key = deduceKey(String.valueOf(randomPassword).getBytes());
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextDouble();

            var key = deduceKey(String.valueOf(randomPassword).getBytes());
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
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

    static String keyToString(byte[] key) {
        String result = "";
        try {
            result = new String(key, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    static void addIfAbsent(String key, String originalPassword) {
        var parousatimh = testo.get(key);
        if (parousatimh != null && !parousatimh.equals(originalPassword)) {
            verifyCollision(originalPassword, parousatimh);
        }
        testo.put(key, originalPassword);

    }

    /*
        Used for the recursive algorithm only. We should *never* end up with the same key.
        Well... almost never (if you run it more than 2^256 times, well, you do end
        up with some existent key).
     */
    static void throwIfExists(String key, String originalPassword) {
        var parousatimh = testo.get(key);
        if (parousatimh != null) {
            throw new RuntimeException("EEEEEEEEEEEEEEEEEEEEEE");
        }
        testo.put(key, originalPassword);

    }

    static void verifyCollision(String previousPassword, String newPassword) { //different byte sequences may be mapped to same strings, such as '?????????'
        var key = deduceKey(previousPassword.getBytes());
        var key2 = deduceKey(newPassword.getBytes());
        if (Arrays.equals(key, key2)) {
            System.out.println("key: " + key + " for password: " + newPassword + " existed for " + previousPassword);
            collisions_detected.add(newPassword);
            collisions_detected.add(previousPassword);
        }

    }

}
