package com.gmalandrakis.key_derivation.archive;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static java.lang.Math.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class V2_working {

    /*
    1. Σπαμε τον δοθεντα κωδικό σε τμήματα των 256bits (32 bytes) τα οποία κάνουμε xor μεταξύ τους.
Αν ο κωδικός είναι μικρότερος από 256 μπιτσ, βάζουμε όσα χρειάζεται ως padding (μηδενισμένα).
2. Κάθε byte του αποτελέσματος γίνεται OR με τον αριθμό των χαρακτήρων που δόθηκαν ΚΑΙ XOR ΜΕ ΤΗ ΘΕΣΗ ΤΟΥ.
3. Σπάμε το αποτέλεσμα σε 4 doubles (8 bytes το καθενα):
    Q,W,E,R
Για την ακριβεια τα σπαμε σε οχτω ομαδες των τεσσαρων βυτες, που κατα περισταση αντιμετωπιζουμε ως double
ή long.
4. Συνάγουμε δυο doubles (ολα τα παραπάνω αντιμετωπίζονται ως δουβλες):
    1: cos (Q) + SIN (R+E) στρογγυλοποιημενο στους 12 δεκαδικούς, unsigned (δεν μηδενίζεται -μεταξυ 0 και 2 παντα)
    2: COS (W+R)) + sin (Q) στρογγυλοποιημενο στους 12
5: Για καθε σετ χαρακτηρων, κάνουμε xor το δεξί και το αριστερό τους τεταρτημοριο, και προκυπτουν 4 16μπιτα.
    Για καθε δεκαεξαμπιτο, αφοτου το αντιμετωπίσουμε ως short βρισκουμε τον αμέσως επόμενο πρωτο αριθμο.
    Πολλαπλασιάζουμε αυτούς τους αριθμούς μεταξύ τους και καταχωρούμε το αποτέλεσμα σε long long (64bit). Εξακρίβωσε
    ότι δεν υπάρχει περίπτωση υπερχειλισης.

5n: Κανουμε byteσ 1,2 του Q XOR με τα βυτεσ 1 & 2 των W. Το αποτελεσμα το κανουμε ξορ με τα 1,2 του Ε.
Αντιστοιχα με το R. Επειτα παιρνουμε αυτες τις
6. Κάνουμε xor το προηγούμενο αποτέλεσμα με το 1 και το 2 του βήματος τέσσερα.
7. xnor το αποτέλεσμα του 6 με τα Q,W,E,R αντιστοιχα.
8. xnor
     */

    static ConcurrentHashMap<String, String> testo = new ConcurrentHashMap<>();
    static List<String> collisions_detected = Collections.synchronizedList(new ArrayList<>());
    static Set<Long> prime_col = Collections.synchronizedSet(new HashSet<>());


    public static void main(String[] args) throws RuntimeException {
        var res =  repeat("0");
        var ress = repeat("1");

        var resss = repeat("00");

        var threadPool = Executors.newCachedThreadPool();


        for (int i = 0; i < 30; ++i) {
            threadPool.execute(() -> concurrently());
        }
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(testo.size());

        System.out.println(collisions_detected.size());
        System.out.println(prime_col.size());

    }

    static void concurrently() {
        for (int i = 0; i < 200000; ++i) {
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


        var byteSequence = str.getBytes();
      //  System.out.println(Arrays.toString(byteSequence));

        byte[][] arrayOfArrays = new byte[4][8];

        int pointer = 0; //pointer: 8*i + j
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 8; ++j) {
                arrayOfArrays[i][j] = (byte) (byteSequence[pointer] | ((char) originallength));
               var a  = (byte) ((i+1) * (j+1));
               if(pointer>0){
                   arrayOfArrays[i][j] ^= (byte) (a ^ byteSequence[pointer-1]);
               }
                ++pointer;
            }
        }
       // System.out.println(Arrays.deepToString(arrayOfArrays));



        double a = cos(toLong(arrayOfArrays[0])) + pow(sin(toLong(arrayOfArrays[1]) + toLong(arrayOfArrays[2])),1);
        double bb = pow(cos(toLong(arrayOfArrays[2]) + toLong(arrayOfArrays[3])),1) + pow(sin(toLong(arrayOfArrays[0])),1);
       //  System.out.println(a);
     //   System.out.println(bb);

        short[] shorts = new short[4];
        for (int i = 0; i < 4; ++i) {
            byte[] right_tetar = new byte[]{arrayOfArrays[i][0], arrayOfArrays[i][1]};
            byte[] left_tetar = new byte[]{arrayOfArrays[i][6], arrayOfArrays[i][7]};
            var common = new byte[]{(byte) (right_tetar[0]), (byte) (right_tetar[1])};
          //  shorts[i] = (short) (((common[0] & 0xFF) << 8) | (common[1]));
            shorts[i] = (short) (common[0]  * common[1] );

        }

    long lol = (long) ((long) pow(shorts[0], 1 ) * (long) pow(shorts[1], 2 ) * (long) pow(shorts[2], 3 )  * ((long) pow(shorts[3], 4 ))); //3188
        //long lol = (long) ((long) pow(shorts[0], 1 ) * (long) pow(shorts[1], 1 ) * (long) pow(shorts[2], 1 )  * ((long) pow(shorts[3], 1 ))); //1885
       //  System.out.println(lol);
        prime_col.add(lol);

        byte[] tox = longToBytes(lol);
        byte[] trig1 = doubleToBytes(a);
        byte[] trig2 = doubleToBytes(bb);


        for (int j = 0; j < 8; ++j) {
            tox[j] = (byte) (tox[j] ^ trig1[j] ^ trig2[j]);
        }


        for (int i = 0; i < 4; ++i) {
            byte position_leftmostbyte_cubed = (byte)(i^3 & 0xFF);
            for (int j = 0; j < 8; ++j) {
                arrayOfArrays[i][j] = (byte) (arrayOfArrays[i][j] ^ tox[j] ^ position_leftmostbyte_cubed);
            }
        }

        // System.out.println(Arrays.deepToString(arrayOfArrays));

        return arrayOfArrays;
    }

    static double toDouble(byte[] bytes) {
        var  r = ByteBuffer.wrap(bytes).getDouble();
        return r;
    }

    static long toLong(byte[] bytes) {
        var  r = ByteBuffer.wrap(bytes).getLong();
        return r;
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
















