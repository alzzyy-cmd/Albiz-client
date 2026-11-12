package net.albiz.client.port.soberui.modules;
public class ChunkFinder extends net.albiz.client.port.soberui.module.Module { public ChunkFinder(){super("ChunkFinder","",net.albiz.client.port.soberui.module.Category.CLIENT);}  public it.unimi.dsi.fastutil.longs.LongSet radarChunks(){return it.unimi.dsi.fastutil.longs.LongSet.of();} public int radarColor(){return 0xffef3434;} }
