import java.util.Iterator;
import java.util.Set;

public class LTDecoder {

  public final double c;
  public final double delta;


  private int K = 0;
  private int filesize = 0;
  private int blocksize = 0;
  private BlockGraph block_graph;
  private PRNG prng = null;
  private boolean initialised = false;
  private boolean done = false;

  public LTDecoder() {
    this(Sampler.DEFAULT_C, Sampler.DEFAULT_DELTA);
  }

  public LTDecoder(double c, double delta) {
    this.c = c;
    this.delta = delta;
  }

  public boolean is_done() {
    return this.done;
  }

  public boolean consume_block(LTBlock lt_block) {
    // First time around, initialise
    if (!initialised) {
      this.filesize = lt_block.filesize;
      this.blocksize = lt_block.blocksize;
      this.K = (int) Math.ceil(filesize / (double) blocksize);
      this.block_graph = new BlockGraph(this.K);
      this.prng = new PRNG(this.K, this.delta, this.c);
      this.initialised = true;
    }
    // Run PRNG with given seed to figure out which blocks were XORed to make received data
    SRCBlocks src_blocks = prng.get_src_blocks(lt_block.blockseed);
    // If BP is done, stop
    this.done = handle_block(src_blocks.nums, lt_block.block);
    return this.done;
  }

  private boolean handle_block(Set<Integer> src_blocks, byte[] block) {
    // return block_graph.add_block(src_blocks, block);
    return block_graph.add_block(src_blocks, block);
  }

  public byte[] get_decoded_bytes() {
    if (!is_done()) {
      System.err.println("WARNING: Getting decoded bytes before decoding has finished");
    }
    byte[] decoded = new byte[filesize];
    Iterator<byte[]> itr_block_bytes = block_graph.get_block_bytes_iterator();
    int block = 0;
    while (itr_block_bytes.hasNext()) {
      byte[] block_bytes = itr_block_bytes.next();
      for (int i = 0; i < block_bytes.length; i++) {
        if (block * blocksize + i >= filesize) {
          return decoded;
        }
        decoded[block * blocksize + i] = block_bytes[i];
      }
      block++;
    }
    return decoded;
  }



}
