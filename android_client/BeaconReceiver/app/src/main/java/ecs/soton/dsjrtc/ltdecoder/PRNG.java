package ecs.soton.dsjrtc.ltdecoder;

import java.util.HashSet;
import java.util.Set;

public class PRNG {
  public final static int PRNG_A = 16807;
  public final static long PRNG_M = (1 << 31) - 1;
  public final static long PRNG_MAX_RAND = PRNG_M - 1;

  private Long state;
  private final int K;
  private final double[] cdf;

  public PRNG(int K, double delta, double c) {
    this.state = null;
    this.K = K;
    this.cdf = Sampler.gen_rsd_cdf(K, delta, c);
  }

  private long get_next() {
    this.state = (PRNG_A * this.state) % PRNG_M;
    return this.state;
  }

  private int sample_d() {
    double p = get_next() / (double) PRNG_MAX_RAND;
    for (int i = 0; i < cdf.length; i++) {
      double v = cdf[i];
      if (v > p) {
        return i + 1;
      }
    }
    return cdf.length;
  }

  public void set_seed(long seed) {
    this.state = seed;
  }

  public SRCBlocks get_src_blocks() {
    return get_src_blocks(null);
  }

  public SRCBlocks get_src_blocks(Long seed) {
    if (seed != null) {
      this.state = seed;
    }
    long blockseed = this.state;
    int d = sample_d();
    int have = 0;
    Set<Integer> nums = new HashSet<>();
    while (have < d) {
      int num = (int) (get_next() % K);
      if (!nums.contains(num)) {
        nums.add(num);
        have += 1;
      }
    }
    return new SRCBlocks(blockseed, d, nums);
  }

  public static void main(String[] args) {
    check_prng(25, "[84]");
    check_prng(50, "[68]");
    check_prng(75, "[52]");
    check_prng(100, "[89]");
  }
  
  public static void check_prng(long seed, String exp) {
    PRNG prng = new PRNG(100, Sampler.DEFAULT_DELTA, Sampler.DEFAULT_C);
    String go1 = prng.get_src_blocks(seed).nums.toString();
    String go2 = prng.get_src_blocks().nums.toString();
    System.out.println(String.format("Got: %s | Expected: %s | %s", go1, exp, go1.equals(exp)));
    System.out.println("Next Lot: " + go2);
  }

  public class SRCBlocks {
    public final long blockseed;
    public final int d;
    public final Set<Integer> nums;

    public SRCBlocks(long blockseed, int d, Set<Integer> nums) {
      this.blockseed = blockseed;
      this.d = d;
      this.nums = nums;
    }
  }



}


