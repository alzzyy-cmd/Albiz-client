package net.albiz.client.suschunk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.albiz.client.suschunk.EvidenceBook.Confidence;
import org.junit.jupiter.api.Test;

class EvidenceBookTest {
   private static int[] counts(Object... pairs) {
      int[] counts = new int[EvidenceType.values().length];
      for (int i = 0; i < pairs.length; i += 2) {
         counts[((EvidenceType)pairs[i]).ordinal()] = (Integer)pairs[i + 1];
      }
      return counts;
   }

   @Test
   void rescanReplacesInsteadOfAccumulating() {
      EvidenceBook book = new EvidenceBook(8);
      book.setScan(1L, counts(EvidenceType.STORAGE, 2), 10, 12, 0L);
      book.setScan(1L, counts(EvidenceType.STORAGE, 2), 10, 12, 1L);
      assertEquals(6.0F, book.score(1L), 1.0E-6F);
      assertEquals(2, book.get(1L).count(EvidenceType.STORAGE));
   }

   @Test
   void countsAreCappedPerType() {
      EvidenceBook book = new EvidenceBook(8);
      book.setScan(1L, counts(EvidenceType.GROWN_KELP, 500), -40, -20, 0L);
      assertEquals(EvidenceType.GROWN_KELP.cap() * EvidenceType.GROWN_KELP.defaultWeight(), book.score(1L), 1.0E-6F);
      assertEquals(-40, book.get(1L).minY());
      assertEquals(-20, book.get(1L).maxY());
   }

   @Test
   void weightsAreConfigurable() {
      EvidenceBook book = new EvidenceBook(8);
      book.setScan(1L, counts(EvidenceType.SPAWNER, 1, EvidenceType.STORAGE, 1), 0, 0, 0L);
      assertEquals(5.0F, book.score(1L), 1.0E-6F);
      book.weight(EvidenceType.SPAWNER, 0.0F);
      assertEquals(3.0F, book.score(1L), 1.0E-6F);
      book.weight(EvidenceType.STORAGE, -5.0F);
      assertEquals(0.0F, book.score(1L), 1.0E-6F);
   }

   @Test
   void singleEvidenceTypeNeverReachesHigh() {
      EvidenceBook book = new EvidenceBook(8);
      book.setScan(1L, counts(EvidenceType.GROWN_KELP, 12), 0, 0, 0L);
      // score 12, threshold 6: twice the threshold but only one kind.
      assertEquals(Confidence.MEDIUM, book.confidence(book.get(1L), 6.0F));
      book.setScan(2L, counts(EvidenceType.GROWN_KELP, 12, EvidenceType.STORAGE, 2, EvidenceType.REDSTONE, 1), 0, 0, 0L);
      assertEquals(Confidence.HIGH, book.confidence(book.get(2L), 10.0F));
      book.setScan(3L, counts(EvidenceType.GROWN_KELP, 5), 0, 0, 0L);
      assertEquals(Confidence.LOW, book.confidence(book.get(3L), 10.0F));
      book.setScan(4L, counts(EvidenceType.AMETHYST_CLUSTER, 4), 0, 0, 0L);
      assertEquals(Confidence.NONE, book.confidence(book.get(4L), 10.0F));
      assertEquals(Confidence.NONE, book.confidence(null, 10.0F));
      assertFalse(book.flagged(book.get(3L), 10.0F));
      assertTrue(book.flagged(book.get(2L), 10.0F));
   }

   @Test
   void zeroWeightTypesDoNotCountAsIndependentKinds() {
      EvidenceBook book = new EvidenceBook(8);
      book.weight(EvidenceType.AMETHYST_CLUSTER, 0.0F);
      book.weight(EvidenceType.GROWN_VINES, 0.0F);
      book.setScan(1L, counts(EvidenceType.GROWN_KELP, 12, EvidenceType.AMETHYST_CLUSTER, 9, EvidenceType.GROWN_VINES, 9), 0, 0, 0L);
      assertEquals(Confidence.MEDIUM, book.confidence(book.get(1L), 6.0F));
   }

   @Test
   void alertsOncePerEvidenceAndRespectCooldown() {
      EvidenceBook book = new EvidenceBook(8);
      book.setScan(1L, counts(EvidenceType.STORAGE, 4, EvidenceType.REDSTONE, 2), 0, 0, 0L);
      assertTrue(book.shouldAlert(1L, 10.0F, 1000L, 60000L));
      assertFalse(book.shouldAlert(1L, 10.0F, 2000L, 60000L));
      assertFalse(book.shouldAlert(1L, 10.0F, 999999L, 60000L));
      // Evidence disappears, then returns inside the cooldown: still silent.
      book.setScan(1L, counts(EvidenceType.STORAGE, 1), 0, 0, 3000L);
      assertFalse(book.shouldAlert(1L, 10.0F, 3000L, 60000L));
      book.setScan(1L, counts(EvidenceType.STORAGE, 4, EvidenceType.REDSTONE, 2), 0, 0, 4000L);
      assertFalse(book.shouldAlert(1L, 10.0F, 4000L, 60000L));
      // After the cooldown the returned evidence alerts again.
      assertTrue(book.shouldAlert(1L, 10.0F, 70000L, 60000L));
      assertFalse(book.shouldAlert(99L, 10.0F, 0L, 0L));
   }

   @Test
   void liveEvidenceSurvivesRescanAndExpires() {
      EvidenceBook book = new EvidenceBook(8);
      for (int i = 0; i < 100; i++) {
         book.addLive(1L, EvidenceType.BLOCK_CHANGE, -30, 10L);
      }
      assertEquals(EvidenceType.BLOCK_CHANGE.cap() * 2, book.get(1L).count(EvidenceType.BLOCK_CHANGE));
      book.setScan(1L, counts(), 0, 0, 20L, EvidenceType.BLOCK_CHANGE, EvidenceType.SOUND);
      assertEquals(EvidenceType.BLOCK_CHANGE.cap() * 2, book.get(1L).count(EvidenceType.BLOCK_CHANGE));
      assertEquals(-30, book.get(1L).minY());
      book.expireLive(1000L, 5000L, EvidenceType.BLOCK_CHANGE, EvidenceType.SOUND);
      assertEquals(1, book.size());
      book.expireLive(6000L, 5000L, EvidenceType.BLOCK_CHANGE, EvidenceType.SOUND);
      assertEquals(0, book.size());
   }

   @Test
   void emptyScanRemovesChunkAndDoesNotCreateOne() {
      EvidenceBook book = new EvidenceBook(8);
      book.setScan(1L, counts(), 0, 0, 0L);
      assertNull(book.get(1L));
      book.setScan(1L, counts(EvidenceType.SPAWNER, 1), 0, 0, 0L);
      book.setScan(1L, counts(), 0, 0, 1L);
      assertNull(book.get(1L));
   }

   @Test
   void boundedAndClearable() {
      EvidenceBook book = new EvidenceBook(3);
      for (long i = 0; i < 10; i++) {
         book.setScan(i, counts(EvidenceType.SPAWNER, 1), 0, 0, 0L);
      }
      assertEquals(3, book.size());
      assertNull(book.get(0L));
      book.clear();
      assertEquals(0, book.size());
   }

   @Test
   void explanationListsStrongestFirst() {
      EvidenceBook book = new EvidenceBook(8);
      book.setScan(1L, counts(EvidenceType.GROWN_KELP, 3, EvidenceType.STORAGE, 3, EvidenceType.SPAWNER, 1), 0, 0, 0L);
      assertEquals("Storage blocks x3 (+9.0), Fully grown kelp x3 (+3.0), +1", book.explain(book.get(1L), 2));
      assertEquals("", book.explain(null, 3));
   }

   @Test
   void exportHasHeaderAndNegativeCoordinates() {
      EvidenceBook book = new EvidenceBook(8);
      long chunk = (long)-2 & 0xFFFFFFFFL | ((long)3 & 0xFFFFFFFFL) << 32;
      book.setScan(chunk, counts(EvidenceType.STORAGE, 4, EvidenceType.REDSTONE, 2), -50, -48, 0L);
      List<String> lines = book.export(10.0F);
      assertEquals(2, lines.size());
      assertTrue(lines.get(0).startsWith("chunk_x\tchunk_z\tblock_x\tblock_z\tscore\tconfidence\tmin_y\tmax_y\tgrown_kelp"));
      assertTrue(lines.get(1).startsWith("-2\t3\t-32\t48\t20.0\tMEDIUM\t-50\t-48\t"));
      assertEquals(lines.get(0).split("\t").length, lines.get(1).split("\t").length);
   }
}
