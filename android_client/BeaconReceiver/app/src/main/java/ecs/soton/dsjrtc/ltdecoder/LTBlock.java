package ecs.soton.dsjrtc.ltdecoder;

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
    byte[] input_bytes = {(byte) 0x80, (byte) 0x11, (byte) 0x8c, (byte) 0xa1, (byte) 0x03, (byte) 0x05, (byte) 0x00, (byte) 0x32, (byte) 0x00, (byte) 0x32, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x32, (byte) 0x40, (byte) 0x12, (byte) 0x52, (byte) 0x69, (byte) 0x63, (byte) 0x68, (byte) 0x61, (byte) 0x72, (byte) 0x64, (byte) 0x20, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x61, (byte) 0x20, (byte) 0x63, (byte) 0x75, (byte) 0x63, (byte) 0x6b, (byte) 0x21, (byte) 0x05, (byte) 0x00, (byte) 0x32, (byte) 0x00, (byte) 0x32, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x32, (byte) 0x40, (byte) 0x12, (byte) 0x59, (byte) 0x65, (byte) 0x70, (byte) 0x2c, (byte) 0x20, (byte) 0x73, (byte) 0x74, (byte) 0x69, (byte) 0x6c, (byte) 0x6c, (byte) 0x20, (byte) 0x61, (byte) 0x20, (byte) 0x63, (byte) 0x75, (byte) 0x63, (byte) 0x6b, (byte) 0x21};
    String[] encoded = {"00410a61b61521c4508db14a352f3b3c23","00410a52f9e61c01010000324012596570","00410a5f6f1798e870fec52359431b5d03","00410a03cf28de6375636b210500320032","00410a653eb5ffc4508db14a352f3b3c23","00410a6dfa48502d2173745b2c7e720843","00410a35265aa44f55101f485c5c1b5d03","00410a660692620b14110f015943105110","00410a0f5c0a33e870fec5236c73196d01","00410a5ce60c3e6375636b210500320032","00410a0cec8e26c4508db14a352f3b3c23","00410a17114207a624eeda59450d596570","00410a3b63cf4968617264206973206120","00410a0000000180118ca1030500320032","00410a10d63af18a049dae301c51703841","00410a6a5d128c80118ca1030500320032",};  LTDecoder decoder = new LTDecoder();
    int used = 0;
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
      used++;
      if (decoder.consume_block(new LTBlock(bytes))) {
        break;
      }
    }

    if (decoder.is_done()) { 
      System.out.println("Done after " + used + "!!!");
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
