package ecs.soton.dsjrtc.ltdecoder;

import java.util.Arrays;

public class Sampler {

  public final static double DEFAULT_C = 0.1;
  public final static double DEFAULT_DELTA = 0.5;

  public static double[] gen_tau(double S, int K, double delta) {
    int pivot = (int) Math.floor(K / S);
    double[] tau = new double[Math.max(pivot, K)];
    for (int d = 1; d < pivot; d++) {
      tau[d - 1] = (S / (double) K * 1 / (double) d);
    }
    tau[pivot - 1] = (S / (double) K * Math.log(S / delta));
    for (int d = pivot; d < K; d++) {
      tau[d] = 0;
    }
    return tau;
  }

  public static double[] gen_rho(int K) {
    double[] rho = new double[K];
    rho[0] = 1 / (double) K;
    for (int d = 2; d < K + 1; d++) {
      rho[d - 1] = 1 / (double) (d * (d - 1));
    }
    return rho;
  }

  public static double[] gen_mu(int K, double delta, double c) {
    double S = c * Math.log(K / delta) * Math.sqrt(K);
    double[] tau = gen_tau(S, K, delta);
    double[] rho = gen_rho(K);
    double normalizer = 0;
    for (int d = 0; d < K; d++) {
      normalizer += (rho[d] + tau[d]);
    }
    double[] mu = new double[K];
    for (int d = 0; d < K; d++) {
      mu[d] = (rho[d] + tau[d]) / normalizer;
    }
    return mu;
  }

  public static double[] gen_rsd_cdf(int K, double delta, double c) {
    double[] mu = gen_mu(K, delta, c);
    double[] rsd_cdf = new double[K];
    for (int d = 0; d < K; d++) {
      rsd_cdf[d] = mu[d];
      if (d >= 1) {
        rsd_cdf[d] += rsd_cdf[d - 1];
      }
    }
    return rsd_cdf;
  }


  public static void main(String[] args) {
    // Verify gen_tau
    double[] tau = Sampler.gen_tau(2.5134436810083125, 35, 0.5);
    double[] verif_tau =
        {0.0718126766002375, 0.03590633830011875, 0.023937558866745836, 0.017953169150059376,
            0.014362535320047502, 0.011968779433372918, 0.01025895380003393, 0.008976584575029688,
            0.007979186288915278, 0.007181267660023751, 0.006528425145476136, 0.005984389716686459,
            0.11596318039669093, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    System.out.println(Arrays.equals(tau, verif_tau));
    // Verify gen_tau2
    double[] tau2 = Sampler.gen_tau(0.4158883083359672, 4, 0.5);
    double[] verif_tau2 = {0.1039720770839918, 0.0519860385419959, 0.03465735902799726,
        0.02599301927099795, 0.02079441541679836, 0.01732867951399863, 0.014853153869141685,
        0.012996509635498974, -0.019150758673941327};
    System.out.println(Arrays.equals(tau2, verif_tau2));



    // Verify gen_rho
    double[] rho = Sampler.gen_rho(35);
    double[] verif_rho = {0.02857142857142857, 0.5, 0.16666666666666666, 0.08333333333333333, 0.05,
        0.03333333333333333, 0.023809523809523808, 0.017857142857142856, 0.013888888888888888,
        0.011111111111111112, 0.00909090909090909, 0.007575757575757576, 0.00641025641025641,
        0.005494505494505495, 0.004761904761904762, 0.004166666666666667, 0.003676470588235294,
        0.0032679738562091504, 0.0029239766081871343, 0.002631578947368421, 0.002380952380952381,
        0.0021645021645021645, 0.001976284584980237, 0.0018115942028985507, 0.0016666666666666668,
        0.0015384615384615385, 0.0014245014245014246, 0.0013227513227513227, 0.0012315270935960591,
        0.0011494252873563218, 0.001075268817204301, 0.0010080645161290322, 0.000946969696969697,
        0.00089126559714795, 0.0008403361344537816};
    System.out.println(Arrays.equals(rho, verif_rho));
    // Verify mu
    double[] mu = Sampler.gen_mu(35, 0.5, 0.1);
    double[] verif_mu = {0.07497992757656713, 0.4002846702159759, 0.14236806715408948,
        0.07565395545142685, 0.048074326395485374, 0.03383751968007642, 0.025446777449878045,
        0.02004292348906184, 0.016333927470556227, 0.013663131559695929, 0.011666553662411079,
        0.010128484586043972, 0.09140442516660868, 0.004104012516150365, 0.0035568108473303167,
        0.003112209491414027, 0.0027460671983064945, 0.002440948620716884, 0.0021840066606414223,
        0.00196560599457728, 0.0017784054236651583, 0.001616732203331962, 0.001476146794346574,
        0.0013531345614843596, 0.0012448837965656108, 0.0011491235045221022, 0.0010640032449278725,
        0.0009880030131473102, 0.0009198648743095646, 0.0008585405493555937, 0.0008031508364939425,
        0.000752953909213071, 0.0007073203389577334, 0.0006657132601955137, 0.0006276725024700559};
    System.out.println(Arrays.equals(mu, verif_mu));
    // Verify rsd_cdf
    double[] rsd_cdf = Sampler.gen_rsd_cdf(35, 0.5, 0.1);
    double[] verif_rsd_cdf = {0.07497992757656713, 0.47526459779254304, 0.6176326649466325,
        0.6932866203980593, 0.7413609467935447, 0.7751984664736211, 0.8006452439234991,
        0.820688167412561, 0.8370220948831172, 0.8506852264428131, 0.8623517801052242,
        0.8724802646912682, 0.9638846898578768, 0.9679887023740271, 0.9715455132213574,
        0.9746577227127714, 0.9774037899110779, 0.9798447385317948, 0.9820287451924362,
        0.9839943511870135, 0.9857727566106786, 0.9873894888140106, 0.9888656356083572,
        0.9902187701698415, 0.9914636539664071, 0.9926127774709292, 0.9936767807158571,
        0.9946647837290044, 0.995584648603314, 0.9964431891526696, 0.9972463399891636,
        0.9979992938983766, 0.9987066142373343, 0.9993723274975298, 0.9999999999999999};
    System.out.println(Arrays.equals(rsd_cdf, verif_rsd_cdf));
  }

}
