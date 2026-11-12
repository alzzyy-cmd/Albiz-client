package net.albiz.client.platform;
public final class BuildProfile {
 private static final boolean MOBILE = load();
 private BuildProfile() {}
 private static boolean load(){try(var in=BuildProfile.class.getResourceAsStream("/albiz-profile.properties")){var p=new java.util.Properties();if(in!=null)p.load(in);return "mobile".equals(p.getProperty("profile"));}catch(java.io.IOException e){return false;}}
 public static boolean mobile(){return MOBILE;}
}
