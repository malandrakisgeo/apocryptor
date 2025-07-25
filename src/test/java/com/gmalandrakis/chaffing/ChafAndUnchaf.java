package com.gmalandrakis.chaffing;

import com.gmalandrakis.APOCRYPTOR;
import com.gmalandrakis.key_derivation.KeyDerivation;
import com.gmalandrakis.utils.Utils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.gmalandrakis.chaffing.KeyBasedChaffing.insert;
import static com.gmalandrakis.chaffing.KeyBasedWinnowing.winnow;


public class ChafAndUnchaf {
    @Test
    public void genericTest() throws Throwable {
        var key = "123";
        byte[] content = new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F'};
        var byteArray = KeyDerivation.deduceKey(key.getBytes());
        var positions = Utils.chaffingPositions(byteArray);
        File fil = Paths.get(APOCRYPTOR.class.getClassLoader().getResource("test_chaf").toURI()).toFile();
        File fil2 = Paths.get(APOCRYPTOR.class.getClassLoader().getResource("test_chaf2").toURI()).toFile();
        File fil3 = Paths.get(APOCRYPTOR.class.getClassLoader().getResource("test_chaf3").toURI()).toFile();

        insert(fil, fil2, content, positions);
        var result = winnow(fil2, fil3,  positions);

        if(!Arrays.equals(result, content)){
            throw new RuntimeException("failed");
        }

    }



}
