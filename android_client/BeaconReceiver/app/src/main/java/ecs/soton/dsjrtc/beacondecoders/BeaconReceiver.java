package ecs.soton.dsjrtc.beacondecoders;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import ecs.soton.dsjrtc.ltdecoder.LTBlock;
import ecs.soton.dsjrtc.ltdecoder.LTDecoder;

public class BeaconReceiver {
    // Test Packet Positions
    private static final int TEST_IDENTIFIER_1_BYTES = 16;
    private static final int TEST_ID_START = 0;
    private static final char[] TEST_ID_STR = "11AA22BB33CC44DD".toCharArray();
    private static final int TEST_ID_BYTES = TEST_ID_STR.length / 2;
    private static final int TEST_PACKET_NUM_START = TEST_ID_START + TEST_ID_BYTES;
    private static final int TEST_PACKET_NUM_BYTES = TEST_IDENTIFIER_1_BYTES - TEST_ID_BYTES;

    // Data Packet Positions
    private static final int DATA_IDENTIFIER_1_BYTES = 21;
    private static final int DATA_CHECKSUM_START = 0;
    private static final int DATA_CHECKSUM_BYTES = 2;
    private static final int DATA_CHUNK_ID_START = DATA_CHECKSUM_START + DATA_CHECKSUM_BYTES;
    private static final int DATA_CHUNK_ID_BYTES = 2;
    private static final int DATA_BLOCK_START = DATA_CHUNK_ID_START + DATA_CHUNK_ID_BYTES;
    private static final int DATA_BLOCK_BYTES = DATA_IDENTIFIER_1_BYTES - DATA_BLOCK_START;
    private static final int DATA_CHECKSUM_CALC_START = DATA_CHUNK_ID_START;
    private static final int DATA_CHECKSUM_CALC_BYTES = DATA_IDENTIFIER_1_BYTES - DATA_CHUNK_ID_START;
    public static final int DATA_CHECKSUM_MASK = (int) (Math.pow(2, DATA_CHECKSUM_BYTES*8) - 1);

    // Version identifier, used in checksums to verify that the recevier
    // is on the same version of advert formats. [Major (UINT8), Minor (UINT8)]
    private static final int FORMAT_VER = 0x0100;
    private static final int CHECKSUM_NONSE = 0xBEEF + FORMAT_VER;

    // General
    private Collection<Beacon> lastSeen = null;

    // Test packet structures
    private Set<Beacon> testBeacons = new HashSet<>();
    private int seenTestPackets = 0;
    private long maxTestPacketNum = 0;

    // Data packet structures
    private int seenDataPackets = 0;
    public final Map<Integer, ReceivedData> allReceivedData = new LinkedHashMap<>();
    private Map<Integer, LTDecoder> chunkDecoders = new HashMap<>();
    private Map<Integer, Long> seenTimes = new HashMap<>();
    private Map<Integer, Long> endTimes = new HashMap<>();
    private Map<Integer, Integer> chunkReceiveCount = new HashMap<>();
    private Set<Integer> failedDecodes = new HashSet<>();
    private int lastDecodedString = 0;

    /**
     * @return Whether the beacon identifies a test packet
     */
    public boolean isBeaconTestPacket(Beacon beacon) {
        int type = beacon.getBeaconTypeCode();
        // Must be an AltBeacon
        if (type == 0x0215) {
            Identifier data = beacon.getId1();
            byte[] recv = data.toByteArray();
            try {
                boolean isTestData = true;
                byte[] check = Hex.decodeHex(TEST_ID_STR);
                for (int i = 0; i < check.length; i++) {
                    isTestData &= (check[i] == recv[i]);
                }
                return isTestData;
            } catch (DecoderException e) {
                // shouldn't happen
            }
        }
        return false;
    }

    /**
     * Record the test beacon.
     */
    public void handleTestBeacon(Beacon beacon) {
        Identifier data = beacon.getId1();
        byte[] recv = data.toByteArray();
        ByteBuffer packetNum = ByteBuffer.wrap(recv, TEST_PACKET_NUM_START, TEST_PACKET_NUM_BYTES);
        long value = packetNum.getLong();
        if (value > maxTestPacketNum) {
            maxTestPacketNum = value;
        }
        testBeacons.add(beacon);
        // Track seen separately, this can differ if multiple of the same beacon are sent
        seenTestPackets += 1;
    }

    /**
     * @return Whether the beacon identifies a data packet.
     */
    public boolean isBeaconDataPacket(Beacon beacon) {
        int type = beacon.getBeaconTypeCode();
        // Must be a Data Broadcast Beacon
        if (type == 0xB0DC) {
            Identifier data = beacon.getId1();
            // Get the provided checksum
            byte[] recv = data.toByteArray();
            ByteBuffer checksumBytes = ByteBuffer.wrap(recv, DATA_CHECKSUM_START, DATA_CHECKSUM_BYTES);
            int checksum = ((int) checksumBytes.getShort()) & DATA_CHECKSUM_MASK;
            // Calculate the actual checksum
            CRC32 crc = new CRC32();
            crc.update(recv, DATA_CHECKSUM_CALC_START, DATA_CHECKSUM_CALC_BYTES);
            int calcChecksum = (int) (crc.getValue() + CHECKSUM_NONSE) & DATA_CHECKSUM_MASK;
            // Verify the checksums match, if they do not there may be a version
            // mismatch or a third party may be using the same type ID
            return checksum == calcChecksum;
        }
        return false;
    }

    /**
     * @param beacon Track and attempt to decode a data packet.
     * @return Whether a beacon broadcast has been decoded
     */
    public boolean handleDataBeacon(Beacon beacon) {
        // Track seen data packets
        seenDataPackets += 1;
        // Find the chunk number and the corresponding block
        Identifier data = beacon.getId1();
        byte[] recv = data.toByteArray();
        ByteBuffer chunkNum = ByteBuffer.wrap(recv, DATA_CHUNK_ID_START, DATA_CHUNK_ID_BYTES);
        int chunk = ((int) chunkNum.getShort() & 0xFFFF);
        if (!chunkDecoders.containsKey(chunk)) {
            chunkDecoders.put(chunk, new LTDecoder());
            chunkReceiveCount.put(chunk, 0);
            seenTimes.put(chunk, System.currentTimeMillis());
        }
        // Use the decoder
        LTDecoder decoder = chunkDecoders.get(chunk);
        // Don't attempt to handle if this data has already been decoded
        if (decoder.is_done()) {
            return false;
        }
        // Track those that are being put into the decoder
        chunkReceiveCount.put(chunk,chunkReceiveCount.get(chunk) + 1);

        // Attempt to decode with new block
        byte[] blockData = Arrays.copyOfRange(recv, DATA_BLOCK_START, DATA_BLOCK_START + DATA_BLOCK_BYTES);
        LTBlock block = new LTBlock(blockData);
        if (decoder.consume_block(block)) {
            endTimes.put(chunk, System.currentTimeMillis());
            // Successfully decoded, extract the data from the byte stream
            ReceivedData receivedData = new ReceivedData(decoder.get_decoded_bytes());
            // If the CRC is valid store it, otherwise decoder will need to be reset to
            // attempt again
            if (receivedData.isValid()) {
                recordReceivedData(chunk, receivedData);
                if (receivedData.getType() == ReceivedData.Type.TEXT) {
                    lastDecodedString = chunk;
                }
                return true;
            } else {
                failedDecodes.add(chunk);
            }
        }
        return false;
    }

    public void recordReceivedData(Integer key, ReceivedData newData) {
        allReceivedData.put(key, newData);
    }

    public boolean handleBeacons(Collection<Beacon> beacons) {
        lastSeen = beacons;
        boolean decodedNew = false;

        // Nothing new, nothing to do
        if (beacons.size() == 0) {
            return false;
        }
        // Handle incoming packet as either a data packet if it uses our specific
        // advertisement type. Otherwise check if it is an AltBeacon packet that
        // we are using for testing the receiver.
        for (Beacon beacon : beacons) {
            int type = beacon.getBeaconTypeCode();
            // Handle as a test packet
            if (isBeaconTestPacket(beacon)) {
                handleTestBeacon(beacon);
            }
            // Handle as a data packet
            if (isBeaconDataPacket(beacon)) {
                decodedNew |= handleDataBeacon(beacon);
            }
        }

        return decodedNew;
    }

    /**
     * Wipe decoders that resulted in a failed decode.
     */
    public void resetFailedDecoders() {
        for (Integer failedDecode : failedDecodes) {
            chunkDecoders.remove(failedDecode);
        }
    }

    public String getStatsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<b>Test Packets</b><br>");
        builder.append(String.format("%d / %d Beacons Seen<br>", testBeacons.size(), maxTestPacketNum));
        builder.append(String.format("Seen: %d<br>", seenTestPackets));

        builder.append("<br><b>Data Packets</b><br>");
        builder.append(String.format("Seen: %d<br>", seenDataPackets));
        builder.append(String.format("%5s: %6s - %7s - [%4s/%4s] | %8s | %s<br>",
                "Chunk", "Bytes", "Packets", "Got", "Need", "Result", "Time (ms)"));
        if (chunkDecoders.isEmpty()) {
            builder.append("* No Data Decoding *<br>");
        } else {
            for (int chunk : chunkDecoders.keySet()) {
                LTDecoder decoder = chunkDecoders.get(chunk);
                ReceivedData receivedData = allReceivedData.get(chunk);
                int inputSize = decoder.getFilesize();
                int seenPackets = chunkReceiveCount.get(chunk);
                int neededBlocks = decoder.getBlocksNeeded();
                int gotBlocks = decoder.getResolvedBlocks();
                String state;
                if (decoder.is_done()) {
                    if (receivedData != null) {
                        state = "SUCCESS!";
                    } else {
                        state = "FAILED!";
                    }
                } else {
                    state = "RECEIVING";
                }
                String time = "-";
                if (endTimes.containsKey(chunk)) {
                    time = String.valueOf(endTimes.get(chunk) - seenTimes.get(chunk));
                }

                String stats = String.format("%5d: %6d - %7d - [%4d/%4d] | %8s | %s <br>",
                        chunk, inputSize, seenPackets, gotBlocks, neededBlocks, state, time);
                builder.append(stats);
            }
        }

        builder.append("<br><b>Last Beacons Seen:</b><br>");
        if (lastSeen.isEmpty()) {
            builder.append("* None * <br>");
        } else {
            for (Beacon beacon : lastSeen) {
                Identifier beaconID = beacon.getId1();
                int power = beacon.getTxPower();
                String strID = beaconID.toString().replace("-", "");
                builder.append(strID + "<br>");
            }
        }

        return builder.toString();
    }
}
