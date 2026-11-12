package net.albiz.client.sound;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Honest per-stage packet accounting for the sound pipeline.
 *
 * <p>Counters are written from the network thread and read from the client thread, hence atomic.
 * Nothing here can tell what the server chose not to send: that is {@link Status#NOT_RECEIVED}
 * and is only ever reported as "no packet of this type arrived", never as "nothing happened".
 */
public final class PacketDiagnostics {
   public enum Channel {
      SOUND("Sound packets"),
      ENTITY_SOUND("Entity sound packets"),
      WORLD_EVENT("World event packets"),
      BLOCK_ACTION("Block action packets"),
      ENTITY_SPAWN("Entity spawn packets");

      private final String label;

      Channel(String label) {
         this.label = label;
      }

      public String label() {
         return this.label;
      }
   }

   public enum Status {
      /** The packet object reached the client connection. */
      RECEIVED,
      /** Its fields were read without an exception. */
      DECODED,
      /** It was handed to the client thread and can be shown. */
      AVAILABLE,
      /** It was read but something needed for display was missing (unknown entity, queue full, decode error). */
      INCOMPLETE,
      /** No packet of this type has arrived since the counters were reset. */
      NOT_RECEIVED,
      /** The module was not listening, so nothing can be said. */
      UNKNOWN
   }

   private static final int CHANNELS = Channel.values().length;
   private final AtomicLongArray received = new AtomicLongArray(CHANNELS);
   private final AtomicLongArray decoded = new AtomicLongArray(CHANNELS);
   private final AtomicLongArray available = new AtomicLongArray(CHANNELS);
   private final AtomicLongArray incomplete = new AtomicLongArray(CHANNELS);
   private volatile boolean listening;

   public void listening(boolean value) {
      this.listening = value;
   }

   public void received(Channel channel) {
      this.received.incrementAndGet(channel.ordinal());
   }

   public void decoded(Channel channel) {
      this.decoded.incrementAndGet(channel.ordinal());
   }

   public void available(Channel channel) {
      this.available.incrementAndGet(channel.ordinal());
   }

   public void incomplete(Channel channel) {
      this.incomplete.incrementAndGet(channel.ordinal());
   }


   public long count(Channel channel, Status status) {
      return switch (status) {
         case RECEIVED -> this.received.get(channel.ordinal());
         case DECODED -> this.decoded.get(channel.ordinal());
         case AVAILABLE -> this.available.get(channel.ordinal());
         case INCOMPLETE -> this.incomplete.get(channel.ordinal());
         case NOT_RECEIVED, UNKNOWN -> 0L;
      };
   }

   /** The furthest stage the channel has reached. */
   public Status status(Channel channel) {
      if (!this.listening && this.received.get(channel.ordinal()) == 0L) {
         return Status.UNKNOWN;
      }
      if (this.received.get(channel.ordinal()) == 0L) {
         return Status.NOT_RECEIVED;
      }
      if (this.available.get(channel.ordinal()) > 0L) {
         return this.incomplete.get(channel.ordinal()) > 0L && this.available.get(channel.ordinal()) < this.received.get(channel.ordinal())
            ? Status.INCOMPLETE
            : Status.AVAILABLE;
      }
      if (this.incomplete.get(channel.ordinal()) > 0L) {
         return Status.INCOMPLETE;
      }
      return this.decoded.get(channel.ordinal()) > 0L ? Status.DECODED : Status.RECEIVED;
   }

   public String line(Channel channel) {
      int i = channel.ordinal();
      return channel.label() + ": " + this.status(channel) + " (received " + this.received.get(i) + ", decoded " + this.decoded.get(i) + ", available "
         + this.available.get(i) + ", incomplete " + this.incomplete.get(i) + ")";
   }

   public void reset() {
      for (int i = 0; i < CHANNELS; i++) {
         this.received.set(i, 0L);
         this.decoded.set(i, 0L);
         this.available.set(i, 0L);
         this.incomplete.set(i, 0L);
      }
   }
}
