package com.gestankbratwurst.fastchunkpregen.chunkloading;

import com.gestankbratwurst.fastchunkpregen.io.FCPIO;
import com.gestankbratwurst.fastchunkpregen.util.UtilChunk;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 02.08.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class ChunkLoadingManager implements Listener {

  public ChunkLoadingManager(final FCPIO fcpio) {
    this.loadedChunkKeys = new HashMap<>();
    this.formattedChunkWatchers = new HashSet<>();
    this.fcpio = fcpio;
    for (final World world : Bukkit.getWorlds()) {
      this.loadedChunkKeys.put(world.getUID(), new LongOpenHashSet());
      this.loadChunkData(world);
    }
    this.formattedBuffer = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", ""};
  }

  private final FCPIO fcpio;
  private final Map<UUID, LongSet> loadedChunkKeys;
  private final Set<UUID> formattedChunkWatchers;
  private final String[] formattedBuffer;

  public void addWatcher(final UUID playerID) {
    this.formattedChunkWatchers.add(playerID);
  }

  public void removeWatcher(final UUID playerID) {
    this.formattedChunkWatchers.remove(playerID);
  }

  public int getLoadedChunkAmount() {
    int amount = 0;
    for (final LongSet chunkSet : this.loadedChunkKeys.values()) {
      amount += chunkSet.size();
    }
    return amount;
  }

  public void sendFormattedChunks(final Player player) {
    final Chunk middle = player.getChunk();
    final World world = player.getWorld();
    final UUID worldID = world.getUID();
    final int r = 4;
    final int midX = middle.getX();
    final int midZ = middle.getZ();
    StringBuilder lineBuilder;

    for (int z = midZ - r; z <= midZ + r; z++) {
      lineBuilder = new StringBuilder();
      for (int x = midX - r; x <= midX + r; x++) {
        final long key = UtilChunk.getChunkKey(x, z);
        if (world.isChunkForceLoaded(x, z)) {
          if (this.loadedChunkKeys.get(worldID).contains(key)) {
            lineBuilder.append("§6");
          } else {
            lineBuilder.append("§e");
          }
        } else {
          lineBuilder.append("§7");
        }
        lineBuilder.append((x == midX && z == midZ) ? "⏺" : "■");
      }
      this.formattedBuffer[this.formattedBuffer.length - (2 * r + 1 + (z - (midZ - r))) - 1] = lineBuilder.toString();
    }
    player.sendMessage(this.formattedBuffer);
  }

  public int getLoadedChunkAmount(final UUID worldID) {
    return this.loadedChunkKeys.get(worldID).size();
  }

  private void loadChunkData(final World world) {
    final JsonObject jsonObject = this.fcpio.loadForcedWorldChunks(world.getUID());
    if (jsonObject == null) {
      return;
    }
    final LongSet keySet = this.loadedChunkKeys.get(world.getUID());
    for (final JsonElement element : jsonObject.get("Chunks").getAsJsonArray()) {
      final long key = element.getAsLong();
      keySet.add(key);
      final int[] coords = UtilChunk.getChunkCoords(key);
      world.setChunkForceLoaded(coords[0], coords[1], true);
    }
  }

  private void saveChunkData(final UUID worldID) {
    final JsonObject jsonObject = new JsonObject();
    final JsonArray jsonArray = new JsonArray();
    for (final long key : this.loadedChunkKeys.get(worldID)) {
      jsonArray.add(key);
    }
    jsonObject.add("Chunks", jsonArray);
    this.fcpio.saveForcedWorldChunks(worldID, jsonObject);
  }

  public void forceLoad(final World world, final int x, final int z) {
    world.setChunkForceLoaded(x, z, true);
    this.loadedChunkKeys.get(world.getUID()).add(UtilChunk.getChunkKey(x, z));
  }

  public boolean freeChunk(final World world, final int x, final int z) {
    if (!world.isChunkForceLoaded(x, z)) {
      return false;
    }
    world.setChunkForceLoaded(x, z, false);
    this.loadedChunkKeys.get(world.getUID()).remove(UtilChunk.getChunkKey(x, z));
    return true;
  }

  public boolean isForceLoaded(final World world, final int x, final int z) {
    return this.loadedChunkKeys.get(world.getUID()).contains(UtilChunk.getChunkKey(x, z));
  }

  public void flush() {
    for (final UUID worldID : this.loadedChunkKeys.keySet()) {
      this.saveChunkData(worldID);
    }
  }

  @EventHandler
  public void onLoad(final WorldLoadEvent event) {
    this.loadedChunkKeys.put(event.getWorld().getUID(), new LongOpenHashSet());
    this.loadChunkData(event.getWorld());
  }

  @EventHandler
  public void onLoad(final WorldUnloadEvent event) {
    this.saveChunkData(event.getWorld().getUID());
    this.loadedChunkKeys.remove(event.getWorld().getUID());
  }

  @EventHandler
  public void onLeave(final PlayerQuitEvent event) {
    this.formattedChunkWatchers.remove(event.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onChunkCross(final PlayerMoveEvent event) {
    final Player player = event.getPlayer();
    if (!this.formattedChunkWatchers.contains(player.getUniqueId())) {
      return;
    }
    if (!event.getFrom().getChunk().equals(event.getTo().getChunk())) {
      this.sendFormattedChunks(player);
    }
  }

}
