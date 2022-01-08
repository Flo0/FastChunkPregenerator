package com.gestankbratwurst.fastchunkpregen.io;

import com.gestankbratwurst.fastchunkpregen.FastChunkPregenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 12.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class FCPIO {

  @Getter
  private static FCPIO instance;

  public FCPIO(final FastChunkPregenerator plugin) {
    instance = this;
    final File mainFolder = plugin.getDataFolder();
    if (!mainFolder.exists()) {
      mainFolder.mkdir();
    }
    this.configurationFile = new File(mainFolder, "configuration.yml");
    if (!this.configurationFile.exists()) {
      plugin.saveResource("configuration.yml", false);
    }
    this.taskLoadCache = new File(mainFolder, "taskLoadCache.json");
    this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    this.forceFolder = new File(mainFolder + File.separator + "loaded");
    if (!this.forceFolder.exists()) {
      this.forceFolder.mkdirs();
    }
  }

  private final Gson gson;
  private final File taskLoadCache;
  private final File configurationFile;
  private final File forceFolder;
  @Getter
  private FCPConfiguration fcpConfiguration = null;

  public void saveForcedWorldChunks(final UUID worldID, final JsonObject jsonObject) {
    final File worldFile = new File(this.forceFolder, worldID.toString() + ".json");
    final String jsonStr = this.gson.toJson(jsonObject);
    try {
      Files.write(worldFile.toPath(), jsonStr.getBytes());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public JsonObject loadForcedWorldChunks(final UUID worldID) {
    final File worldFile = new File(this.forceFolder, worldID.toString() + ".json");
    if (!worldFile.exists()) {
      return null;
    }
    final String fileString;
    try {
      fileString = new String(Files.readAllBytes(worldFile.toPath()));
    } catch (final IOException e) {
      return null;
    }
    return this.gson.fromJson(fileString, JsonObject.class);
  }

  public void loadConfiguration() {
    final YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.configurationFile);
    this.fcpConfiguration = new FCPConfiguration(configuration);
  }

  public void saveTaskLoadCache(final JsonObject jsonObject) {
    final String jsonString = this.gson.toJson(jsonObject);

    try {
      Files.write(this.taskLoadCache.toPath(), jsonString.getBytes());
    } catch (final IOException e) {
      e.printStackTrace();
    }

  }

  public JsonObject loadTaskLoadCache() {
    if (!this.taskLoadCache.exists()) {
      return null;
    }
    final String fileString;
    try {
      fileString = new String(Files.readAllBytes(this.taskLoadCache.toPath()));
    } catch (final IOException e) {
      return null;
    }
    return this.gson.fromJson(fileString, JsonObject.class);
  }

  public void deleteTaskLoadCache() {
    this.taskLoadCache.delete();
  }

}
