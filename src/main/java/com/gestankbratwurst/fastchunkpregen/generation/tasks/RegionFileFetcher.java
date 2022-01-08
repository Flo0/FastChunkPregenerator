package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.gestankbratwurst.fastchunkpregen.util.UtilChunk;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 12.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class RegionFileFetcher implements TaskLoad {

  public RegionFileFetcher(final JsonObject jsonObject) {
    this.locationIndex = jsonObject.get("LocationIndex").getAsInt();
    this.fileIndex = jsonObject.get("FileIndex").getAsInt();
    this.cursorX = jsonObject.get("CursorX").getAsInt();
    this.cursorZ = jsonObject.get("CursorZ").getAsInt();
    this.currentRegionX = jsonObject.get("CurrentRegionX").getAsInt();
    this.currentRegionZ = jsonObject.get("CurrentRegionZ").getAsInt();
    this.done = jsonObject.get("Done").getAsBoolean();

    this.worldID = UUID.fromString(jsonObject.get("WorldID").getAsString());

    final JsonArray fileArray = jsonObject.get("RegionFiles").getAsJsonArray();
    this.regionFiles = new File[fileArray.size()];
    for (int i = 0; i < fileArray.size(); i++) {
      this.regionFiles[i] = new File(fileArray.get(i).getAsString());
    }
    this.target = this.regionFiles.length * 32 * 32;

    final JsonArray checkableArray = jsonObject.get("CheckableLocations").getAsJsonArray();
    this.checkableLocations = new LongLinkedOpenHashSet(checkableArray.size());
    for (int i = 0; i < checkableArray.size(); i++) {
      this.checkableLocations.add(checkableArray.get(i).getAsLong());
    }

    final JsonArray generatedKeyArray = jsonObject.get("GeneratedKeys").getAsJsonArray();
    this.generatedKeys = new LongLinkedOpenHashSet(generatedKeyArray.size());
    for (int i = 0; i < generatedKeyArray.size(); i++) {
      this.generatedKeys.add(generatedKeyArray.get(i).getAsLong());
    }
  }

  public RegionFileFetcher(final CoordinateFetcher coordinationFetcher) {
    this.worldID = coordinationFetcher.getWorldID();
    final World world = Bukkit.getWorld(coordinationFetcher.getWorldID());
    if (world == null) {
      throw new IllegalStateException("Generating world is not loaded.");
    }
    final Environment environment = world.getEnvironment();
    final File regionFolder;
    switch (environment) {
      case NORMAL:
        regionFolder = new File(world.getWorldFolder() + File.separator + "region");
        break;
      case NETHER:
        regionFolder = new File(world.getWorldFolder() + File.separator + "DIM-1" + File.separator + "region");
        break;
      case THE_END:
        regionFolder = new File(world.getWorldFolder() + File.separator + "DIM1" + File.separator + "region");
        break;
      default:
        throw new UnsupportedOperationException("World environment must be one of: NORMAL, NETHER, END");
    }
    if (!regionFolder.exists()) {
      throw new IllegalStateException("World folder of unsupported dimension: " + regionFolder);
    }
    this.regionFiles = regionFolder.listFiles();
    this.target = this.regionFiles.length * 32 * 32;
    this.checkableLocations = new LongOpenHashSet(this.target);
    this.generatedKeys = coordinationFetcher.getLocations();
  }

  @Getter(AccessLevel.PACKAGE)
  private final UUID worldID;
  @Getter(AccessLevel.PACKAGE)
  private final LongSet checkableLocations;
  @Getter(AccessLevel.PACKAGE)
  private final LongLinkedOpenHashSet generatedKeys;
  private final File[] regionFiles;
  private final int target;
  private int locationIndex;
  private int fileIndex;
  private int cursorX;
  private int cursorZ = 32;
  private int currentRegionX;
  private int currentRegionZ;
  private boolean done = false;

  @Override
  public void proceed() {
    if (this.fileIndex == this.regionFiles.length) {
      this.done = true;
      return;
    }
    if (this.cursorZ == 32) {
      final String[] split = this.regionFiles[this.fileIndex++].getName().split("\\.");
      this.currentRegionX = Integer.parseInt(split[1]);
      this.currentRegionZ = Integer.parseInt(split[2]);
      this.cursorX = 0;
      this.cursorZ = 0;
    }
    final long chunkKey = UtilChunk.getChunkKey(this.currentRegionX * 32 + this.cursorX++, this.currentRegionZ * 32 + this.cursorZ);
    this.checkableLocations.add(chunkKey);
    this.locationIndex++;
    if (this.cursorX == 32) {
      this.cursorZ++;
      this.cursorX = 0;
    }
  }

  private double percent() {
    return ((int) ((100D / this.target * (this.locationIndex)) * 100D)) / 100D;
  }

  @Override
  public void onCompletion(final GeneratorTask generatorTask) {
    generatorTask.queueTaskLoad(new ChunkGenerationFilter(this));
  }

  @Override
  public String summary(final long millisDelta) {
    return "§6[§f" + this.getWorldName() + "§6]§e Fetching world chunks: §f" + this.locationIndex + " §eof §f" + this.target + " §e[§f"
        + this
        .percent() + "%§e]";
  }

  @Override
  public String getWorldName() {
    return Bukkit.getWorld(this.worldID).getName();
  }

  @Override
  public boolean isDone() {
    return this.done;
  }


  @Override
  public JsonObject getAsJson() {
    final JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("LocationIndex", this.locationIndex);
    jsonObject.addProperty("FileIndex", this.fileIndex);
    jsonObject.addProperty("CursorX", this.cursorX);
    jsonObject.addProperty("CursorZ", this.cursorZ);
    jsonObject.addProperty("CurrentRegionX", this.currentRegionX);
    jsonObject.addProperty("CurrentRegionZ", this.currentRegionZ);
    jsonObject.addProperty("Done", this.done);
    jsonObject.addProperty("WorldID", this.worldID.toString());

    final JsonArray fileArray = new JsonArray();
    for (final File file : this.regionFiles) {
      fileArray.add(file.getAbsolutePath());
    }
    jsonObject.add("RegionFiles", fileArray);

    final JsonArray checkableArray = new JsonArray();
    for (final long key : this.checkableLocations) {
      checkableArray.add(key);
    }
    jsonObject.add("CheckableLocations", checkableArray);

    final JsonArray generatedKeyArray = new JsonArray();
    for (final long key : this.generatedKeys) {
      generatedKeyArray.add(key);
    }
    jsonObject.add("GeneratedKeys", generatedKeyArray);

    return jsonObject;
  }

  @Override
  public TaskLoadType getLoadType() {
    return TaskLoadType.REGION_FETCHER;
  }

  @Override
  public void setPaused(boolean value) {
    
  }

  @Override
  public boolean isPaused() {
    return false;
  }

}
