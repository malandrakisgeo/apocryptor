package com.gmalandrakis.key_derivation.archive;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static java.lang.Math.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class V1 {

    /*
    1. Σπαμε τον δοθεντα κωδικό σε τμήματα των 256bits (32 bytes) τα οποία κάνουμε xor μεταξύ τους.
Αν ο κωδικός είναι μικρότερος από 256 μπιτσ, βάζουμε όσα χρειάζεται ως padding (μηδενισμένα).
2. Κάθε byte του αποτελέσματος γίνεται OR με τον αριθμό των χαρακτήρων που δόθηκαν.
3. Σπάμε το αποτέλεσμα σε 4 doubles (8 bytes το καθενα):
    Q,W,E,R
Για την ακριβεια τα σπαμε σε οχτω ομαδες των τεσσαρων βυτες, που κατα περισταση αντιμετωπιζουμε ως double
ή long.
4. Συνάγουμε δυο doubles (ολα τα παραπάνω αντιμετωπίζονται ως δουβλες):
    1: cos (Q) + SIN (E) στρογγυλοποιημενο στους 12 δεκαδικούς, unsigned (δεν μηδενίζεται στους πραγματικους -μεταξυ 0,κατι και 2 παντα)
    2: COS (W)) + sin (R) στρογγυλοποιημενο στους 12
5: Για καθε σετ χαρακτηρων, κάνουμε xor το δεξί και το αριστερό τους τεταρτημοριο, και προκυπτουν 4 16μπιτα.
    Για καθε δεκαεξαμπιτο, αφοτου το αντιμετωπίσουμε ως short βρισκουμε τον αμέσως επόμενο πρωτο αριθμο.
    Πολλαπλασιάζουμε αυτούς τους πρώτους αριθμούς μεταξύ τους και καταχωρούμε το αποτέλεσμα σε long long (64bit). Εξακρίβωσε
    ότι δεν υπάρχει περίπτωση υπερχειλισης.
6. Κάνουμε xor το προηγούμενο αποτέλεσμα με το 1 και το 2 του βήματος τέσσερα.
7. xnor το αποτέλεσμα του 6 με τα Q,W,E,R αντιστοιχα.
     */

    static ConcurrentHashMap<String, String> testo = new ConcurrentHashMap<>();
    static Set<String> collisions_detected = Collections.synchronizedSet(new HashSet<>());
    static Set<String> prime_collisions = Collections.synchronizedSet(new HashSet<>());
    static Set<Long> prime_col = Collections.synchronizedSet(new HashSet<>());


    public static void main(String[] args) throws RuntimeException {
       var res =  repeat("1");
        var ress = repeat("2");

        var threadPool = Executors.newCachedThreadPool();


        for (int i = 0; i < 10; ++i) {
           threadPool.execute(() -> concurrently());
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var to = 1;
        System.out.println("2");
        var te =  repeat(String.valueOf(3106146762245053741L));
        var tet = repeat(String.valueOf(1116344660356152553l));

        System.out.println(collisions_detected.size());
        System.out.println(prime_col.size());

    }

    static void concurrently() {
        for (int i = 0; i < 10000; ++i) {
            var randomPassword = new Random().nextInt();
            var key = repeat(String.valueOf(randomPassword));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }
    }

    static void addIfAbsent(String key, String originalPassword) {
        var parousatimh = testo.get(key);
        if(parousatimh != null && !parousatimh.equals(originalPassword)){
            verifyCollision(originalPassword, parousatimh);
        }
        testo.put(key,originalPassword);

    }

    static void verifyCollision(String previousPassword, String newPassword){
        var key = repeat(previousPassword);
        var key2 = repeat(newPassword);
        if(Arrays.equals(key[0], key2[0]) && Arrays.equals(key[1], key2[1]) && Arrays.equals(key[2], key2[2]) && Arrays.equals(key[3], key2[3])){
            System.out.println("key: " + key + " for password: " + newPassword + " existed for " + previousPassword);
            collisions_detected.add(newPassword);
            collisions_detected.add(previousPassword);
        }

    }

    static byte[][] repeat(String str) {
        var originallength = str.length();

        if (originallength < 32) {
            var difference = 32 - originallength;
            for (int i = 0; i < difference; ++i) {
                str += "0";
            }
        }

        if (originallength > 32) {
            //TODO
        }


        var b = str.getBytes();


        byte[][] arrayOfArrays = new byte[4][8];

        int pointer = 0; //pointer: 8*i + j
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 8; ++j) {
                arrayOfArrays[i][j] = (byte) (b[pointer] | ((char) originallength));
                ++pointer;
            }
        }


        double a = cos(toDouble(arrayOfArrays[0])) + sin(toDouble(arrayOfArrays[1]));
        double bb = cos(toDouble(arrayOfArrays[2])) + sin(toDouble(arrayOfArrays[3]));
        //  System.out.println(a);
        //System.out.println(bb);

        short[] shorts = new short[4];
        for (int i = 0; i < 4; ++i) {
            byte[] right_tetar = new byte[]{arrayOfArrays[i][0], arrayOfArrays[i][1]};
            byte[] left_tetar = new byte[]{arrayOfArrays[i][6], arrayOfArrays[i][7]};
            var common = new byte[]{(byte) (right_tetar[0] ^ left_tetar[0]), (byte) (right_tetar[1] ^ left_tetar[1])};
           // shorts[i] = (short) (((common[0] & 0xFF) << 8) | (common[1] & 0xFF));
            shorts[i] = (short) (common[0]  * common[1] );
        }

        int[] primes = new int[4];

        for (int i = 0; i < shorts.length; i++) {
            primes[i] = nextPrime(shorts[i]);
        }

        long lol = (long) primes[0] * primes[1] * primes[2] * primes[3];
        prime_col.add(lol);
        //  System.out.println(lol);

        byte[] tox = longToBytes(lol);
        byte[] trig1 = doubleToBytes(a);
        byte[] trig2 = doubleToBytes(bb);


        for (int j = 0; j < 8; ++j) {
            tox[j] = (byte) (tox[j] ^ trig1[j] ^ trig2[j]);
        }


        for (int i = 0; i < 4; ++i) {
            byte leftmost_byte_cubed = (byte)(i^3 & 0xFF);
            for (int j = 0; j < 8; ++j) {
                arrayOfArrays[i][j] = (byte) (arrayOfArrays[i][j] ^ tox[j] ^ leftmost_byte_cubed);
            }
        }

        // System.out.println(Arrays.deepToString(arrayOfArrays));

        return arrayOfArrays;
    }

    static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    static byte[] doubleToBytes(double x) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(x);
        return buffer.array();
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


    static int nextPrime(int input) { //thank you, stackoverflow!
        int counter;
        input++;
        while (true) {
            int l = (int) sqrt(input);
            counter = 0;
            for (int i = 2; i <= l; i++) {
                if (input % i == 0) counter++;
            }
            if (counter == 0)
                return input;
            else {
                input++;
                continue;
            }
        }
    }


}
















