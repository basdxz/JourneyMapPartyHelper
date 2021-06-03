package com.github.basdxz.journeymappartyhelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//TODO: enum to class with private constructor
public enum Util {
    ;
    public static final int SHORT_MASK = 0xFFFF;
    public static final int BYTE_MASK = 0xFF;
    public static final int NIBBLE_MASK = 0xF;

    private static void checkNibble(byte input){
        //Converts nibbles to integers and unsigns them (so (byte)-127 becomes (int)129 etc)
        //Compares them with 15, which is the max size for a nibble
        if ((input & BYTE_MASK) > NIBBLE_MASK){
            throw new IllegalArgumentException("Input not nibble!");
        }
    }

    public static byte byteFromNibbles(byte highNibble, byte lowNibble) {
        //Sanity check, don't work with improper nibbles.
        checkNibble(highNibble);
        checkNibble(lowNibble);
        //Shifts the high nibble by 4 bits
        //Basically: 0000xxxx -> xxxx0000
        //Where 0 is expected to be nothing and x is the important bits
        //noinspection NumericCastThatLosesPrecision
        return (byte) (highNibble << 4 + lowNibble);
    }

    public static byte[] nibblesFromByte(byte inputByte) {
        byte[] outputNibbles = new byte[2];
        //Shifts the lower four bits off then masks off the top bits, leaving the higher 4 bits.
        outputNibbles[0] = (byte) (inputByte >> 4 & NIBBLE_MASK);
        //Masks off the high 4 bits, leaving only the lower 4 bits.
        outputNibbles[1] = (byte) (inputByte & NIBBLE_MASK);
        return outputNibbles;
    }

    public static short[] bytesToShorts(byte... bytes) {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("Input can't be zero in length!");
        }
        if ((bytes.length & 1) == 1){
            throw new IllegalArgumentException("Number of bytes must be even!");
        }

        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static byte[] shortsToBytes(short... shorts) {
        byte[] bytes = new byte[(shorts.length << 1)];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(shorts);
        return bytes;
    }

    public static byte[] concatenateByteArrays(byte[]... byteArrays) {
        if (byteArrays == null || byteArrays.length < 1) {
            throw new IllegalArgumentException("Input must contain at least one byte array!");
        }

        byte[] outputBytes;
        if (byteArrays.length == 1) {
            outputBytes = byteArrays[0];
        } else {
            //Calculate length of byte array
            int byteCount = 0;
            for (byte[] byteArray : byteArrays) {
                byteCount += byteArray.length;
            }

            //Init the byte array and buffer
            outputBytes = new byte[byteCount];
            ByteBuffer payloadBuffer = ByteBuffer.wrap(outputBytes);

            //Populate the array through the buffer
            for (byte[] byteArray : byteArrays) {
                payloadBuffer.put(byteArray);
            }
        }
        return outputBytes;
    }

    private static byte maskBit(int targetBit) {
        if (targetBit < 0 || targetBit > 7) {
            throw new IllegalArgumentException("targetBit must be 0 to 7!");
        }
        //noinspection NumericCastThatLosesPrecision
        return (byte) (1 << targetBit);
    }

    public static byte flipBit(byte inByte, int targetBit) {
        //noinspection NumericCastThatLosesPrecision
        return (byte) (inByte ^ maskBit(targetBit));
    }

    public static byte setBitHigh(byte inByte, int targetBit) {
        //noinspection NumericCastThatLosesPrecision
        return (byte) (inByte | maskBit(targetBit));
    }

    public static byte setBitLow(byte inByte, int targetBit) {
        //noinspection NumericCastThatLosesPrecision
        return (byte) (inByte & ~maskBit(targetBit));
    }

    public static boolean readBit(byte inByte, int targetBit) {
        return (inByte & maskBit(targetBit)) != 0;
    }

    public static String byteArrayToUTF16String(byte... bytes){
        if (bytes == null){
            throw new IllegalArgumentException("Bytes can't be null!");
        }
        if (bytes.length == 0){
            throw new IllegalArgumentException("Bytes can't be zero in length!");
        }
        return new String(bytes, StandardCharsets.UTF_16BE);
    }

    public static byte[] aUTF16StringToByteArray(String inputString){
        if (inputString == null){
            throw new IllegalArgumentException("String can't be null!");
        }
        if (inputString.isEmpty()){
            throw new IllegalArgumentException("String can't be empty!");
        }
        return inputString.getBytes(StandardCharsets.UTF_16BE);
    }

    public static void logToForgeLog(String input) {
        if (Reference.DEBUG) {
            JourneymapPartyHelper.LOGGER.info(input);
        }
    }

    public static void dumpByteArrayToForgeLog(byte... bytes) {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("Input bytes cannot be zero in length.");
        }
        logToForgeLog("-=Byte Dump Start=-");
        for (byte b : bytes) {
            logToForgeLog(Integer.toBinaryString(b & BYTE_MASK | BYTE_MASK + 1).substring(1));
        }
        logToForgeLog("--=Byte Dump End=--");
    }

    public static List<Integer> printIndex(String sourceString, String targetString) {
        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < sourceString.length() - targetString.length() + 1; i++) {
            if (sourceString.startsWith(targetString, i)) {
                indexList.add(i);
            }
        }
        return indexList;
    }
}
