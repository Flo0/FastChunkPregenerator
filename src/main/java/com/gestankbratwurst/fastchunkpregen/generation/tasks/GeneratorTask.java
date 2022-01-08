package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.gestankbratwurst.fastchunkpregen.FastChunkPregenerator;
import com.gestankbratwurst.fastchunkpregen.generation.GeneratorManager;
import com.gestankbratwurst.fastchunkpregen.generation.NotificationType;
import com.gestankbratwurst.fastchunkpregen.io.FCPConfiguration;
import com.gestankbratwurst.fastchunkpregen.io.FCPIO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import lombok.Getter;
import lombok.Setter;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 12.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class GeneratorTask implements Runnable {

  public GeneratorTask(final FastChunkPregenerator plugin) {
    this.generatorManager = plugin.getGeneratorManager();
    this.fcpio = plugin.getFcpio();
    final FCPConfiguration configuration = this.fcpio.getFcpConfiguration();
    this.notificationType = configuration.getNotificationType();
    this.nanosPerTick = (long) (configuration.getMaxMillisPerTick() * 1E6);
    this.tickDelay = configuration.getWaitTicksBetween() + 1;
    this.millisBetweenNotification = configuration.getSecondsPerNotification() * 1000L;
    this.async = configuration.isAsyncChunkLoadingEnabled();
    this.highPrio = configuration.isHighAsyncPrio();
    this.maxAsyncParallels = configuration.getMaxParallelAsyncCalls();
    this.pendingTaskLoads = new PriorityQueue<>();
  }

  private final FCPIO fcpio;
  @Getter
  private final boolean async;
  @Getter
  private final boolean highPrio;
  @Getter
  private final GeneratorManager generatorManager;
  @Getter
  private final int maxAsyncParallels;
  private final long millisBetweenNotification;
  private long lastNotification = System.currentTimeMillis();
  private final int tickDelay;
  private final long nanosPerTick;
  private final NotificationType notificationType;
  private long tickCounter;
  @Setter
  @Getter
  private boolean paused = false;
  private final PriorityQueue<TaskLoad> pendingTaskLoads;

  public List<String> getPending() {
    final List<String> lines = new ArrayList<>();
    int index = 0;
    for (final TaskLoad load : this.pendingTaskLoads) {
      lines.add("§6[§f" + index++ + "§6] " + load.loadInfo() + (index == 1 ? "§7 (current)" : ""));
    }
    Collections.reverse(lines);
    return lines;
  }

  public void onEnable() {
    final JsonObject jsonObject = this.fcpio.loadTaskLoadCache();
    if (jsonObject != null) {
      this.loadFromJson(jsonObject);
    }
  }

  public void onDisable() {
    final JsonObject jsonObject = this.getAsJson();
    if (jsonObject == null) {
      this.fcpio.deleteTaskLoadCache();
      return;
    }
    this.fcpio.saveTaskLoadCache(jsonObject);
  }

  private JsonObject getAsJson() {
    final JsonObject jsonObject = new JsonObject();

    if (!this.isRunning()) {
      return null;
    }

    jsonObject.addProperty("Paused", this.paused);

    final JsonArray taskLoadArray = new JsonArray();
    while (!this.pendingTaskLoads.isEmpty()) {
      taskLoadArray.add(TaskLoadType.serializeLoad(this.pendingTaskLoads.poll()));
    }
    jsonObject.add("TaskLoadData", taskLoadArray);

    return jsonObject;
  }

  private void loadFromJson(final JsonObject jsonObject) {
    this.paused = jsonObject.get("Paused").getAsBoolean();
    final JsonArray taskLoadArray = jsonObject.get("TaskLoadData").getAsJsonArray();
    for (final JsonElement element : taskLoadArray) {
      this.pendingTaskLoads.add(TaskLoadType.deserializeLoad(element.getAsJsonObject()));
    }
  }

  public void stop() {
    this.pendingTaskLoads.poll();
  }

  public boolean isRunning() {
    return !this.pendingTaskLoads.isEmpty();
  }

  public void queueTaskLoad(final TaskLoad taskLoad) {
    this.pendingTaskLoads.add(taskLoad);
  }

  public String info() {
    if (this.pendingTaskLoads.isEmpty()) {
      return null;
    }
    final long reqTime = System.currentTimeMillis() - this.lastNotification;
    this.lastNotification = System.currentTimeMillis();
    final TaskLoad next = this.pendingTaskLoads.peek();
    assert next != null;
    return next.summary(reqTime);
  }

  @Override
  public void run() {
    if (this.paused || this.pendingTaskLoads.isEmpty() || ++this.tickCounter % this.tickDelay != 0) {
      return;
    }

    final long start = System.nanoTime();
    final long notificationDelta = System.currentTimeMillis() - this.lastNotification;
    final TaskLoad taskLoad = this.pendingTaskLoads.peek();

    assert taskLoad != null;
    if (taskLoad.isDone()) {
      
      final TaskLoad load = this.pendingTaskLoads.poll();
      assert load != null;
      this.notificationType.call(load.summary(notificationDelta));
      this.lastNotification = 0L;
      load.onCompletion(this);
      return;
    }
    if (notificationDelta >= this.millisBetweenNotification) {
      this.lastNotification = System.currentTimeMillis();
      this.notificationType.call(taskLoad.summary(notificationDelta));
    }
    while (System.nanoTime() - start < this.nanosPerTick && !taskLoad.isDone()) {
      taskLoad.proceed();
    }
  }

}
