package net.albiz.client.security;

public final class OfficialBuild {
   private OfficialBuild() {
   }

   public static OfficialBuild.State state() {
      return OfficialBuild.State.NOT_CHECKED;
   }

   public static void checkBeforeLaunch() {
   }

   public static void enforceIfPending() {
   }

   public static void enforceOnFirstTick() {
   }

   public static enum State {
      PENDING,
      VERIFIED,
      MODIFIED,
      NOT_CHECKED;
   }
}
