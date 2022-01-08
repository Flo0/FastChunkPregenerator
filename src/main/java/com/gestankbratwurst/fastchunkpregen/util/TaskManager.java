package com.gestankbratwurst.fastchunkpregen.util;

import com.gestankbratwurst.fastchunkpregen.FastChunkPregenerator;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 13.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class TaskManager {

  private static FastChunkPregenerator plugin;
  private static BukkitScheduler bukkitScheduler;

  public static void init(final FastChunkPregenerator plugin) {
    TaskManager.plugin = plugin;
    TaskManager.bukkitScheduler = Bukkit.getScheduler();
  }

  public static void runSync(final Runnable runnable) {
    bukkitScheduler.runTask(plugin, runnable);
  }

}
