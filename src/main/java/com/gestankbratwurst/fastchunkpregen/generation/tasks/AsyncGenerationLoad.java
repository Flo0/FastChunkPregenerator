package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.gestankbratwurst.fastchunkpregen.generation.generators.ChunkGenerator;
import com.gestankbratwurst.fastchunkpregen.generation.generators.PaperGenerator;
import com.gestankbratwurst.fastchunkpregen.generation.generators.PaperUrgentGenerator;
import com.gestankbratwurst.fastchunkpregen.io.FCPConfiguration;
import com.gestankbratwurst.fastchunkpregen.io.FCPIO;
import com.gestankbratwurst.fastchunkpregen.util.UtilChunk;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
public class AsyncGenerationLoad implements TaskLoad {

  public AsyncGenerationLoad(final JsonObject jsonObject) {
    final FCPConfiguration config = FCPIO.getInstance().getFcpConfiguration();
    final boolean highPrio = config.isHighAsyncPrio();
    this.unsafeCalls = config.isUnsafeAsync();
    this.parallelAmount = config.getMaxParallelAsyncCalls();

    if (this.unsafeCalls) {
      throw new UnsupportedOperationException("This feature is no longer available.");
    } else {
      this.chunkGenerator = highPrio ? new PaperUrgentGenerator() : new PaperGenerator();
    }

    this.chunksGenerated = new AtomicInteger(jsonObject.get("ChunksGenerated").getAsInt());

    final UUID worldID = UUID.fromString(jsonObject.get("WorldID").getAsString());
    this.world = Bukkit.getWorld(worldID);
    if (this.world == null) {
      throw new IllegalStateException("The world to be generated is not loaded.");
    }

    this.filteredChunkKeys = new LongArrayList();
    final JsonArray generatedKeyArray = jsonObject.get("FilteredChunkKeys").getAsJsonArray();
    for (final JsonElement element : generatedKeyArray) {
      this.filteredChunkKeys.add(element.getAsLong());
    }

    this.index = new AtomicInteger(jsonObject.get("Index").getAsInt());
    this.lastAmount = jsonObject.get("LastAmount").getAsInt();
    this.done = jsonObject.get("Done").getAsBoolean();
    this.threadsDone = new AtomicInteger(this.parallelAmount);
  }

  public AsyncGenerationLoad(final ChunkGenerationFilter filter, final int parallelAmount) {
    final FCPConfiguration config = FCPIO.getInstance().getFcpConfiguration();
    final boolean highPrio = config.isHighAsyncPrio();
    this.unsafeCalls = config.isUnsafeAsync();
    this.parallelAmount = config.getMaxParallelAsyncCalls();
    if (this.unsafeCalls) {
      throw new UnsupportedOperationException("This feature is no longer available.");
    } else {
      this.chunkGenerator = highPrio ? new PaperUrgentGenerator() : new PaperGenerator();
    }
    this.filteredChunkKeys = filter.getFilteredChunks();
    this.world = filter.getWorld();
    this.threadsDone = new AtomicInteger(parallelAmount);
    this.chunksGenerated = new AtomicInteger(0);
    this.index = new AtomicInteger(0);
  }

  private final boolean unsafeCalls;
  private final World world;
  private final ChunkGenerator chunkGenerator;
  private final LongList filteredChunkKeys;
  private final AtomicInteger index;
  private int lastAmount = 0;
  private boolean done = false;
  private final AtomicInteger threadsDone;
  private final AtomicInteger chunksGenerated;
  private final int parallelAmount;

  private double percent() {
    return ((int) ((100D / this.filteredChunkKeys.size() * (this.index.get())) * 100D)) / 100D;
  }

  private double chPs(final long millis) {
    final int generated = this.chunksGenerated.get();
    final int chDelta = generated - this.lastAmount;
    this.lastAmount = generated;
    return ((int) (chDelta * 100000D / millis)) / 100D;
  }

  private long next() {
    return this.filteredChunkKeys.getLong(this.index.getAndIncrement());
  }

  @Override
  public void proceed() {
    if (this.index.get() == this.filteredChunkKeys.size()) {
      if (this.threadsDone.get() == this.parallelAmount) {
        this.done = true;
      }
      return;
    }

    final int threadsToStart = this.threadsDone.get();
    for (int i = 0; i < threadsToStart; i++) {
      final long next = this.next();
      final int[] coords = UtilChunk.getChunkCoords(next);
      this.chunkGenerator.generateAsyncAt(this.world, coords[0], coords[1]).thenRun(() -> {
        this.threadsDone.incrementAndGet();
        this.chunksGenerated.incrementAndGet();
      });

      this.threadsDone.decrementAndGet();
      if (this.index.get() == this.filteredChunkKeys.size()) {
        return;
      }
    }
  }

  @Override
  public void onCompletion(final GeneratorTask generatorTask) {

  }

  @Override
  public String summary(final long millisDelta) {
    return "§6[§f" + this.getWorldName() + "§6]§e Generating Chunks: §f" + this.chunksGenerated.get() + " §eof §f" + this.filteredChunkKeys
        .size() +
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

    jsonObject.addProperty("ChunksGenerated", this.chunksGenerated);
    jsonObject.addProperty("Index", this.index);
    jsonObject.addProperty("LastAmount", this.lastAmount);
    jsonObject.addProperty("Done", this.done);
    jsonObject.addProperty("WorldID", this.world.getUID().toString());

    final JsonArray filteredChunksArray = new JsonArray();
    for (final long key : this.filteredChunkKeys) {
      filteredChunksArray.add(key);
    }
    jsonObject.add("FilteredChunkKeys", filteredChunksArray);

    return jsonObject;
  }

  @Override
  public TaskLoadType getLoadType() {
    return TaskLoadType.ASYNC_CHUNK;
  }

  @Override
  public void setPaused(boolean value) {
    
  }

  @Override
  public boolean isPaused() {
    return false;
  }

}
