package com.github.basdxz.journeymappartyhelper.things;

import com.google.common.math.IntMath;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.basdxz.journeymappartyhelper.Utils.*;

public class ChatFriendlyWaypoint extends Waypoint {
    public ChatFriendlyWaypoint() {
        super("", 0, 0, 0, Color.WHITE, Type.Normal, 0);
    }

    public ChatFriendlyWaypoint(Waypoint original) {
        super(original);
    }

    public void testChatFriendlyness() {
        fromChatFriendlyString(toChatFriendlyString());
    }

    public void fromChatFriendlyString(String cfWaypointString) {
        cfWaypointString = StringUtils.substringBetween(cfWaypointString, "{", "}");
        if (cfWaypointString == null) {
            throw new RuntimeException("Waypoint can't be null!");
        }
        byte[] cfWaypointBytes = cfWaypointString.getBytes(StandardCharsets.UTF_16LE);

        //Decode correction header
        byte correctionHeader = cfWaypointBytes[0];
        boolean isExtendedByAByte = readBit(correctionHeader, 5);
        boolean isCorrectiveExtendedByAByte = readBit(correctionHeader, 6);
        int requiredCorrectiveBytes = correctionHeader & 0b11111;
        int cfWaypointBytesExpectedEnd = cfWaypointBytes.length;

        //if(isExtendedByAByte) cfWaypointBytesExpectedEnd--;
        if (isCorrectiveExtendedByAByte) cfWaypointBytesExpectedEnd--;

        byte[] correctiveBytes = Arrays.copyOfRange(cfWaypointBytes, 1, requiredCorrectiveBytes + 1);
        short[] cfWaypointShorts = bytesToShorts(Arrays.copyOfRange(cfWaypointBytes, requiredCorrectiveBytes + 1,
                cfWaypointBytesExpectedEnd));
        int cByteCount = 0;
        int cBitCount = 1;
        for (int i = 0; i < cfWaypointShorts.length; i++) {
            //There are three cases which minecraft doesn't like and this covers all of them.
            if (readBit(correctiveBytes[cByteCount], 7 - cBitCount)) {
                cfWaypointShorts[i] += 32;
            }
            cBitCount++;
            //This is a page flipper
            if (cBitCount == 8) {
                cByteCount++;
                cBitCount = 1;
            }
        }
        //Undoes the chat corrective actions
        cfWaypointBytes = shortsToBytes(cfWaypointShorts);

        if (isExtendedByAByte) cfWaypointBytes = Arrays.copyOfRange(cfWaypointBytes, 0, cfWaypointBytes.length - 1);

        //Regenerate the offset header
        byte nameOffsetByte = cfWaypointBytes[0];
        byte posXYOffsetByte = cfWaypointBytes[1];
        byte posZDOffsetByte = cfWaypointBytes[2];

        //Regenerates offsets
        byte[] posXYOffsetBytes = nibblesFromByte(posXYOffsetByte);
        byte[] posZDOffsetBytes = nibblesFromByte(posZDOffsetByte);
        int nameOffset = (nameOffsetByte & 0xFF) + 1;
        int posXOffset = (posXYOffsetBytes[0] & 0xFF) + 1;
        int posYOffset = (posXYOffsetBytes[1] & 0xFF) + 1;
        int posZOffset = (posZDOffsetBytes[0] & 0xFF) + 1;
        int posDOffset = (posZDOffsetBytes[1] & 0xFF) + 1;
        int expectedPayloadLength = nameOffset + posXOffset + posYOffset + posZOffset + posDOffset;

        //Shift array by 3 to leave just the payload
        cfWaypointBytes = Arrays.copyOfRange(cfWaypointBytes, 3, cfWaypointBytes.length);

        if (cfWaypointBytes.length != expectedPayloadLength) {
            throw new RuntimeException("Malformed Waypoint payload header!");
        }

        int leftArrayPointer = 0;
        int rightArrayPointer = nameOffset;
        byte[] nameBytes = Arrays.copyOfRange(cfWaypointBytes, leftArrayPointer, rightArrayPointer);
        leftArrayPointer = rightArrayPointer;
        rightArrayPointer += posXOffset;
        byte[] posXBytes = Arrays.copyOfRange(cfWaypointBytes, leftArrayPointer, rightArrayPointer);
        leftArrayPointer = rightArrayPointer;
        rightArrayPointer += posYOffset;
        byte[] posYBytes = Arrays.copyOfRange(cfWaypointBytes, leftArrayPointer, rightArrayPointer);
        leftArrayPointer = rightArrayPointer;
        rightArrayPointer += posZOffset;
        byte[] posZBytes = Arrays.copyOfRange(cfWaypointBytes, leftArrayPointer, rightArrayPointer);
        leftArrayPointer = rightArrayPointer;
        rightArrayPointer += posDOffset;
        byte[] posDBytes = Arrays.copyOfRange(cfWaypointBytes, leftArrayPointer, rightArrayPointer);

        String name = new String(nameBytes, StandardCharsets.UTF_8);
        int posX = (new BigInteger(posXBytes)).intValue();
        int posY = (new BigInteger(posYBytes)).intValue();
        int posZ = (new BigInteger(posZBytes)).intValue();
        int posD = (new BigInteger(posDBytes)).intValue();

        setName(name);
        setLocation(posX, posY, posZ, posD);
    }

    public String toChatFriendlyString() {
        byte[] bytes = this.toByteArray();
        //UTF-16LE doesn't have garbage at the end or start of the string.
        return "{" + new String(bytes, StandardCharsets.UTF_16LE) + "}";
    }

    public byte[] toByteArray() {
        //Get all the payload data
        byte[] name = this.name.getBytes(StandardCharsets.UTF_8);
        byte[] posX = BigInteger.valueOf(this.x).toByteArray();
        byte[] posY = BigInteger.valueOf(this.y).toByteArray();
        byte[] posZ = BigInteger.valueOf(this.z).toByteArray();
        byte[] posD = BigInteger.valueOf((int) this.dimensions.toArray()[0]).toByteArray();

        //Offset of 0 equates to length of 1 on the decode
        int nameOffset = name.length - 1;
        int posXOffset = posX.length - 1;
        int posYOffset = posY.length - 1;
        int posZOffset = posZ.length - 1;
        int posDOffset = posD.length - 1;

        //Sanity check
        if (nameOffset > 255 || posXOffset > 15 || posYOffset > 15 || posZOffset > 15 || posDOffset > 15) {
            throw new RuntimeException("Waypoint too big!");
        }

        //Generate the offset header
        byte nameOffsetByte = (byte) nameOffset;
        byte posXYOffsetByte = byteFromNibbles((byte) posXOffset, (byte) posYOffset);
        byte posZDOffsetByte = byteFromNibbles((byte) posZOffset, (byte) posDOffset);
        byte[] offsetHeader = {nameOffsetByte, posXYOffsetByte, posZDOffsetByte};
        //Bit 7 always one in corrective header
        byte correctiveHeader = setBit((byte) 0, 7, true);

        //Get output length in bytes
        int outputBytesLength = 3 + name.length + posX.length + posY.length + posZ.length + posD.length;

        boolean isExtendedByAByte = false;
        //Checks if bytes is odd, adds an extra byte if so.
        if ((outputBytesLength & 1) == 1) {
            isExtendedByAByte = true;
            outputBytesLength++;
            //Bit 5 is used for extra odd to even byte marking
            correctiveHeader = setBit((byte) 0, 5, true);
        }

        //Populate the output buffer with the header and payload
        byte[] outputBytes = new byte[outputBytesLength];
        ByteBuffer byteBuffer = ByteBuffer.wrap(outputBytes);
        byteBuffer.put(offsetHeader);
        byteBuffer.put(name);
        byteBuffer.put(posX);
        byteBuffer.put(posY);
        byteBuffer.put(posZ);
        byteBuffer.put(posD);

        //Without this, the UTF-16 gets messed up since it needs 2 bytes per char.
        if (isExtendedByAByte) {
            byteBuffer.put((byte) 0xFF);
        }

        short[] outputShorts = bytesToShorts(outputBytes);
        int requiredCorrectiveBytes = IntMath.divide(outputShorts.length, 7, RoundingMode.CEILING);
        //This is like addition, but uses byte wise because that's actually what we're doing
        correctiveHeader |= requiredCorrectiveBytes;
        byte[] correctiveBytes = new byte[requiredCorrectiveBytes];

        int cByteCount = 0;
        int cBitCount = 0;
        for (int i = 0; i < outputShorts.length; i++) {
            //On each new byte, set the 7th bit
            if (cBitCount == 0) correctiveBytes[cByteCount] = setBit((byte) 0, 7, true);

            //There are three cases which minecraft doesn't like and this covers all of them.
            int outputShortUnsigned = outputShorts[i] & 0xFFFF;
            if (outputShortUnsigned == 167 || outputShortUnsigned < 32 || outputShortUnsigned == 127) {
                correctiveBytes[cByteCount] = setBit(correctiveBytes[cByteCount], 6 - cBitCount, true);
                outputShorts[i] -= 32;
            }
            cBitCount++;
            //This is a page flipper
            if (cBitCount == 7) {
                cByteCount++;
                cBitCount = 0;
            }
        }
        //Corrects the output, no more disconnects from bad characters
        outputBytes = shortsToBytes(outputShorts);

        //One for the header, and however long for the actual corrective data
        int correctiveLength = 1 + requiredCorrectiveBytes;

        boolean isCorrectiveExtendedByAByte = false;
        //Checks if bytes is odd, adds an extra byte if so.
        if ((correctiveLength & 1) == 1) {
            isCorrectiveExtendedByAByte = true;
            outputBytesLength++;
            correctiveHeader = setBit(correctiveHeader, 6, true);
        }

        //Populate the output buffer with the header and payload
        byte[] finalOutputBytes = new byte[correctiveLength + outputBytesLength];
        byteBuffer = ByteBuffer.wrap(finalOutputBytes);
        byteBuffer.put(correctiveHeader);
        byteBuffer.put(correctiveBytes);
        byteBuffer.put(outputBytes);

        //Without this, the UTF-16 gets messed up since it needs 2 bytes per char.
        if (isCorrectiveExtendedByAByte) {
            byteBuffer.put((byte) 0xFF);
        }

        //Sanity check
        if (finalOutputBytes.length > 198) {
            throw new RuntimeException("Final waypoint string too long!");
        }

        return finalOutputBytes;
    }

    public static List<String> getAllChatFriendlyWaypoints() {
        List<String> chatFriendlyWaypoints = new ArrayList<>();
        WaypointStore.instance().getAll().forEach(waypoint -> chatFriendlyWaypoints.add(
                waypoint.getId().replaceAll("\\s+", "")));
        return chatFriendlyWaypoints;
    }


}
