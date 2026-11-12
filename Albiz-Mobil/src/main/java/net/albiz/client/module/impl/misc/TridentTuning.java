package net.albiz.client.module.impl.misc;
/** Never multiply two independently enabled trident boosts. */
public final class TridentTuning {
 private TridentTuning(){}
 public static double combine(double legacy,double integrated){return safe(integrated)<=1?safe(legacy):Math.max(safe(legacy),safe(integrated));}
 private static double safe(double value){return Double.isFinite(value)?Math.max(.1,Math.min(10,value)):1;}
}
