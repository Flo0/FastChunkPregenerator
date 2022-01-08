package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.gestankbratwurst.fastchunkpregen.util.UtilChunk;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
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
public class ChunkGenerationFilter implements TaskLoad {

  public ChunkGenerationFilter(final JsonObject jsonObject) {
    this.generatedKeys = new LongLinkedOpenHashSet();
    final JsonArray generatedChunkArray = jsonObject.get("GeneratedChunks").getAsJsonArray();
    for (final JsonElement element : generatedChunkArray) {
      this.generatedKeys.add(element.getAsLong());
    }

    this.world = Bukkit.getWorld(UUID.fromString(jsonObject.get("WorldID").getAsString()));
    if (this.world == null) {
      throw new IllegalStateException("Generated world is not loaded.");
    }

    this.checkableLocations = new LongLinkedOpenHashSet();
    final JsonArray checkableLocationArray = jsonObject.get("CheckableLocations").getAsJsonArray();
    for (final JsonElement element : checkableLocationArray) {
      this.checkableLocations.add(element.getAsLong());
    }

    this.filteredChunks = new LongArrayList();
    final JsonArray filteredChunkArray = jsonObject.get("FilteredChunks").getAsJsonArray();
    for (final JsonElement element : filteredChunkArray) {
      this.filteredChunks.add(element.getAsLong());
    }
    this.done = jsonObject.get("Done").getAsBoolean();
    this.index = jsonObject.get("Index").getAsInt();
    this.lastIndex = jsonObject.get("LastIndex").getAsInt();
    this.initialSize = jsonObject.get("InitialSize").getAsInt();
  }

  public ChunkGenerationFilter(final RegionFileFetcher regionFileFetcher) {
    this.checkableLocations = regionFileFetcher.getCheckableLocations();
    this.filteredChunks = new LongArrayList(this.checkableLocations.size());
    this.world = Bukkit.getWorld(regionFileFetcher.getWorldID());
    if (this.world == null) {
      throw new IllegalStateException("Generated world is not loaded.");
    }
    this.generatedKeys = regionFileFetcher.getGeneratedKeys();
    this.initialSize = this.generatedKeys.size();
  }

  @Getter(AccessLevel.PACKAGE)
  private final LongList filteredChunks;
  @Getter(AccessLevel.PACKAGE)
  private final World world;
  private final LongSet checkableLocations;
  private final LongLinkedOpenHashSet generatedKeys;
  private boolean done = false;
  private int index = 0;
  private int lastIndex = 0;
  private final int initialSize;

  private double percent() {
    return ((int) ((100D / this.initialSize * (this.index)) * 100D)) / 100D;
  }

  private double chPs(final long millis) {
    final int chDelta = this.index - this.lastIndex;
    this.lastIndex = this.index;
    return ((int) (chDelta * 100000D / millis)) / 100D;
  }

  @Override
  public void proceed() {
    if (this.generatedKeys.size() == 0) {
      this.done = true;
      return;
    }
    final long chunkKey = this.generatedKeys.removeFirstLong();
    this.index++;
    final int[] coordinates = UtilChunk.getChunkCoords(chunkKey);
    if (this.checkableLocations.remove(chunkKey)) {
      if (!this.world.isChunkGenerated(coordinates[0], coordinates[1])) {
        this.filteredChunks.add(chunkKey);
      }
    } else {
      this.filteredChunks.add(chunkKey);
    }
  }

  @Override
  public void onCompletion(final GeneratorTask generatorTask) {
    if (generatorTask.isAsync()) {
      generatorTask.queueTaskLoad(new AsyncGenerationLoad(this, generatorTask.getMaxAsyncParallels()));
    } else {
      generatorTask.queueTaskLoad(new ChunkGenerationLoad(this));
    }
  }

  @Override
  public String summary(final long millisDelta) {
    return "§6[§f" + this.getWorldName() + "§6]§e Filtering pre-generated: §f" + this.index + " §eof §f" + this.initialSize
        + " §e[§f" + this.percent() + "%§e]§f with §e[§f" + this.chPs(millisDelta) + " Chunks/s§e]";
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

    final JsonArray generatedChunkArray = new JsonArray();
    for (final long key : this.generatedKeys) {
      generatedChunkArray.add(key);
    }
    jsonObject.add("GeneratedChunks", generatedChunkArray);

    final JsonArray checkableLocationArray = new JsonArray();
    for (final long key : this.checkableLocations) {
      checkableLocationArray.add(key);
    }
    jsonObject.add("CheckableLocations", checkableLocationArray);

    final JsonArray filteredChunkArray = new JsonArray();
    for (final long key : this.filteredChunks) {
      filteredChunkArray.add(key);
    }
    jsonObject.add("FilteredChunks", filteredChunkArray);

    jsonObject.addProperty("Done", this.done);
    jsonObject.addProperty("Index", this.index);
    jsonObject.addProperty("LastIndex", this.lastIndex);
    jsonObject.addProperty("InitialSize", this.initialSize);
    jsonObject.addProperty("WorldID", this.world.getUID().toString());

    return jsonObject;
  }

  @Override
  public TaskLoadType getLoadType() {
    return TaskLoadType.CHUNK_FILTER;
  }

  @Override
  public void setPaused(boolean value) {
    
  }

  @Override
  public boolean isPaused() {
    return false;
  }

}
