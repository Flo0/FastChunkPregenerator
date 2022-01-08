package com.gestankbratwurst.fastchunkpregen.generation;

import com.gestankbratwurst.fastchunkpregen.FastChunkPregenerator;
import com.gestankbratwurst.fastchunkpregen.generation.tasks.CoordinateFetcher;
import com.gestankbratwurst.fastchunkpregen.generation.tasks.GeneratorTask;
import java.util.List;
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
public class GeneratorManager {

  public GeneratorManager(final FastChunkPregenerator plugin) {
    this.generatorTask = new GeneratorTask(plugin);
    Bukkit.getScheduler().runTaskTimer(plugin, this.generatorTask, 1L, 1L);
  }

  private final GeneratorTask generatorTask;

  public void enableGenerator() {
    this.generatorTask.onEnable();
  }

  public void disableGenerator() {
    this.generatorTask.onDisable();
  }

  public boolean isRunning() {
    return this.generatorTask.isRunning();
  }

  public boolean isPaused() {
    return this.generatorTask.isPaused();
  }

  public String info() {
    return this.generatorTask.info();
  }

  public void start(final World world, final int cx, final int cy, final int radius) {
    this.generatorTask.queueTaskLoad(new CoordinateFetcher(world, cx, cy, radius));
  }

  public void resume() {
    this.generatorTask.setPaused(false);
  }

  public void pause() {
    this.generatorTask.setPaused(true);
  }

  public void stop() {
    this.generatorTask.stop();
  }

  public List<String> getPending() {
    return this.generatorTask.getPending();
  }

}
