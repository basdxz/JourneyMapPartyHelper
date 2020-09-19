package com.github.basdxz.journeymappartyhelper;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public enum Utils {
    ;

    public static short[] bytesToShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static byte[] shortsToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
        return bytes;
    }

    public static byte byteFromNibbles(byte highNibble, byte lowNibble) {
        //Sanity check, don't work with improper nibbles.
        //Converts nibbles to integers and unsigns them (so (byte)-127 becomes (int)129 etc)
        //Compares them with 15, which is the max size for a nibble
        if ((highNibble & 0xFF) > 15 || (lowNibble & 0xFF) > 15) {
            throw new RuntimeException("Inputs must be nibbles!");
        }
        //Shifts the high nibble by 4 bits
        //Basically: 0000xxxx -> xxxx0000
        //Where 0 is expected to be nothing and x is the important bits
        highNibble = (byte) (highNibble << 4);
        return (byte) (highNibble + lowNibble);
    }

    public static byte[] nibblesFromByte(byte Byte) {
        byte[] nibbles = new byte[2];
        //Shifts the lower four bits off then masks off the top bits, leaving the higher 4 bits.
        nibbles[0] = (byte) ((Byte >> 4) & 0xF);
        //Masks off the high 4 bits, leaving only the lower 4 bits.
        nibbles[1] = (byte) (Byte & 0xF);
        return nibbles;
    }

    public static byte flipBit(byte inByte, int targetBit) {
        if (targetBit < 0 || targetBit > 7) {
            throw new RuntimeException("targetBit must be 0 to 7!");
        }

        byte bitMask = (byte) (1 << targetBit);
        inByte ^= bitMask;

        return inByte;
    }

    public static byte setBit(byte inByte, int targetBit, boolean targetState) {
        if (targetBit < 0 || targetBit > 7) {
            throw new RuntimeException("targetBit must be 0 to 7!");
        }
        byte bitMask = (byte) (1 << targetBit);

        if (targetState) {
            inByte |= bitMask;
        } else {
            inByte &= ~bitMask;
        }

        return inByte;
    }

    public static boolean readBit(byte inByte, int targetBit) {
        if (targetBit < 0 || targetBit > 7) {
            throw new RuntimeException("targetBit must be 0 to 7!");
        }
        byte bitMask = (byte) (1 << targetBit);
        return (inByte & bitMask) != 0;
    }

    public static void dumpByteArrayToPrintStream(byte[] bytes, PrintStream printStream) {
        if (bytes.length == 0) {
            throw new RuntimeException("Byte Array must contain stuff!");
        }
        printStream.println("-=Byte Dump Start=-");
        for (byte b : bytes) {
            printStream.println(Integer.toBinaryString(b & 255 | 256).substring(1));
        }
        printStream.println("--=Byte Dump End=--");
    }
}
