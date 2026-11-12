package net.albiz.client.port.soberui;
public final class ConfigShare{
 public static String cleanName(String s){return s.replaceAll("[^A-Za-z0-9_]","");}
 public static String shortCode(String s){if(s==null)return null;String v=s.strip();return v.matches("(?i)SC-[A-Z0-9_]{3,16}")?v.substring(3):null;}
 public static java.util.concurrent.CompletableFuture<String> upload(String json,String name,java.util.Set<String> codes){return java.util.concurrent.CompletableFuture.failedFuture(new UnsupportedOperationException("Use Albiz local profiles; remote Sober upload is not enabled."));}
 public static java.util.concurrent.CompletableFuture<String> download(String code){return java.util.concurrent.CompletableFuture.failedFuture(new UnsupportedOperationException("Remote Sober download is not enabled."));}
}
