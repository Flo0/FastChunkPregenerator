package com.gestankbratwurst.fastchunkpregen.util;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.function.Consumer;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 12.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class UtilChunk {

  public static int[] getChunkCoords(final long chunkKey) {
    final int x = ((int) chunkKey);
    final int z = (int) (chunkKey >> 32);
    return new int[]{x, z};
  }

  public static int apply(final Chunk middle, final int radius, final Consumer<Chunk> consumer) {
    int amount = 0;
    for (int x = middle.getX() - radius; x <= middle.getX() + radius; x++) {
      for (int z = middle.getZ() - radius; z <= middle.getZ() + radius; z++) {
        consumer.accept(middle.getWorld().getChunkAt(x, z));
        amount++;
      }
    }
    return amount;
  }

  public static LongSet fetch(final int mx, final int mz, final int radius) {
    final LongSet chunkKeys = new LongOpenHashSet();
    for (int x = mx - radius; x <= mx + radius; x++) {
      for (int z = mz - radius; z <= mz + radius; z++) {
        chunkKeys.add(getChunkKey(x, z));
      }
    }
    return chunkKeys;
  }

  public static long getChunkKey(final int x, final int z) {
    return (long) x & 0xffffffffL | ((long) z & 0xffffffffL) << 32;
  }

  public static long getChunkKey(final Chunk chunk) {
    return (long) chunk.getX() & 0xffffffffL | ((long) chunk.getZ() & 0xffffffffL) << 32;
  }

  public static Chunk keyToChunk(final World world, final long chunkID) {
    Preconditions.checkArgument(world != null, "World cannot be null");
    return world.getChunkAt((int) chunkID, (int) (chunkID >> 32));
  }

  public static boolean isChunkLoaded(final Location loc) {
    final int chunkX = loc.getBlockX() >> 4;
    final int chunkZ = loc.getBlockZ() >> 4;
    final World world = loc.getWorld();
    if (world == null) {
      return false;
    }
    return world.isChunkLoaded(chunkX, chunkZ);
  }

  public static long getChunkKey(final Location loc) {
    return getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
  }

  public static long getChunkKey(final ChunkSnapshot chunk) {
    return (long) chunk.getX() & 0xffffffffL | ((long) chunk.getZ() & 0xffffffffL) << 32;
  }

}
