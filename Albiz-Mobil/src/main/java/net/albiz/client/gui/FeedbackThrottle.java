package net.albiz.client.gui;
public final class FeedbackThrottle {
 private final long interval;private long last;private boolean used;
 public FeedbackThrottle(long interval){if(interval<=0)throw new IllegalArgumentException();this.interval=interval;}
 public boolean allow(long now){if(used && now-last<interval)return false;used=true;last=now;return true;}
 public void reset(){used=false;}
}
