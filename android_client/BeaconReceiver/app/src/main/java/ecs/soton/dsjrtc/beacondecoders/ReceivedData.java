package ecs.soton.dsjrtc.beacondecoders;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

public class ReceivedData {
    // Data Packet Positions
    private static final int DATA_CHECKSUM_START = 0;
    private static final int DATA_CHECKSUM_BYTES = 4;
    private static final int DATA_TYPE_ID_START = DATA_CHECKSUM_START + DATA_CHECKSUM_BYTES;
    private static final int DATA_TYPE_ID_BYTES = 1;
    public static final int DATA_TYPE_ID_MASK = (int) (Math.pow(2, DATA_TYPE_ID_BYTES*8) - 1);
    private static final int DATA_BLOCK_START = DATA_TYPE_ID_START + DATA_TYPE_ID_BYTES;
    private static final int DATA_CHECKSUM_CALC_START = DATA_TYPE_ID_START;
    public static final long DATA_CHECKSUM_MASK = (long) (Math.pow(2, DATA_CHECKSUM_BYTES*8) - 1);

    public static final int TEXT_TYPE_ID = 1;
    public static final int IMAGE_TYPE_ID = 2;
    public static final int ADVERT_TYPE_ID = 3;

    // Class variables
    private byte[] payload;
    private Type type;
    private boolean valid;

    public ReceivedData(byte[] data) {
        this.valid = checkDataValid(data);
        this.type = extractType(data);
        this.payload = extractPayload(data);
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * @return Whether the CRC indicates that the data is valid
     */
    private boolean checkDataValid(byte[] data) {
        // Get the provided checksum
        ByteBuffer checksumBytes = ByteBuffer.wrap(data, DATA_CHECKSUM_START, DATA_CHECKSUM_BYTES);
        long checksum = ((long) checksumBytes.getInt()) & DATA_CHECKSUM_MASK;
        // Calculate the actual checksum
        CRC32 crc = new CRC32();
        int dataChecksumCalcBytes = data.length - DATA_CHECKSUM_CALC_START;
        crc.update(data, DATA_CHECKSUM_CALC_START, dataChecksumCalcBytes);
        long calcChecksum = ((long) crc.getValue() & DATA_CHECKSUM_MASK);
        // Verify the checksums match, if they do not there may be a version
        // mismatch or a third party may be using the same type ID
        return checksum == calcChecksum;
    }


    public Type getType() {
        return type;
    }


    private Type extractType(byte[] data) {
        // Get the provided type
        ByteBuffer typeBytes = ByteBuffer.wrap(data, DATA_TYPE_ID_START, DATA_TYPE_ID_BYTES);
        int typeInt = ((int) typeBytes.get()) & DATA_TYPE_ID_MASK;
        switch (typeInt) {
            case TEXT_TYPE_ID:
                return Type.TEXT;
            case IMAGE_TYPE_ID:
                return Type.IMAGE;
            case ADVERT_TYPE_ID:
                return Type.ADVERT;
            default:
                return Type.UNKNOWN;
        }
    }

    public byte[] getPayload() {
        return payload;
    }

    private byte[] extractPayload(byte[] data) {
        payload = Arrays.copyOfRange(data, DATA_BLOCK_START, data.length);
        return payload;
    }

    /**
     * Receive types that can be handled
     */
    public enum Type {
        TEXT, IMAGE, ADVERT, UNKNOWN
    }

}
