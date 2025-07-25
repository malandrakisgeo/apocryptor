package com.gmalandrakis.chaffing;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import static com.gmalandrakis.utils.Utils.adjustChaffingPositions;

/**
 * @author George Malandrakis (malandrakisgeo@gmail.com)
 */
public class KeyBasedChaffing {


    public static void insert(File sourceFilename, File targetFilename, byte[] chaff, long[] positions) throws Throwable {
        RandomAccessFile source = new RandomAccessFile(sourceFilename, "rw");
        RandomAccessFile dest = new RandomAccessFile(targetFilename, "rw");
        FileChannel sourceChannel = source.getChannel();
        FileChannel targetChannel = dest.getChannel();
        if (source.length() == 0) {
            return;
        }
        long totalSum = Arrays.stream(positions).sum();
        if (totalSum >= source.length()) {
            positions = adjustChaffingPositions(positions, source.length());
        }
        long curPos = 0;
        long posSum = 0;
        int step = chaff.length / positions.length;
        int l = 0;
        int n = sourceFilename.equals(targetFilename) ? step : 0;

        for (int i = 0; i < chaff.length; i += step) {
            curPos = (long) positions[l];
            var curCont = defineContent(chaff, positions, l);
            source.getChannel().transferTo(posSum, curPos, targetChannel);
            posSum += curPos + n;
            source.seek(posSum);
            dest.write(curCont);
            ++l;
        }
        source.getChannel().transferTo(posSum, source.length(), targetChannel);
        sourceChannel.close();
        targetChannel.close();
    }


    static byte[] defineContent(byte[] chaff, long[] positions, int currentPos) {
        if (positions.length == chaff.length) {
            return new byte[]{chaff[currentPos]};
        }
        assert (chaff.length > positions.length);
        int div = chaff.length / positions.length;
        return Arrays.copyOfRange(chaff, currentPos * div, (currentPos + 1) * div);
    }
}
