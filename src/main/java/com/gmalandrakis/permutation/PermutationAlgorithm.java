package com.gmalandrakis.permutation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.gmalandrakis.key_derivation.KeyDerivation.deduceKey;


public class PermutationAlgorithm {

    /*
    Για τα πρώτα 32 blocks του αρχείου, αντιμετωπίζουμε τα βυτες του κλειδιου KEY_1 ως ιντεγερσ (0-255 ή -127 - 128 αντιστοιχα).
Κάθε αριθμός αντιστοιχεί σε ένα block, ο πρώτος ιντ στο πρώτο μπλοκ, κ.ο.κ.
Ταξινομούμε έπειτα από το μικρότερο στο μεγαλύτερο -και αυτό είναι και το permutation που κάνουμε στα κρυπτογραφημένα blocks. Αν το κλειδί περιέχει δυο ιδιους ακεραίους Α,Β στις θέσεις π.χ. 10 και 13, τα αντίστοιχα blocks γινονται χιαστί (το 13 γίνεται 10ο, το 10ο γίνεται 13ο). Αν περιέχει τρεις ίδιους ακεραίους, έστω Α-Β-Γ, πάει Γ-Α-Β.
Αν περιέχει τέσσερις, έστω Α-Β-Γ-Δ πάει Γ-Α-Δ-Β. Αν περιέχει πέντε, απορρίπτεται και συνάγουμε νέο κλειδί
από το message digest του.

     */
    public static byte[] permuteArrayByKey(byte[] inputArray, byte[] key) {
        List<Integer> positions = new ArrayList<>();
        List<Byte> unsortedKeyBytes = new ArrayList<>();
        byte[] outputArray = new byte[32];
        var cinco = 0;
        for (byte b : key) {
            var i = (int) b;
            cinco = positions.contains(i) ? cinco + 1 : 0;
            if (cinco == 5) {
                return permuteArrayByKey(inputArray, deduceKey(key)); //do for recalculated key if five consecutive numbers are the same
            }
            positions.add(i);
            unsortedKeyBytes.add(b);
        }

        positions = positions.stream().sorted().toList();

        var outputArrayPos = 0;
        for (int i = 0; i < positions.size(); i++) {

            var consecutive = positions.lastIndexOf(positions.get(i)) - positions.indexOf(positions.get(i));
            var index = unsortedKeyBytes.indexOf(positions.get(i).byteValue());
            if (consecutive == 0) {
                outputArray[outputArrayPos] = inputArray[index];
            }
            if (consecutive == 1) {
                outputArray[outputArrayPos] = inputArray[index + 1];
                outputArray[++outputArrayPos] = inputArray[index];
                ++i;
            }
            if (consecutive == 2) {
                outputArray[outputArrayPos] = inputArray[index + 2];
                outputArray[++outputArrayPos] = inputArray[index];
                outputArray[++outputArrayPos] = inputArray[index + 1];
                i += 2;
            }
            if (consecutive == 3) {
                outputArray[outputArrayPos] = inputArray[index + 2];
                outputArray[++outputArrayPos] = inputArray[index];
                outputArray[++outputArrayPos] = inputArray[index + 3];
                outputArray[++outputArrayPos] = inputArray[index + 1];
                i += 3;
            }

            ++outputArrayPos;
        }
        return outputArray;
    }


    public static byte[] unpermuteArrayByKey(byte[] permutedArray, byte[] key) {
      /*
        Εχουμε ενα κευ με 32 βυτες, και ενα ακατωμενο αρραυ 32 στοιχειων.
        Ταξινομουμε τα βυτες απο το μικροτερο στο μεγαλυτερο, οπως και πριν.
        Για το μικροτερο βυτε, εστω α, βρισκουμε το ινδεξ του στο κλειδι, εστω α'.
        Το πρωτο στοιχειο το ανακατωμενου αρραυ παει στο α'.
        κοκ

       */
        List<Byte> positions = new ArrayList<>();
        List<Byte> unsortedKeyBytes = new ArrayList<>();
        byte[] outputArray = new byte[32];
        var cinco = 0;
        for (byte b : key) {
            var i = (int) b;
            cinco = positions.contains(i) ? cinco + 1 : 0;
            if (cinco == 4) {
                return unpermuteArrayByKey(permutedArray, deduceKey(key)); //do for recalculated key if five consecutive numbers are the same
            }
            positions.add(b);
            unsortedKeyBytes.add(b);
        }

        positions = positions.stream().sorted().toList();
        for (int i = 0; i < positions.size(); i++) {
            var consecutive = positions.lastIndexOf(positions.get(i)) - positions.indexOf(positions.get(i));

            var actualIndex = unsortedKeyBytes.indexOf(positions.get(i));

            if (consecutive == 0) {
                outputArray[actualIndex] = permutedArray[i];
            }
            if (consecutive == 1) {
                outputArray[actualIndex + 1] = permutedArray[i];
                outputArray[actualIndex] = permutedArray[++i];
            }
            if (consecutive == 2) {
                outputArray[actualIndex + 2] = permutedArray[i];
                outputArray[actualIndex] = permutedArray[++i];
                outputArray[actualIndex + 1] = permutedArray[++i];
            }
            if (consecutive == 3) {
                outputArray[actualIndex + 2] = permutedArray[i];
                outputArray[actualIndex] = permutedArray[++i];
                outputArray[actualIndex + 3] = permutedArray[++i];
                outputArray[actualIndex + 1] = permutedArray[++i];
            }
        }

        return outputArray;
    }


    public static byte[][] permuteArraysByKey(byte[][] inputArray, byte[] key) {
        //TODO: TI KANOUME AN TO INPUT ARRAY EXEI TA TELEUTAIA N KENA??
        List<Byte> positions = new ArrayList<>();
        List<Byte> unsortedKeyBytes = new ArrayList<>();
        byte[][] outputArray = new byte[32][];
        var cinco = 0;
        for (byte b : key) {
            var i = (int) b;
            cinco = positions.contains(b) ? cinco + 1 : 0;
            if (cinco == 3) {
                return permuteArraysByKey(inputArray, deduceKey(key)); //do for recalculated key if five consecutive numbers are the same
            }
            positions.add(b);
            unsortedKeyBytes.add(b);
        }

        positions = positions.stream().sorted().toList();
        var outputArrayPos = 0;
        for (int i = 0; i < positions.size(); i++) {

            var index = unsortedKeyBytes.indexOf(positions.get(i));
          /*  if (inputArray[index].length == 0) {
                continue;
            }*/
            var consecutive = positions.lastIndexOf(positions.get(i)) - positions.indexOf(positions.get(i));

            if (consecutive == 0) {
                outputArray[outputArrayPos] = inputArray[index];
                ++outputArrayPos;
            }
            if (consecutive == 1) {
                outputArray[outputArrayPos] = inputArray[unsortedKeyBytes.lastIndexOf(positions.get(i))];
                outputArray[++outputArrayPos] = inputArray[index];
                ++outputArrayPos;
                ++i;
            }
            if (consecutive == 2) {
                var indexes = findIndexesOfElement(unsortedKeyBytes.stream().toList(), positions.get(i));
                outputArray[outputArrayPos++] = inputArray[indexes.getLast()];
                outputArray[outputArrayPos++] = inputArray[indexes.getFirst()];
                outputArray[outputArrayPos++] = inputArray[indexes.get(1)];
                i += 2;
            }
            if (consecutive == 3) {
                var indexes = findIndexesOfElement(unsortedKeyBytes.stream().toList(), positions.get(i));

                outputArray[outputArrayPos] = inputArray[indexes.get(2)];
                outputArray[++outputArrayPos] = inputArray[indexes.get(0)];
                outputArray[++outputArrayPos] = inputArray[indexes.get(3)];
                outputArray[++outputArrayPos] = inputArray[indexes.get(1)];
                ++outputArrayPos;
                i += 3;
            }


        }
        return outputArray;
    }


    public static byte[][] unpermuteArraysByKey(byte[][] permutedArray, byte[] key) {
        if (permutedArray.length != 32 || key.length != 32) {
            throw new RuntimeException("Operation not supported");
        }
        List<Byte> positions = new ArrayList<>();
        List<Byte> unsortedKeyBytes = new ArrayList<>();
        byte[][] outputArray = new byte[32][];
        var cinco = 0;
        for (byte b : key) {
            var i = (int) b;
            cinco = positions.contains(b) ? cinco + 1 : 0;
            if (cinco == 3) {
                return unpermuteArraysByKey(permutedArray, deduceKey(key)); //do for recalculated key if five consecutive numbers are the same
            }
            positions.add(b);
            unsortedKeyBytes.add(b);
        }

        positions = positions.stream().sorted().toList();
        for (int i = 0; i < positions.size(); i++) {
          /*  if (permutedArray[i] == null || permutedArray[i].length == 0) {
                continue;
            }*/
            var u = positions.get(i);
            var consecutive = positions.lastIndexOf(u) - positions.indexOf(positions.get(i));
            var actualIndex = unsortedKeyBytes.indexOf(u);

            if (consecutive == 0) {
                outputArray[actualIndex] = permutedArray[i];
            }

            if (consecutive == 1) {
                outputArray[unsortedKeyBytes.lastIndexOf(positions.get(i))] = permutedArray[i];
                outputArray[actualIndex] = permutedArray[++i];
            }
            if (consecutive == 2) {
                var indexes = findIndexesOfElement(unsortedKeyBytes.stream().toList(), positions.get(i)); ///POSITIONS edw, oxi unsortedKeyBytes!!!!!!!!!!1
                outputArray[indexes.getLast()] = permutedArray[i];
                outputArray[indexes.getFirst()] = permutedArray[++i];
                outputArray[indexes.get(1)] = permutedArray[++i];
                //  i += 2;
            }
            if (consecutive == 3) {
                var indexes = findIndexesOfElement(positions.stream().toList(), positions.get(i));
                outputArray[actualIndex + 2] = permutedArray[indexes.getFirst()];
                outputArray[actualIndex] = permutedArray[indexes.get(1)];
                outputArray[actualIndex + 3] = permutedArray[indexes.get(3)];
                outputArray[actualIndex + 1] = permutedArray[indexes.get(2)];
                //  i += 3;
            }
        }

        for (int i = 0; i < outputArray.length; i++) {
            if (outputArray[i] == null) { //TODO: Make sure this never happens in the first place
                outputArray[i] = new byte[]{};
            }
        }

        return outputArray;
    }

    private static List<Integer> findIndexesOfElement(List<Byte> list, byte element) {
        List<Integer> r = new ArrayList<>();
        AtomicInteger i = new AtomicInteger();
        list.forEach(e -> {
            if (e == element) {
                r.add(i.get());
            }
            i.incrementAndGet();
        });
        return r;
    }
}
