package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.gestankbratwurst.fastchunkpregen.generation.GeneratorManager;
import com.gestankbratwurst.fastchunkpregen.util.TaskManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 14.11.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
@RequiredArgsConstructor
public class ChunkLoadTaskListener implements Listener {

  private final GeneratorManager generatorManager;

  @EventHandler
  public void onLogin(final PlayerLoginEvent event) {
    final Player player = event.getPlayer();
    if (!player.hasPermission("fcp.pause.bypass")) {
      this.generatorManager.pause();
    }
  }

  @EventHandler
  public void onLogout(final PlayerQuitEvent event) {
    TaskManager.runSync(() -> {
      for (final Player player : Bukkit.getOnlinePlayers()) {
        if (!player.hasPermission("fcp.pause.bypass")) {
          return;
        }
      }
      this.generatorManager.resume();
    });
  }

}
