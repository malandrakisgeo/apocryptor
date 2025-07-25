package com.gmalandrakis.key_derivation.archive;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static java.lang.Math.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class KeyDerivationOld {

    /*
    1. Σπαμε τον δοθεντα κωδικό σε τμήματα των 256bits (32 bytes) τα οποία κάνουμε xor μεταξύ τους.
Αν ο κωδικός είναι μικρότερος από 256 μπιτσ, βάζουμε όσα χρειάζεται ως padding (μηδενισμένα).
2. Σπάμε το αποτέλεσμα σε 4 doubles (8 bytes το καθενα):
    Q,W,E,R
Για την ακριβεια τα σπαμε σε οχτω ομαδες των τεσσαρων βυτες, που κατα περισταση αντιμετωπιζουμε ως double
ή long.

3) Στη θεση καθε μεμονωμένου byte των Q,W,E,R,
αποθηκευουμε τον εαυτό του xor με το μήκος του κωδικού, καθώς και με το αμέσως
προηγούμενο αποθηκευμένο byte (το πρώτο byte του Q εξαιρείται από το δεύτερο σκέλος)
4. Συνάγουμε δυο doubles (ολα τα παραπάνω αντιμετωπίζονται ως δουβλες):
    1: cos (Q) + SIN (R+E) στρογγυλοποιημενο στους 12 δεκαδικούς, unsigned (δεν μηδενίζεται -μεταξυ 0 και 2 παντα)
    2: COS (W+R)) + sin (Q) στρογγυλοποιημενο στους 12
5. Για καθε ενα απο τα νέα Q,W,E,R
παίρνουμε τα δυο πρώτα και τα δυο τελευταία bytes, και σχηματίζουμε ένα καινούριο
κάνοντας τα xor μεταξύ τους ως εξής: πρώτο με έβδομο, δεύτερο και όγδοο
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
        var res = repeat(new byte[]{0});
        var ress = repeat(new byte[]{1});
        var resss = repeat(new byte[]{0, 0});
        var resssss = new byte[68];
        resssss[32] = 1;
        resssss[33] = 2;
        resssss[64] = 5;
        resssss[65] = 5;
        var p = repeat(resssss);

        System.out.println(p);
        var threadPool = Executors.newCachedThreadPool();


        for (int i = 0; i < 50; ++i) {
            threadPool.execute(() -> concurrently());
        }
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        recursionTest();
        System.out.println("Total collisions: " + collisions_detected.size());
        System.out.println("Total different magic nums: " + prime_col.size());
        System.out.println("Total keys calculated: " + testo.size());

    }

    static void recursionTest() {
        var randomPassword = getRandomString();
        var result = repeat(randomPassword.getBytes());
        addIfAbsent(keyToString(result), randomPassword);

        for (int i = 0; i < 20000; i++) {
            var newResult = repeat(arrayFlattening(result));
            addIfAbsent(keyToString(newResult), keyToString(result));
            result = newResult;
        }

    }

    static void concurrently() {
        for (int i = 0; i < 2500000; ++i) {
            var randomPassword = getRandomString();

            var key = repeat((randomPassword.getBytes()));
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextInt();

            var key = repeat(String.valueOf(randomPassword).getBytes());
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }

        for (int i = 0; i < 150000; ++i) {
            var randomPassword = new Random().nextDouble();

            var key = repeat(String.valueOf(randomPassword).getBytes());
            String s = keyToString(key);
            addIfAbsent(s, String.valueOf(randomPassword));
        }
    }

    static void addIfAbsent(String key, String originalPassword) {
        var parousatimh = testo.get(key);
        if (parousatimh != null && !parousatimh.equals(originalPassword)) {
            verifyCollision(originalPassword, parousatimh);
        }
        testo.put(key, originalPassword);

    }

    static void verifyCollision(String previousPassword, String newPassword) { //different byte sequences may be mapped to same strings, such as '?????????'
        var key = repeat(previousPassword.getBytes());
        var key2 = repeat(newPassword.getBytes());
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
        //var length = 255;
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public static byte[] getKeyFromInput(byte[] input){
        var res = repeat(input);

        return flatten(res);
    }

    public static byte[] flatten(byte[][] arrayOfArrays){
        var totalLength = 0;
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

    public static byte[][] repeat(byte[] oldByteSequence) {
        var originallength = oldByteSequence.length;
        var byteSequence = new byte[32];


        if (originallength <= 32) {
            var difference = 32 - originallength;
            for (int i = 0; i < 32; ++i) {
                if (i < originallength) {
                    byteSequence[i] = oldByteSequence[i];
                } else {
                    byteSequence[i] = '0';
                }
            }
        }

        if (originallength > 32) { //65
            int parts_of_32_bytes = originallength % 32;
            outer: for (int j = 0; j < 32; j++) { //0,32,64
                byte total_xor = '0';
                for (int i = 0; i < parts_of_32_bytes ; i++) {
                    if(j + (i*32) >= originallength){
                        break;
                    }
                    var a = oldByteSequence[j + (i*32)];
                    total_xor ^= a;
                }
                byteSequence[j] ^= total_xor;

            }
            for (int i = 0; i < originallength - parts_of_32_bytes*32; i++) {
                byteSequence[i] ^= oldByteSequence[i + parts_of_32_bytes * 32];
            }
          //  System.out.println(byteSequence);

            //TODO: check if it is really what we think it is
        }


        // var byteSequence = str.getBytes();
        //  System.out.println(Arrays.toString(byteSequence));

        byte[][] arrayOfArrays = new byte[4][8];

        int pointer = 0; //pointer: 8*i + j
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 8; ++j) {
                var a = (byte) ((i + 1) * (j + 1));
                arrayOfArrays[i][j] = (byte) (a ^ byteSequence[pointer] ^ ((char) originallength));
                if (pointer > 0) {
                    arrayOfArrays[i][j] ^= (byte) (byteSequence[pointer - 1]); //arxika a ^
                }
                ++pointer;
            }
        }
        // System.out.println(Arrays.deepToString(arrayOfArrays));


        byte[] bitos = new byte[4];

        for (int i = 0; i < 4; ++i) {
            bitos[i] = (byte) (arrayOfArrays[i][0] ^ arrayOfArrays[i][1] ^ arrayOfArrays[i][6] ^ arrayOfArrays[i][7]);
        }

        long first = (long) pow(bitos[0], 1) == 0 ? 1 : (long) pow(bitos[0], 1);
        long second = (long) bitos[1] == 0 ? 1 : (long) pow(bitos[1], 2);
        long third = (long) bitos[2] == 0 ? 1 : (long) pow(bitos[2], 3);
        long fourth = (long) bitos[3] == 0 ? 1 : (long) pow(bitos[3], 4);
        long lol2 = first * second * third * fourth;


        prime_col.add(lol2);

        byte[] tox = longToBytes(lol2);
        byte[] trigonometric_1 = doubleToBytes(round(cos(toLong(arrayOfArrays[0])), 12) + round(sin(toLong(arrayOfArrays[1]) + toLong(arrayOfArrays[2])), 12));
        byte[] trigonometric_2 = doubleToBytes(round(cos(toLong(arrayOfArrays[2]) + toLong(arrayOfArrays[3])), 12) + round(sin(toLong(arrayOfArrays[0])), 12));


        for (int j = 0; j < 8; ++j) {
            tox[j] = (byte) (tox[j] ^ trigonometric_1[j] ^ trigonometric_2[j]);
        }

        for (int i = 0; i < 4; ++i) {
            //byte position_leftmostbyte_cubed = (byte) (i ^ 3 & 0xFF);
            byte position_leftmostbyte_cubed = (byte) ((i * i * i) & 0xFF);
            for (int j = 0; j < 8; ++j) {
                arrayOfArrays[i][j] = (byte) (arrayOfArrays[i][j] ^ tox[j] ^ position_leftmostbyte_cubed);
            }
        }

        // System.out.println(Arrays.deepToString(arrayOfArrays));

        return arrayOfArrays;
    }

    static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        if (places == 0) {
            return value;
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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

    static byte[] arrayFlattening(byte[][] x) {
        ArrayList<Byte> arrayList = new ArrayList<Byte>();
        var bytes = new byte[32];
        Arrays.stream(x)
                .forEach(a -> {
                    final List<Byte> list = new ArrayList<>();
                    for (byte b : a) {
                        list.add(b);
                    }
                    arrayList.addAll(list);
                });
        for (int i = 0; i < 32; i++) {
            bytes[i] = arrayList.get(i).byteValue();
        }
        return bytes;
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

















