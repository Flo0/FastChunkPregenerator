package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.gestankbratwurst.fastchunkpregen.generation.generators.ChunkGenerator;
import com.gestankbratwurst.fastchunkpregen.generation.generators.SpigotGenerator;
import com.gestankbratwurst.fastchunkpregen.util.UtilChunk;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.UUID;
import org.bukkit.Bukkit;
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
public class ChunkGenerationLoad implements TaskLoad {

  public ChunkGenerationLoad(final JsonObject jsonObject) {
    this.chunkGenerator = new SpigotGenerator();
    this.filteredChunkKeys = new LongArrayList();
    final JsonArray generatedKeyArray = jsonObject.get("FilteredChunkKeys").getAsJsonArray();
    for (final JsonElement element : generatedKeyArray) {
      this.filteredChunkKeys.add(element.getAsLong());
    }
    final UUID worldID = UUID.fromString(jsonObject.get("WorldID").getAsString());
    this.world = Bukkit.getWorld(worldID);
    if (this.world == null) {
      throw new IllegalStateException("The world to be generated is not loaded.");
    }
    this.index = jsonObject.get("Index").getAsInt();
    this.done = jsonObject.get("Done").getAsBoolean();
    this.lastIndex = jsonObject.get("LastIndex").getAsInt();
  }

  public ChunkGenerationLoad(final ChunkGenerationFilter filter) {
    this.chunkGenerator = new SpigotGenerator();
    this.filteredChunkKeys = filter.getFilteredChunks();
    this.world = filter.getWorld();
  }

  private final World world;
  private final ChunkGenerator chunkGenerator;
  private final LongList filteredChunkKeys;
  private int index = 0;
  private boolean done = false;
  private int lastIndex = 0;

  private double chPs(final long millis) {
    final int chDelta = this.index - this.lastIndex;
    this.lastIndex = this.index;
    return ((int) (chDelta * 100000D / millis)) / 100D;
  }

  private double percent() {
    return ((int) ((100D / this.filteredChunkKeys.size() * (this.index)) * 100D)) / 100D;
  }

  @Override
  public void proceed() {
    if (this.index == this.filteredChunkKeys.size()) {
      this.done = true;
      return;
    }
    final long key = this.filteredChunkKeys.getLong(this.index++);
    final int[] coords = UtilChunk.getChunkCoords(key);
    this.chunkGenerator.generateSyncAt(this.world, coords[0], coords[1]);
  }

  @Override
  public void onCompletion(final GeneratorTask generatorTask) {

  }

  @Override
  public String summary(final long millisDelta) {
    return "§6[§f" + this.getWorldName() + "§6]§e Generating Chunks: §f" + this.index + " §eof §f" + this.filteredChunkKeys.size() +
        " §e[§f" + this.percent() + "%§e]§f with §e[§f" + this.chPs(millisDelta) + " Chunks/s§e]";
  }

  @Override
  public String getWorldName() {
    return this.world.getName();
  }

  @Override
  public boolean isDone() {
    return this.done;
  }

  @Override
  public JsonObject getAsJson() {
    final JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("WorldID", this.world.getUID().toString());
    jsonObject.addProperty("Index", this.index);
    jsonObject.addProperty("Done", this.done);
    jsonObject.addProperty("LastIndex", this.lastIndex);
    final JsonArray filteredChunksArray = new JsonArray();
    for (final long key : this.filteredChunkKeys) {
      filteredChunksArray.add(key);
    }
    jsonObject.add("FilteredChunkKeys", filteredChunksArray);

    return jsonObject;
  }

  @Override
  public TaskLoadType getLoadType() {
    return TaskLoadType.SYNC_CHUNK;
  }

  @Override
  public void setPaused(boolean value) {
    
  }

  @Override
  public boolean isPaused() {
    return false;
  }
}
