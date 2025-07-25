package com.gmalandrakis.key_derivation.archive;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class KeyDerivationOldOfficial872025 {

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

    public static void main(String[] args){
        var s = new byte[]{'0', 'a', 'h','r'};
        //var str = "TestmeTest3575fd"; //DISCREPANCY
       // var str = "ZBUTSuts432uts";
        var str = "Test123";
        System.out.println(getKeyFromInput(str.getBytes()));
    }


    public static byte[] getKeyFromInput(byte[] input) {
        var keyAsMultiArray = deduceKey(input);
        return flatten(keyAsMultiArray);
    }

    public static byte[] flatten(byte[][] arrayOfArrays) {
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
/*

char define_xored_length(int password_length){

    if(password_length < 127){
        return password_length;
    }
    char xored_length = (password_length) % 128 != 0 ?  (password_length) % 128 :  (password_length) % 127;
    return xored_length;
}
 */
    /**
     * Takes an arbitrary byte array and returns 32-byte (256bit) key as byte[4][8].
     */
    public static byte[][] deduceKey(byte[] oldByteSequence) {
        var originallength = oldByteSequence.length; //TODO: Handle if 255
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

        if (originallength > 32) {
            int parts_of_32_bytes = originallength % 32;
            outer:
            for (int j = 0; j < 32; j++) { //0,32,64
                byte total_xor = '0';
                for (int i = 0; i < parts_of_32_bytes; i++) {
                    if (j + (i * 32) >= originallength) {
                        break;
                    }
                    var a = oldByteSequence[j + (i * 32)];
                    total_xor ^= a;
                }
                byteSequence[j] ^= total_xor;

            }
            for (int i = 0; i < originallength - parts_of_32_bytes * 32; i++) {
                byteSequence[i] ^= oldByteSequence[i + parts_of_32_bytes * 32];
            }

            //TODO: check if it is really what we think it is
        }


        byte[][] arrayOfArrays = new byte[4][8];

        int pointer = 0; //pointer: 8*i + j
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 8; ++j) {
                var a = (byte) ((i + 1) * (j + 1));
                arrayOfArrays[i][j] = (byte) (a ^ byteSequence[pointer] ^ ((byte) originallength));
                if (pointer > 0) {
                    arrayOfArrays[i][j] ^= (byte) (byteSequence[pointer - 1]); //arxika a ^
                }
                ++pointer;
            }
        }
        var lng = toLong(arrayOfArrays[0]);
        var dbl = cos(lng);
        byte[] r1 = doubleToBytes(dbl);

        byte[] trigonometric_1 = doubleToBytes(round(cos(toLong(arrayOfArrays[0])), 12) + round(sin(toLong(arrayOfArrays[1]) + toLong(arrayOfArrays[2])), 12));
        byte[] trigonometric_2 = doubleToBytes(round(cos(toLong(arrayOfArrays[1]) + toLong(arrayOfArrays[2])), 12) + round(sin(toLong(arrayOfArrays[3])), 12));

        byte[] bitos = new byte[4];

        for (int i = 0; i < 4; ++i) {
            bitos[i] = (byte) (arrayOfArrays[i][0] ^ arrayOfArrays[i][1] ^ arrayOfArrays[i][6] ^ arrayOfArrays[i][7]);
        }

        long first = (long) pow(bitos[0], 1) == 0 ? 1 : (long) pow(bitos[0], 1);
        long second = (long) bitos[1] == 0 ? 1 : (long) pow(bitos[1], 2);
        long third = (long) bitos[2] == 0 ? 1 : (long) pow(bitos[2], 3);
        long fourth = (long) bitos[3] == 0 ? 1 : (long) pow(bitos[3], 4);
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

    static long toLong(byte[] bytes) {
        //We originall skipped the LITTLE ENDIAN
        var r = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
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



}

















