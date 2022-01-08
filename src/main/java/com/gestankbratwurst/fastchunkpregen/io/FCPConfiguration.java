package com.gestankbratwurst.fastchunkpregen.io;

import com.gestankbratwurst.fastchunkpregen.generation.NotificationType;
import lombok.Getter;
import org.bukkit.Bukkit;
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
public class FCPConfiguration {

  protected FCPConfiguration(final YamlConfiguration configuration) {
    this.maxMillisPerTick = configuration.getDouble("MaxMillisPerTick", 20.5);
    this.waitTicksBetween = configuration.getInt("WaitTicksBetween", 0);
    this.asyncChunkLoadingEnabled = configuration.getBoolean("AsyncChunkLoadingEnabled", false);
    this.highAsyncPrio = configuration.getBoolean("HighAsyncPriority", true);
    this.unsafeAsync = configuration.getBoolean("UnsafeAsyncCalls", false);
    this.maxParallelAsyncCalls = configuration.getInt("MaxParallelAsyncCalls", 4);
    this.secondsPerNotification = configuration.getInt("SecondsPerNotification", 60);
    this.pauseOnPlayerLogin = configuration.getBoolean("OnlyGenerateWithNoPlayersOnline", false);

    final String notificationTypeString = configuration.getString("NotificationType", "CONSOLE");
    NotificationType notificationType;
    try {
      notificationType = NotificationType.valueOf(notificationTypeString);
    } catch (final Exception e) {
      notificationType = NotificationType.CONSOLE;
      Bukkit.getLogger().warning("NotificationType does not exist: " + notificationTypeString);
      Bukkit.getLogger().warning("Using fallback: " + notificationType.toString());
    }

    this.notificationType = notificationType;
  }

  @Getter
  private final double maxMillisPerTick;
  @Getter
  private final int waitTicksBetween;
  @Getter
  private final boolean asyncChunkLoadingEnabled;
  @Getter
  private final boolean highAsyncPrio;
  @Getter
  private final boolean unsafeAsync;
  @Getter
  private final int maxParallelAsyncCalls;
  @Getter
  private final int secondsPerNotification;
  @Getter
  private final NotificationType notificationType;
  @Getter
  private final boolean pauseOnPlayerLogin;

}