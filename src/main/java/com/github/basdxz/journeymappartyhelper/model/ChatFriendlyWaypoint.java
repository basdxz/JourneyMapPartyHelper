package com.github.basdxz.journeymappartyhelper.model;

import com.github.basdxz.journeymappartyhelper.Util;
import com.google.common.math.IntMath;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;

import java.awt.Color;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFriendlyWaypoint extends Waypoint {
    private static final long serialVersionUID = -81907985975076374L;
    //How many things we are taking back and fourth
    private static final int FIELDS_TRANSCODED = 5;
    private static final int DATA_PAYLOAD_HEADER_LENGTH = 3;
    private static final int CORRECTION_PAYLOAD_HEADER_LENGTH = 1;
    //Fully aware this sets the byte to -128
    @SuppressWarnings("NumericCastThatLosesPrecision")
    private static final byte PRE_CORRECTED_BYTE_MASK = (byte) 0b1000_0000; //-127
    private static final byte CORRECTION_BYTES_MAX_LENGTH = 0b0011_1111;
    private static final byte ODD_DATA_BYTE_MASK = 0b0100_0000;
    //Sending § or  will get your client kicked
    private static final char[] INVALID_CHAT_CHARS = {167, 127};
    //Anything less than blank space will also kick you
    private static final char LOWEST_VALID_CHAT_CHAR = 32;
    public static final int ERROR_DIM_ID = -42069;

    public ChatFriendlyWaypoint(Waypoint original) {
        super(original);
    }

    public ChatFriendlyWaypoint(String name, int posX, int posY, int posZ, int currentDimension) {
        super(name, posX, posY, posZ, Color.WHITE, Type.Normal, currentDimension);
    }

    public String toChatString(){
        return Util.byteArrayToUTF16String(Encode.toByteArray(this));
    }

    public String toChatReadableString(){
        return "[Name: " + getName() + " X:" + getX() + " Y:" + getY() + " Z:" + getZ() + " D:" +
                getDimensions().toArray()[0] + "]";
    }

    public static ChatFriendlyWaypoint fromChatString(String inputString){
        String dataSubString = inputString.substring(1, inputString.length() - 1);
        ChatFriendlyWaypoint outputWaypoint = null;

        //TODO: Make this check in the Chat reading regex stuff
        if (dataSubString.length() >= 5){
            try{
                byte[] rawWaypointBytes = Util.aUTF16StringToByteArray(dataSubString);
                outputWaypoint = Decode.fromByteArray(rawWaypointBytes);
            } catch (Exception ignored) {
                outputWaypoint = new ChatFriendlyWaypoint(" ", 0, 0, 0, ERROR_DIM_ID);
            }
        }

        return outputWaypoint;
    }

    //TODO: enum to class with private constructor in separate file
    private enum Encode {
        ;
        static byte[] toByteArray(ChatFriendlyWaypoint orgin) {
            //Creates the data
            byte[][] rawData = getRawData(orgin);
            byte[] dataHeader = getDataHeader(rawData);
            byte[] dataPayload = getDataPayload(rawData);
            byte[] dataPreCorrection = Util.concatenateByteArrays(dataHeader, dataPayload);
            //Creates the corrections for chat transfer
            byte[] correctionHeader = getCorrectionHeader(dataPreCorrection.length);
            dataPreCorrection = makeDataBytesEven(dataPreCorrection);
            byte[] correctionPayload = getCorrectionPayload(dataPreCorrection);
            byte[] dataPostCorrection = correctData(dataPreCorrection, correctionPayload);
            //TODO: Check if byte count even, throw exception if not!
            return Util.concatenateByteArrays(correctionHeader, correctionPayload, dataPostCorrection);
        }

        private static byte[][] getRawData(ChatFriendlyWaypoint orgin){
            //Prevents an empty name
            String safeName;
            if(orgin.name.isEmpty()){
                safeName = " ";
            } else {
                safeName = orgin.name;
            }

            //Get payload data into fields
            //Note: BigInteger's toByteArray will drop leading 0 bytes
            byte[] nameBytes = safeName.getBytes(StandardCharsets.UTF_8);
            //TODO: Dont use BigIntergers, switch to: Ints.toByteArray(<int>)
            byte[] xBytes = BigInteger.valueOf(orgin.getX()).toByteArray();
            byte[] yBytes = BigInteger.valueOf(orgin.getY()).toByteArray();
            byte[] zBytes = BigInteger.valueOf(orgin.getZ()).toByteArray();
            byte[] dBytes = BigInteger.valueOf((int) orgin.getDimensions().toArray()[0]).toByteArray();
            return new byte[][]{nameBytes, xBytes, yBytes, zBytes, dBytes};
        }

        private static byte[] getDataHeader(byte[]... payload){
            if (payload.length != FIELDS_TRANSCODED){
                throw new IllegalArgumentException("Header needs " + FIELDS_TRANSCODED + " values to generate!");
            }

            //Offset of 0 equates to length of 1 on the decode, because data is always 1 to 16 not 0 to 15
            int nameOffsetByte = payload[0].length - 1;
            int xPosOffsetNibble = payload[1].length - 1;
            int yPosOffsetNibble = payload[2].length - 1;
            int zPosOffsetNibble = payload[3].length - 1;
            int dPosOffsetNibble = payload[4].length - 1;

            //Bound check pre casting
            if (nameOffsetByte > Util.BYTE_MASK){
                throw new IllegalArgumentException("Waypoint name too big!");
            }
            if (xPosOffsetNibble > Util.NIBBLE_MASK){
                throw new IllegalArgumentException("Waypoint xPos too big!");
            }
            if (yPosOffsetNibble > Util.NIBBLE_MASK){
                throw new IllegalArgumentException("Waypoint yPos too big!");
            }
            if (zPosOffsetNibble > Util.NIBBLE_MASK){
                throw new IllegalArgumentException("Waypoint zPos too big!");
            }
            if (dPosOffsetNibble > Util.NIBBLE_MASK){
                throw new IllegalArgumentException("Waypoint dPos too big!");
            }

            byte[] header = new byte[3];
            ByteBuffer headerBuffer = ByteBuffer.wrap(header);
            //noinspection NumericCastThatLosesPrecision
            headerBuffer.put((byte) nameOffsetByte);
            headerBuffer.put(Util.byteFromNibbles((byte) xPosOffsetNibble, (byte) yPosOffsetNibble));
            headerBuffer.put(Util.byteFromNibbles((byte) zPosOffsetNibble, (byte) dPosOffsetNibble));

            return header;
        }

        private static byte[] getDataPayload(byte[]... payload){
            if (payload.length != FIELDS_TRANSCODED){
                throw new IllegalArgumentException("Payload needs " + FIELDS_TRANSCODED + " values to generate!");
            }
            return Util.concatenateByteArrays(payload);
        }

        private static boolean isDataBytesLengthOdd(int dataBytesLength) {
            //Sum of Correction header, correction payload, data header and data payload in bytes
            int sumLength = dataBytesLength + getCorrectionBytesLength(dataBytesLength);
            return (sumLength & 1) == 1;
        }

        private static byte[] getCorrectionHeader(int dataPayloadLength) {
            int correctionHeader = PRE_CORRECTED_BYTE_MASK;
            int correctionPayloadLength = getCorrectionPayloadLength(dataPayloadLength);
            correctionHeader |= correctionPayloadLength;
            if (isDataBytesLengthOdd(dataPayloadLength)) {
                correctionHeader |= ODD_DATA_BYTE_MASK;
            }
            return new byte[]{(byte) correctionHeader};
        }

        //TODO: Don't use var args!
        private static byte[] makeDataBytesEven(byte... inputBytes) {
            if (inputBytes == null) {
                throw new IllegalArgumentException("Input bytes can't be null!");
            }

            byte[] outputBytes;
            if (isDataBytesLengthOdd(inputBytes.length)) {
                outputBytes = Util.concatenateByteArrays(inputBytes, new byte[]{PRE_CORRECTED_BYTE_MASK});
            } else {
                outputBytes = inputBytes;
            }

            return outputBytes;
        }

        //TODO: Don't use var args!
        private static byte[] getCorrectionPayload(byte... inputData) {
            if (inputData == null) {
                throw new IllegalArgumentException("Input bytes can't be null!");
            }

            byte[] dataBytes;
            //Ensures the data is even before to short conversion
            if ((inputData.length & 1) == 0) {
                dataBytes = inputData;
            } else {
                dataBytes = Util.concatenateByteArrays(new byte[]{PRE_CORRECTED_BYTE_MASK}, inputData);
            }
            short[] dataShorts = Util.bytesToShorts(dataBytes);

            byte[] correctionPayload = new byte[getCorrectionPayloadLength(dataBytes.length)];
            int cByteCount = 0;
            int cBitCount = 0;
            for (short dataShort : dataShorts) {
                //On each new byte, set the 7th bit
                if (cBitCount == 0) {
                    correctionPayload[cByteCount] = PRE_CORRECTED_BYTE_MASK;
                }

                //There are three cases which minecraft doesn't like and this covers all of them.
                int aUTF16CharValue = dataShort & Util.SHORT_MASK;
                if (aUTF16CharValue == INVALID_CHAT_CHARS[0] || aUTF16CharValue == INVALID_CHAT_CHARS[1] ||
                        aUTF16CharValue < LOWEST_VALID_CHAT_CHAR) {
                    correctionPayload[cByteCount] = Util.setBitHigh(correctionPayload[cByteCount], 6 - cBitCount);
                }
                cBitCount++;
                //This is a page flipper
                if (cBitCount == 7) {
                    cByteCount++;
                    cBitCount = 0;
                }
            }

            return correctionPayload;
        }

        //TODO: Don't use var args!
        static byte[] correctData(byte[]... inputBytes) {
            //TODO: Cut down on the sanity checks, put more on one line
            if (inputBytes.length != 2) {
                throw new IllegalArgumentException("Input must be data and corrective payload!");
            }
            byte[] dataBytes = inputBytes[0];
            byte[] correctionPayloadBytes = inputBytes[1];
            if (dataBytes == null) {
                throw new IllegalArgumentException("Data can't be null!");
            }
            if (correctionPayloadBytes == null) {
                throw new IllegalArgumentException("Correction Payload can't be null!");
            }
            if (dataBytes.length == 0) {
                throw new IllegalArgumentException("Data can't zero in length!");
            }
            if (correctionPayloadBytes.length == 0) {
                throw new IllegalArgumentException("Correction Payload can't zero in length!");
            }
            if (correctionPayloadBytes.length >= dataBytes.length) {
                throw new IllegalArgumentException("Correction Payload must be shorter than Data!");
            }

            //Ensures the data is even before to short conversion
            boolean isInputDataByteCountOdd = false;
            if ((dataBytes.length & 1) == 1) {
                dataBytes = Util.concatenateByteArrays(new byte[]{PRE_CORRECTED_BYTE_MASK}, dataBytes);
                isInputDataByteCountOdd = true;
            }
            short[] dataShorts = Util.bytesToShorts(dataBytes);

            int cByteCount = 0;
            int cBitCount = 0;
            for (int cShortCount = 0; cShortCount < dataShorts.length; cShortCount++) {
                if (Util.readBit(correctionPayloadBytes[cByteCount], 6 - cBitCount)) {
                    int correctableShort = dataShorts[cShortCount];
                    //This produces its compliment, aka 0110 -> 1001. Works both ways.
                    //Then ands it with a short mask to clear the higher bits pre casting.
                    correctableShort = ~correctableShort & Util.SHORT_MASK;
                    //noinspection NumericCastThatLosesPrecision
                    dataShorts[cShortCount] = (short) correctableShort;
                }
                cBitCount++;
                //This is a page flipper
                if (cBitCount == 7) {
                    cByteCount++;
                    cBitCount = 0;
                }
            }
            byte[] correctedDataBytes = Util.shortsToBytes(dataShorts);

            //Ensures the data is odd again if we made it even for the correction
            if (isInputDataByteCountOdd) {
                correctedDataBytes = Arrays.copyOfRange(correctedDataBytes, 1, correctedDataBytes.length);
            }

            return correctedDataBytes;
        }

        private static int getCorrectionBytesLength(int dataBytesLength) {
            return getCorrectionPayloadLength(dataBytesLength) + CORRECTION_PAYLOAD_HEADER_LENGTH;
        }

        private static int getCorrectionPayloadLength(int dataBytesLength) {
            if (dataBytesLength > CORRECTION_BYTES_MAX_LENGTH) {
                throw new IllegalArgumentException("Correction payload would be too long!");
            }
            //Divide by two, always rounding up to get the length in shorts
            int payloadShortsSumLength = IntMath.divide(dataBytesLength, 2, RoundingMode.CEILING);
            //Each corrective byte has 7 useable bits.
            //Shorts are used because UTF-16 characters take up two bytes.
            return IntMath.divide(payloadShortsSumLength, 7, RoundingMode.CEILING);
        }
    }

    //TODO: enum to class with private constructor in separate file
    private enum Decode {
        ;
        static ChatFriendlyWaypoint fromByteArray(byte[] bytes){
            byte correctionHeader = bytes[0];
            int correctionPayloadLength = getCorrectionPayloadLength(correctionHeader);

            byte[] correctionPayload = Arrays.copyOfRange(bytes, 1, correctionPayloadLength + 1);
            byte[] dataPostCorrection = Arrays.copyOfRange(bytes, correctionPayloadLength + 1, bytes.length);
            byte[] dataPreCorrection = correctData(dataPostCorrection, correctionPayload);

            byte[] dataHeader = Arrays.copyOfRange(dataPreCorrection, 0, DATA_PAYLOAD_HEADER_LENGTH);
            byte[] offsetBytes = getOffsetBytes(dataHeader);

            byte[] dataPayload;
            if(isOddDataByte(correctionHeader)){
                dataPayload = Arrays.copyOfRange(dataPreCorrection, DATA_PAYLOAD_HEADER_LENGTH,
                        dataPreCorrection.length - 1);
            } else {
                dataPayload = Arrays.copyOfRange(dataPreCorrection, DATA_PAYLOAD_HEADER_LENGTH,
                        dataPreCorrection.length);
            }
            //TODO: Put dataPayload and offsetBytes into new getFromRaw method inside Decode

            String name = getName(dataPayload, offsetBytes);
            int[] positionData = getPosition(dataPayload, offsetBytes);

            int x = positionData[0];
            int y = positionData[1];
            int z = positionData[2];
            int dim = positionData[3];

            return new ChatFriendlyWaypoint(name, x, y, z, dim);
        }

        private static boolean isOddDataByte(byte correctionHeaderByte) {
            return (byte) (correctionHeaderByte & ODD_DATA_BYTE_MASK) == 1;
        }

        private static int getCorrectionPayloadLength(byte correctionHeaderByte) {
            return correctionHeaderByte & CORRECTION_BYTES_MAX_LENGTH;
        }

        private static byte[] getOffsetBytes(byte... inputBytes) {
            if (inputBytes.length != DATA_PAYLOAD_HEADER_LENGTH) {
                throw new IllegalArgumentException("Requires " + DATA_PAYLOAD_HEADER_LENGTH + " Bytes to get offsets!");
            }
            int nameOffset = inputBytes[0] + 1;

            byte[] nXandYOffsets = Util.nibblesFromByte(inputBytes[1]);
            int xOffset = nXandYOffsets[0] + 1;
            int yOffset = nXandYOffsets[1] + 1;

            byte[] nZandDOffsets = Util.nibblesFromByte(inputBytes[2]);
            int zOffset = nZandDOffsets[0] + 1;
            int dOffset = nZandDOffsets[1] + 1;

            //noinspection NumericCastThatLosesPrecision
            return new byte[]{(byte) nameOffset, (byte) xOffset, (byte) yOffset, (byte) zOffset, (byte) dOffset};
        }

        private static String getName(byte[]... inputBytes) {
            if (inputBytes.length != 2) {
                throw new IllegalArgumentException("Input Bytes must be 2 in length!");
            }
            byte[] inputData = inputBytes[0];
            byte[] inputOffsets = inputBytes[1];
            if (inputData == null) {
                throw new IllegalArgumentException("Input Data can't be null!");
            }
            if (inputOffsets == null) {
                throw new IllegalArgumentException("Input Offsets can't be null!");
            }
            byte nameOffset = inputOffsets[0];
            return new String(Arrays.copyOfRange(inputData, 0, nameOffset), StandardCharsets.UTF_8);
        }

        @SuppressWarnings("NumericCastThatLosesPrecision")
        private static int[] getPosition(byte[]... inputBytes) {
            if (inputBytes.length != 2) {
                throw new IllegalArgumentException("Input Bytes must be 2 in length!");
            }
            byte[] inputData = inputBytes[0];
            byte[] inputOffsets = inputBytes[1];
            if (inputData == null) {
                throw new IllegalArgumentException("Input Data can't be null!");
            }
            if (inputOffsets == null) {
                throw new IllegalArgumentException("Input Offsets can't be null!");
            }

            byte namePosOffset = inputOffsets[0];
            byte xPosOffset = (byte) (namePosOffset + inputOffsets[1]);
            byte yPosOffset = (byte) (xPosOffset + inputOffsets[2]);
            byte zPosOffset = (byte) (yPosOffset + inputOffsets[3]);
            byte dPosOffset = (byte) (zPosOffset + inputOffsets[4]);

            byte[] xPosbytes = Arrays.copyOfRange(inputData, namePosOffset, xPosOffset);
            //TODO: Dont use BigIntergers, switch to: Ints.toByteArray(<int>)
            int xPos = new BigInteger(xPosbytes).intValue();

            byte[] yPosbytes = Arrays.copyOfRange(inputData, xPosOffset, yPosOffset);
            int yPos = new BigInteger(yPosbytes).intValue();

            byte[] zPosbytes = Arrays.copyOfRange(inputData, yPosOffset, zPosOffset);
            int zPos = new BigInteger(zPosbytes).intValue();

            byte[] dPosbytes = Arrays.copyOfRange(inputData, zPosOffset, dPosOffset);
            int dPos = new BigInteger(dPosbytes).intValue();

            return new int[]{xPos, yPos, zPos, dPos};
        }

        private static byte[] correctData(byte[] dataBytes, byte[] correctionBytes){
            //Method is symetrical
            return Encode.correctData(dataBytes, correctionBytes);
        }
    }

    public static List<String> getAllChatFriendlyWaypoints() {
        List<String> chatFriendlyWaypoints = new ArrayList<>();
        WaypointStore.instance().getAll().forEach(waypoint -> chatFriendlyWaypoints.add(
                waypoint.getId().replaceAll("\\s+", "")));
        return chatFriendlyWaypoints;
    }
}
