package net.albiz.client.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.albiz.client.sound.PacketDiagnostics.Channel;
import net.albiz.client.sound.PacketDiagnostics.Status;
import org.junit.jupiter.api.Test;

class PacketDiagnosticsTest {
   @Test
   void statusFollowsTheFurthestStageReached() {
      PacketDiagnostics d = new PacketDiagnostics();
      assertEquals(Status.UNKNOWN, d.status(Channel.SOUND));
      d.listening(true);
      assertEquals(Status.NOT_RECEIVED, d.status(Channel.SOUND));
      d.received(Channel.SOUND);
      assertEquals(Status.RECEIVED, d.status(Channel.SOUND));
      d.decoded(Channel.SOUND);
      assertEquals(Status.DECODED, d.status(Channel.SOUND));
      d.available(Channel.SOUND);
      assertEquals(Status.AVAILABLE, d.status(Channel.SOUND));
      assertEquals(Status.NOT_RECEIVED, d.status(Channel.WORLD_EVENT));
   }

   @Test
   void lostPacketsAreReportedAsIncomplete() {
      PacketDiagnostics d = new PacketDiagnostics();
      d.listening(true);
      d.received(Channel.ENTITY_SOUND);
      d.decoded(Channel.ENTITY_SOUND);
      d.incomplete(Channel.ENTITY_SOUND);
      assertEquals(Status.INCOMPLETE, d.status(Channel.ENTITY_SOUND));
      d.received(Channel.ENTITY_SOUND);
      d.decoded(Channel.ENTITY_SOUND);
      d.available(Channel.ENTITY_SOUND);
      assertEquals(Status.INCOMPLETE, d.status(Channel.ENTITY_SOUND));
      assertEquals(2L, d.count(Channel.ENTITY_SOUND, Status.RECEIVED));
      assertTrue(d.line(Channel.ENTITY_SOUND).contains("incomplete 1"));
      d.reset();
      assertEquals(Status.NOT_RECEIVED, d.status(Channel.ENTITY_SOUND));
   }
}
