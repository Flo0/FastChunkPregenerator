package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.google.gson.JsonObject;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 12.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public interface TaskLoad extends Comparable<TaskLoad> {

  void proceed();

  void onCompletion(GeneratorTask generatorTask);

  String summary(long millisDelta);

  default String loadInfo() {
    return "§e" + this.getLoadType().getDisplayName() + " §fon §e" + this.getWorldName();
  }

  String getWorldName();

  boolean isDone();

  JsonObject getAsJson();

  TaskLoadType getLoadType();

  void setPaused(boolean value);

  boolean isPaused();

  @Override
  default int compareTo(final TaskLoad other) {
    return other.getLoadType().ordinal() - this.getLoadType().ordinal();
  }

}
