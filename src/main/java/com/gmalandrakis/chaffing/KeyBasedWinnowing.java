package com.gmalandrakis.chaffing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

import static com.gmalandrakis.utils.Utils.adjustChaffingPositions;

public class KeyBasedWinnowing {

    public static byte[] winnow(File sourceFilename, File targetFilename, long[] positions) throws Throwable {
        RandomAccessFile source = new RandomAccessFile(sourceFilename, "rw");
        RandomAccessFile dest = new RandomAccessFile(targetFilename, "rw");
        FileChannel sourceChannel = source.getChannel();
        FileChannel targetChannel = dest.getChannel();
        long totalSum = Arrays.stream(positions).sum();
        System.out.println(source.length());
        if (totalSum >= source.length() - 32) {
            positions = adjustChaffingPositions(positions, source.length() - 32);
        }
        long curPos = 0;
        long posSum = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(32);
        WritableByteChannel retriever = Channels.newChannel(outputStream);
        int step = 32 / positions.length;
        int l = 0;
        int n = sourceFilename.equals(targetFilename) ? step : 0;

        for (int i = 0; i < 32; i += step) {
            curPos = positions[l];
            source.getChannel().transferTo(posSum, curPos, targetChannel);
            source.getChannel().transferTo(posSum + curPos, step, retriever);
            posSum += curPos + step;
            ++l;
        }
        source.getChannel().transferTo(posSum, source.length(), targetChannel);
        sourceChannel.close();
        targetChannel.close();


        return outputStream.toByteArray();
    }




}
