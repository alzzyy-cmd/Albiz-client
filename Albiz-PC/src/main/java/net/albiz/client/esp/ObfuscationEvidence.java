package net.albiz.client.esp;

/** Data-source policy, not an Orebfuscator version detector or an invented decryption bypass. */
public final class ObfuscationEvidence {
    private ObfuscationEvidence() {}
    public static boolean acceptReceivedEntry(boolean retiredByLaterUpdate) { return !retiredByLaterUpdate; }
    public static boolean acceptReceivedToken(Object retiredToken,Object currentToken) { return currentToken!=null && retiredToken!=currentToken; }
    public static boolean keepPacketMarkerOnMaskedScan(boolean orebfuscatorProfile,boolean receivedEntity) { return orebfuscatorProfile && receivedEntity; }
    public static boolean acceptChunkState(boolean orebfuscatorProfile, boolean receivedBlockEntity) {
        // Orebfuscator may substitute a believable block state; do not treat it as verified storage.
        return !orebfuscatorProfile || receivedBlockEntity;
    }
    public static boolean acceptHint(boolean enabled, boolean realData, boolean supportedType,
                                     boolean chunkLoaded, double distanceSquared, double range) {
        return enabled && !realData && supportedType && chunkLoaded && range >= 0
            && Double.isFinite(distanceSquared) && distanceSquared >= 0 && distanceSquared <= range * range;
    }
}
