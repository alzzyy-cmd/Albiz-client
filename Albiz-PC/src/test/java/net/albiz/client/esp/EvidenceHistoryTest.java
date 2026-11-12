package net.albiz.client.esp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EvidenceHistoryTest {
 @Test void historyExpiresWithoutClaimingCurrentExistence() {
  var history=new EvidenceHistory<String>(3,100);history.remember(1,"chest",0);
  int[] count={0};history.forEachAlive(99,(key,value)->count[0]++);assertEquals(1,count[0]);
  history.forEachAlive(100,(key,value)->fail("expired"));assertEquals(0,history.size());
 }
 @Test void boundedHistoryEvictsOldestAndFreshDataCanForgetIt() {
  var history=new EvidenceHistory<String>(2,100);history.remember(1,"a",0);history.remember(2,"b",1);history.remember(3,"c",2);
  assertEquals(2,history.size());history.forEachAlive(3,(key,value)->assertNotEquals(1L,key));history.forget(2);assertEquals(1,history.size());history.clear();assertEquals(0,history.size());
 }
 @Test void freshEquivalentTokenIsNotMistakenForRetiredData() {
  Object old=new String("chest"),fresh=new String("chest");assertEquals(old,fresh);
  assertFalse(ObfuscationEvidence.acceptReceivedToken(old,old));assertTrue(ObfuscationEvidence.acceptReceivedToken(old,fresh));assertFalse(ObfuscationEvidence.acceptReceivedToken(null,null));
 }
 @Test void maskedChunkScanDoesNotDeleteReceivedEntityEvidence() {
  assertTrue(ObfuscationEvidence.keepPacketMarkerOnMaskedScan(true,true));
  assertFalse(ObfuscationEvidence.keepPacketMarkerOnMaskedScan(true,false));assertFalse(ObfuscationEvidence.keepPacketMarkerOnMaskedScan(false,true));
 }
}
