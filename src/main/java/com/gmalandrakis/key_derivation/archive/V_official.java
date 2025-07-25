package com.gmalandrakis.key_derivation.archive;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static java.lang.Math.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class V_official {

    /*
    1. Σπαμε τον δοθεντα κωδικό σε τμήματα των 256bits (32 bytes) τα οποία κάνουμε xor μεταξύ τους.
Αν ο κωδικός είναι μικρότερος από 256 μπιτσ, βάζουμε όσα χρειάζεται ως padding (μηδενισμένα).
2. Κάθε byte του αποτελέσματος γίνεται OR με τον αριθμό των χαρακτήρων που δόθηκαν ΚΑΙ XOR ΜΕ ΤΗ ΘΕΣΗ ΤΟΥ.
3. Σπάμε το αποτέλεσμα σε 4 doubles (8 bytes το καθενα):
    Q,W,E,R
Για την ακριβεια τα σπαμε σε οχτω ομαδες των τεσσαρων βυτες, που κατα περισταση αντιμετωπιζουμε ως double
ή long.
3A) Στη θεση καθε μεμονωμένου byte των Q,W,E,R,
αποθηκευουμε τον εαυτό του xor με το μήκος του κωδικού, καθώς και με το αμέσως
προηγούμενο αποθηκευμένο byte (το πρώτο byte του Q εξαιρείται από το δεύτερο σκέλος)
4. Συνάγουμε δυο doubles (ολα τα παραπάνω αντιμετωπίζονται ως δουβλες):
    1: cos (Q) + SIN (R+E) στρογγυλοποιημενο στους 12 δεκαδικούς, unsigned (δεν μηδενίζεται -μεταξυ 0 και 2 παντα)
    2: COS (W+R)) + sin (Q) στρογγυλοποιημενο στους 12
5. Για καθε ενα απο τα νέα Q,W,E,R
παίρνουμε τα δυο πρώτα και τα δυο τελευταία bytes, και σχηματίζουμε ένα καινούριο
κάνοντας τα xor μεταξύ τους ως εξής: πρώτο με έβδομο, δεύτερο με όγδοο και μετά και τα αποτελέσματα
μεταξύ τους (εξέτασε κατά ποσον ειναι ισοδύναμο με το να τα εκανες ολα xor μονομιας).
Αποθηκεύουμε τα αποτελέσματα (τέσσερα bytes) σε τιμές V,B,N,M
6. Σχηματίζουμε long 64 bits υψώνοντας τα V,B,N,M σε 1η, 2η, 3η, 4η δυναμη αντιστοιχα
και πολλαπλασιαζοντας τα αποτελεσματα μεταξυ τους. Ο μεγαλυτερος
αριθμος που μπορει να προκυψει ειναι 255^1 * 255^2 * 255^3 * 255^4
Εστω αποτελεσμα 3:
7. Κάνουμε XOR  τα αντιστοιχα bytes των 1,2,3 (πρώτο με πρώτο, δεύτερο με δεύτερο, κ.ο.κ)
8. Αντικαθιστουμε τα bytes των Q,W,E,R με τα ΞΟΡ τους με τα bytes που προεκυψαν
απο το έβδομο βήμα, καθώς και με τον κύβο του αριθμού του αντίστοιχου byte (π.χ.
το Ε που ειναι τριτο, ολα του τα βυτες γινονται xor με το 27, και με το αντιστοιχο βυτε του 7ου βηματος
πρώτο με πρώτο, δεύτερο με δεύτερο, κ.ο.κ)

     */

    static ConcurrentHashMap<String, String> testo = new ConcurrentHashMap<>();
    static List<String> collisions_detected = Collections.synchronizedList(new ArrayList<>());
    static Set<Long> prime_col = Collections.synchronizedSet(new HashSet<>());


    public static void main(String[] args) throws RuntimeException {
        var res = repeat("0");
        var ress = repeat("1");
        var resss = repeat("00");

        var threadPool = Executors.newCachedThreadPool();


        for (int i = 0; i < 30; ++i) {
           // threadPool.execute(() -> concurrently());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        recursionTest();
        System.out.println("Total collisions: " + collisions_detected.size());
        System.out.println("Total different lols: " + prime_col.size());
        System.out.println("Total keys calculated: " + testo.size());



    }

    static void recursionTest(){
        var randomPassword = getRandomString();
        var result = repeat(randomPassword);
        addIfAbsent(keyToString(result), randomPassword);

        for(int i=0; i<5000; i++){
            var newResult = repeat(keyToString(result));
            addIfAbsent(keyToString(newResult), keyToString(result));
            result = newResult;
        }


    }

    static void concurrently() {
        for (int i = 0; i < 2500000; ++i) {
            var randomPassword = getRandomString();

            var key = repeat(String.valueOf(randomPassword));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }

     /*   for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextInt();

            var key = repeat(String.valueOf(randomPassword));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextDouble();

            var key = repeat(String.valueOf(randomPassword));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }*/
    }

    static void addIfAbsent(String key, String originalPassword) {
        var parousatimh = testo.get(key);
        if (parousatimh != null && !parousatimh.equals(originalPassword)) {
            verifyCollision(originalPassword, parousatimh);
        }
        testo.put(key, originalPassword);

    }

    static void verifyCollision(String previousPassword, String newPassword) { //different byte sequences may be mapped to same strings, such as '?????????'
        var key = repeat(previousPassword);
        var key2 = repeat(newPassword);
        if (Arrays.equals(key[0], key2[0]) && Arrays.equals(key[1], key2[1]) && Arrays.equals(key[2], key2[2]) && Arrays.equals(key[3], key2[3])) {
            System.out.println("key: " + key + " for password: " + newPassword + " existed for " + previousPassword);
            collisions_detected.add(newPassword);
            collisions_detected.add(previousPassword);
        }

    }

    static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()-=+";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        var length = new Random().nextInt(20);
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

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
                var a = (byte) ((i + 1) * (j + 1));
                if (pointer > 0) {
                    arrayOfArrays[i][j] ^= (byte) (a ^ byteSequence[pointer - 1]);
                }
                ++pointer;
            }
        }
        // System.out.println(Arrays.deepToString(arrayOfArrays));

        double a = cos(toLong(arrayOfArrays[0])) + pow(sin(toLong(arrayOfArrays[1]) + toLong(arrayOfArrays[2])), 1);
        double bb = pow(cos(toLong(arrayOfArrays[2]) + toLong(arrayOfArrays[3])), 1) + pow(sin(toLong(arrayOfArrays[0])), 1);

        byte[] bitos = new byte[4];

        for (int i = 0; i < 4; ++i) {
            byte[] right_tetar = new byte[]{arrayOfArrays[i][0], arrayOfArrays[i][1]};
            byte[] left_tetar = new byte[]{arrayOfArrays[i][6], arrayOfArrays[i][7]};
            var common = new byte[]{(byte) (right_tetar[0] ^ left_tetar[0]), (byte) (right_tetar[1] ^ left_tetar[1])};

            //bitos[i] = (byte) (common[0] ^ common[1]);
            bitos[i] = (byte) (arrayOfArrays[i][0] ^ arrayOfArrays[i][1] ^ arrayOfArrays[i][6] ^ arrayOfArrays[i][7]);
        }

        long first = (long) pow(bitos[0], 1) == 0 ? 1 : (long) pow(bitos[0], 1);
        long second = (long) bitos[1] == 0 ? 1 : (long) pow(bitos[1], 2);
        long third = (long) bitos[2] == 0 ? 1 : (long) pow(bitos[2], 3);
        long fourth = (long) (long) bitos[3] == 0 ? 1 : (long) pow(bitos[3], 4);
        long lol2 = first*second*third*fourth;

        //long lol2 = (long) ((long) pow(bitos[0], 1) * (long) pow(bitos[1], 2) * (long) pow(bitos[2], 3) * ((long) pow(bitos[3], 4))); //256
        /*
            Kati endiaferon ginetai me to bitos:
                An trekseis to test me nextLong h nextDouble, vgainoun arketes xiliades diaforetikes times tou long.
                An to trekseis me nextInt, vgainoun molis 256. Yparxei kapoia eggenhs adunamia edw.
                Alla apo th stigmh pou den exoume colissions, to afhnoume pros diereunhsh.
         */

        prime_col.add(lol2);

        byte[] tox = longToBytes(lol2);
        byte[] trig1 = doubleToBytes(a);
        byte[] trig2 = doubleToBytes(bb);


        for (int j = 0; j < 8; ++j) {
            tox[j] = (byte) (tox[j] ^ trig1[j] ^ trig2[j]);
        }

        for (int i = 0; i < 4; ++i) {
            byte position_leftmostbyte_cubed = (byte) (i ^ 3 & 0xFF);
            for (int j = 0; j < 8; ++j) {
                arrayOfArrays[i][j] = (byte) (arrayOfArrays[i][j] ^ tox[j] ^ position_leftmostbyte_cubed);
            }
        }

        // System.out.println(Arrays.deepToString(arrayOfArrays));

        return arrayOfArrays;
    }

    static double toDouble(byte[] bytes) {
        var r = ByteBuffer.wrap(bytes).getDouble();
        return r;
    }

    static long toLong(byte[] bytes) {
        var r = ByteBuffer.wrap(bytes).getLong();
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



}

















