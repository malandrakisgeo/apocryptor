package com.gmalandrakis.key_derivation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.gmalandrakis.key_derivation.KeyDerivation_V2.deduceKey;
import static com.gmalandrakis.utils.Utils.concatAll;
import static com.gmalandrakis.utils.Utils.flattenKey;
import static java.lang.Math.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class KeyDerivation_V2 {

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

   /* public static void main(String[] args) {
        var str = "TestmeTest3575fd"; //DISCREPANCY
        var str2 = "anothertest653";
        System.out.println(Arrays.toString(deduceKey(str.getBytes())));
        System.out.println(Arrays.toString(deduceKey(str2.getBytes())));

        str = "TestmeTest3575fdTestmeTest3575fdTestmeTest3575fdTestmeTest3575fdTestmeTest35%%75fd";
        str2 = "TestmeTest35s75fdTestfmeTest3575343fdTestmeTest3fgd575fdTes8tmeTest357df5fdTestm09eTest93%(575fdd";
        System.out.println(Arrays.toString(deduceKey(str.getBytes())));
        System.out.println(Arrays.toString(deduceKey(str2.getBytes())));

        str = "0123456789ABCDEF0123456789ABCDEFGHIJKLMNOPQRSTUVGHIJKLMNOPQRSTUV";
        str2 = "GHIJKLMNOPQRSTUVGHIJKLMNOPQRSTUV0123456789ABCDEF0123456789ABCDEF";
        System.out.println(Arrays.toString(deduceKey(str.getBytes())));
        System.out.println(Arrays.toString(deduceKey(str2.getBytes())));
        str = "Περπατούσε. Για πρώτη φορά στην ενήλικη ζωή του, ένιωσε την Αμίνα, παιδική του φίλη, να περπατάει δίπλα του. Η Αμίνα δεν υπήρχε πια. Ή μάλλον... υπήρχε και μάλιστα πολύ, για εκείνον. Εφηβεία, φοιτητηλίκι, στρατός, δουλειές, μακροχρόνιες και βραχυχρόνιες σχέσεις, όλες αποτυχημένες,και ποτέ δεν ένιωσε άγγιγμασαν της Αμίνας, ποτέ δεν γεύτηκε γλυκότερο φιλί. Στριφογυρνούσαν στο μυαλό του παλιά λόγια που είχαν πει,υποσχέσεις που σκέπασε ο οδοστρωτήρας χρόνος.";

        System.out.println(Arrays.toString(deduceKey(str.getBytes())));
        System.out.println(Arrays.toString(deduceKey(str2.getBytes())));
        System.out.println(Arrays.toString(deduceKey("123".getBytes())));

        System.out.println(Arrays.toString(getKeyFromInput(s)));
        System.out.println(Arrays.toString(getKeyFromInput(new byte[]{0, 0, 0, 0})));
        System.out.println(Arrays.toString(getKeyFromInput(new byte[]{0, 0, 0, 1})));
        System.out.println((getKeyFromInput(new byte[]{0, 0, 0, 0}).length));
        System.out.println((getKeyFromInput(new byte[]{0, 0, 0, 1}).length));

    }
*/

    /*public static byte[] getKeyFromInput(byte[] input) {
        var keyAsMultiArray = deduceKeyInternal(input);
        return flattenKey(keyAsMultiArray);
    }*/


    static byte xoredLength(int originalLength) {
        assert (originalLength > 0);
        if (originalLength <= 127) {
            return (byte) originalLength;
        }
        return originalLength % 128 != 0 ? (byte) (originalLength % 128) : 127;
    }
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
/*
    Αν το κλειδι ειναι μεγαλυτερο απο 32 βυτες,
    1. Τα πρωτα 32 βυτες χρησιμοποιουνται για message digest A
    Αποθηκευουμε
    2. Τα υπολοιπα, για καθε 32αδα Ν
        α. Αντιμετωπιζουμε καθε 8 βυτες ως signed long Τ
        β. Πολλαπλασιαζουμε το Τ XOR N επι cos(Ν)
        γ. xor το αποτελεσμα με το Α, και αντικατασταση του Α με το αποτελεσμα
 */
        if (originallength > 32) {
            int parts_of_32_bytes = originallength / 32;
            byteSequence = deduceKey(Arrays.copyOfRange(oldByteSequence, 0, 32));
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
                long bbrb = (toSignedLong(Arrays.copyOfRange(oldByteSequence, i * 32 + 8, i * 32 + 16)) + 4) * (b);
                var bt2 = longToBytes(bbrb);
                var bt3 = longToBytes((toSignedLong(Arrays.copyOfRange(oldByteSequence, i * 32 + 16, i * 32 + 24)) + 9) * (c));
                var bt4 = longToBytes((toSignedLong(Arrays.copyOfRange(oldByteSequence, i * 32 + 24, i * 32 + 32)) + 16) * (d));
                byte[] by = concatAll(bt1, bt2, bt3, bt4);
                for (int j = 0; j < 32; j++) {
                    byteSequence[j] ^= by[j];
                }

            }
            for (int j = 0; j < originallength % 32; j++) {
                byteSequence[j] ^= oldByteSequence[32 * parts_of_32_bytes + j];
                byteSequence[j] ^= (byte) (j & 0xFF);
            }
           /* for (int i = 0; i < 32; i++) {
                for (int j = 0; j < parts_of_32_bytes; j++) {
                    byteSequence[i] ^= oldByteSequence[32 * j + i];
                    byteSequence[i] ^= (byte) (j & 0xFF);
                }
            }

            for (int j = 0; j < originallength % 32; j++) {
                byteSequence[j] ^= oldByteSequence[32 * parts_of_32_bytes + j];
                byteSequence[j] ^= (byte) (j & 0xFF);
            }*/

        }

        byte xored = xoredLength(originallength);

        byte[][] arrayOfArrays = new byte[4][8];

        int pointer = 0; //pointer: 8*i + j
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


        byte[] trigonometric_1 = doubleToBytes(round(cos(toSignedLong(arrayOfArrays[0])), 12) + round(sin(toSignedLong(arrayOfArrays[1]) + toSignedLong(arrayOfArrays[2])), 12));
        byte[] trigonometric_2 = doubleToBytes(round(cos(toSignedLong(arrayOfArrays[1]) + toSignedLong(arrayOfArrays[2])), 12) + round(sin(toSignedLong(arrayOfArrays[3])), 12));

        byte[] bitos = new byte[4];

        for (int i = 0; i < 4; ++i) {
            bitos[i] = (byte) (arrayOfArrays[i][0] ^ arrayOfArrays[i][1] ^ arrayOfArrays[i][6] ^ arrayOfArrays[i][7]);
        }

        long first = (long) pow(bitos[0], 1) == 0 ? 1 : (long) pow(bitos[0], 1);
        long second = (long) bitos[1] == 0 ? 2 : (long) pow(bitos[1], 2);
        long third = (long) bitos[2] == 0 ? 4 : (long) pow(bitos[2], 3);
        long fourth = (long) bitos[3] == 0 ? 27 : (long) pow(bitos[3], 4);
        long ginomeno = first * second * third * fourth;


        byte[] tox = longToBytes(ginomeno);


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


    static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        if (places == 0) {
            return value;
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    static long toSignedLong(byte[] bytes) {
        assert (bytes.length <= 8);
        //We originally skipped the LITTLE ENDIAN
        byte[] bb = new byte[8];

        try {
            for (int i = 0; i < bytes.length; i++) {
                bb[i] = bytes[i];
            }
            for (int j = bytes.length; j < 8; j++) {
                bb[j] = 0;
            }

            // return ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).put(bytes).getLong(0);
            return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
        } catch (Exception e) {
            System.out.println("lol");
            throw new RuntimeException(e);
        }
    }

    static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN);
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
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putDouble(x);
        return buffer.array();
    }


}

















