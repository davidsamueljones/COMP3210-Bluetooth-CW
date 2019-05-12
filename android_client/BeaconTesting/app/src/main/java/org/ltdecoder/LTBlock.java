package org.ltdecoder;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LTBlock {
  public static final int FILESIZE_BYTES = 2;
  public static final int BLOCKSIZE_BYTES = 1;
  public static final int BLOCKSEED_BYTES = 4;


  public static final int HEADER_START = 0;
  public static final int FILESIZE_START = HEADER_START;
  public static final int BLOCKSIZE_START = FILESIZE_START + FILESIZE_BYTES;
  public static final int BLOCKSEED_START = BLOCKSIZE_START + BLOCKSIZE_BYTES;
  public static final int BLOCK_START = BLOCKSEED_START + BLOCKSEED_BYTES;
  public static final int HEADER_BYTES = BLOCK_START;

  public final int filesize;
  public final int blocksize;
  public final long blockseed;
  public final byte[] block;

  public LTBlock(byte[] bytes) {
    this(getFilesize(bytes), getBlocksize(bytes), getBlockseed(bytes),
        getBlock(bytes, getBlocksize(bytes)));
  }


  public LTBlock(int filesize, int blocksize, long blockseed, byte[] block) {
    this.filesize = filesize;
    this.blocksize = blocksize;
    this.blockseed = blockseed;
    this.block = block;
  }

  public static int getFilesize(byte[] bytes) {
    byte[] fs_bytes = Arrays.copyOfRange(bytes, FILESIZE_START, FILESIZE_START + FILESIZE_BYTES);
    int filesize = ((int) ByteBuffer.wrap(fs_bytes).getShort()) & 0xFFFF;
    return filesize;
  }

  public static int getBlocksize(byte[] bytes) {
    byte[] bs_bytes = Arrays.copyOfRange(bytes, BLOCKSIZE_START, BLOCKSIZE_START + BLOCKSIZE_BYTES);
    int blocksize = ((int) ByteBuffer.wrap(bs_bytes).get()) & 0xFF;
    return blocksize;
  }

  public static long getBlockseed(byte[] bytes) {
    byte[] bs_bytes = Arrays.copyOfRange(bytes, BLOCKSEED_START, BLOCKSEED_START + BLOCKSEED_BYTES);
    long blockseed = ((long) ByteBuffer.wrap(bs_bytes).getInt() & 0xFFFFFFFF);
    return blockseed;
  }

  public static byte[] getBlock(byte[] bytes, int blocksize) {
    byte[] block_bytes = Arrays.copyOfRange(bytes, BLOCK_START, BLOCK_START + blocksize);
    return block_bytes;
  }

  public static void main(String[] args) {
        "01720f41d8fec115050b4750150b0a1154566521040d",
    byte b = 0x1;

    byte[] input_bytes = {(byte) 0x8a, (byte) 0x5d, (byte) 0xa1, (byte) 0x8d, (byte) 0x03, (byte) 0x05, (byte) 0x00, (byte) 0x32, (byte) 0x00, (byte) 0x32, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x32, (byte) 0x40, (byte) 0x02, (byte) 0x52, (byte) 0x21};
    String[] encoded = {"00130a5afb64d18a5da18d030500320032","00130a7e2ef58c01010000324002522130","00130a0177f01b8b5ca18d314502602102","00130a3eb214f701010000324002522130","00130a5d1ee3bf01010000324002522130","00130a079763cd8b5ca18d314502602102","00130a63a8c44f8b5ca18d314502602102","00130a2274580801010000324002522130","00130a66b0e4e98b5ca18d314502602102","00130a35e59cc38b5ca18d314502602102","00130a6bb2ea788a5da18d030500320032","00130a7b0888fe8b5ca18d314502602102","00130a10cfba6501010000324002522130","00130a066d756b8b5ca18d314502602102","00130a1204bcef8b5ca18d314502602102","00130a316b23658b5ca18d314502602102","00130a322321118a5da18d030500320032","00130a635e48f48b5ca18d314502602102","00130a057114878b5ca18d314502602102","00130a6742b8d88b5ca18d314502602102",};

    LTDecoder decoder = new LTDecoder();

    for (String str_bytes : encoded) {
      byte[] bytes = new byte[str_bytes.length() / 2];
      for (int i = 0; i < bytes.length; i++) {
        int index = i * 2;
        int j = Integer.parseInt(str_bytes.substring(index, index + 2), 16);
        bytes[i] = (byte) j;
      }

//      System.out.println(str_bytes);
//      System.out.print("(");
//      System.out.print(getFilesize(bytes)  + ", ");
//      System.out.print(getBlocksize(bytes)  + ", ");
//      System.out.print(getBlockseed(bytes));
//      System.out.println(")");
      
      // Consume till finished
      if (decoder.consume_block(new LTBlock(bytes))) {
        break;
      }
    }

    if (decoder.is_done()) { 
      System.out.println("Done!!!");
      byte[] decoded = decoder.get_decoded_bytes();
      if (Arrays.equals(input_bytes, decoded)) {
        System.out.println("[SUCCESS] Input == Decoded");
      } else {
        System.out.println("[FAILED] Input != Decoded");
      }
    } else {
      System.out.println("[FAILED] Reached EOS!!!");
    }
      





    // String str_bytes = "01c71025065a5d6a6374247e6d6a29676e1a406f2c733c";
    // byte[] bytes = new byte[str_bytes.length() / 2];

    // System.out.println(getFilesize(bytes));
    // System.out.println(getBlocksize(bytes));
    //
    // System.out.println(Arrays.toString(getBlock(bytes, getBlocksize(bytes))));
  }

}
