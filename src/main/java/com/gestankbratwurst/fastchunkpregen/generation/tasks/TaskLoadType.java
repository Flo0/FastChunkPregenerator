package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.google.gson.JsonObject;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 26.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
@AllArgsConstructor
public enum TaskLoadType {

  COORD_FETCHER(CoordinateFetcher::new, "Fetching Coordinates"),
  REGION_FETCHER(RegionFileFetcher::new, "Fetching Region Files"),
  CHUNK_FILTER(ChunkGenerationFilter::new, "Filtering Chunks"),
  SYNC_CHUNK(ChunkGenerationLoad::new, "Generating Chunks (Sync)"),
  ASYNC_CHUNK(AsyncGenerationLoad::new, "Generating Chunks (Async)");

  private final Function<JsonObject, TaskLoad> loadSupplier;
  @Getter
  private final String displayName;

  public static TaskLoad deserializeLoad(final JsonObject jsonObject) {
    final TaskLoadType type = TaskLoadType.valueOf(jsonObject.get("LoadType").getAsString());
    return type.loadSupplier.apply(jsonObject.get("Data").getAsJsonObject());
  }

  public static JsonObject serializeLoad(final TaskLoad load) {
    final JsonObject jsonObject = new JsonObject();

    final TaskLoadType type = load.getLoadType();
    jsonObject.addProperty("LoadType", type.toString());
    jsonObject.add("Data", load.getAsJson());

    return jsonObject;
  }

}
