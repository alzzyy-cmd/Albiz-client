package net.albiz.client.esp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ObfuscationEvidenceTest {
 @Test void randomizedChunkMaterialIsNotVerifiedStorage() { assertFalse(ObfuscationEvidence.acceptChunkState(true,false)); }
 @Test void receivedBlockEntityEvidenceIsRetained() { assertTrue(ObfuscationEvidence.acceptChunkState(true,true)); }
 @Test void vanillaProfileKeepsOrdinaryChunkScan() { assertTrue(ObfuscationEvidence.acceptChunkState(false,false)); }
 @Test void hintsRequireOptInLoadedChunkAndRange() {
  assertTrue(ObfuscationEvidence.acceptHint(true,false,true,true,100,32));
  assertFalse(ObfuscationEvidence.acceptHint(false,false,true,true,100,32));
  assertFalse(ObfuscationEvidence.acceptHint(true,false,true,false,100,32));
  assertFalse(ObfuscationEvidence.acceptHint(true,false,true,true,2000,32));
  assertFalse(ObfuscationEvidence.acceptHint(true,false,false,true,100,32));
 }
 @Test void verifiedDataDoesNotAlsoBecomeUnconfirmedHint() { assertFalse(ObfuscationEvidence.acceptHint(true,true,true,true,1,32)); }
 @Test void invalidDistancesCannotPassHintPolicy() {
  assertFalse(ObfuscationEvidence.acceptHint(true,false,true,true,Double.NaN,32));
  assertFalse(ObfuscationEvidence.acceptHint(true,false,true,true,-1,32));
 }
 @Test void oldReceivedDataCannotResurrectRetiredMarker() {
  assertFalse(ObfuscationEvidence.acceptReceivedEntry(true));
  assertTrue(ObfuscationEvidence.acceptReceivedEntry(false));
 }
}
