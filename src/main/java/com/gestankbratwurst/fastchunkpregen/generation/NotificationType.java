package com.gestankbratwurst.fastchunkpregen.generation;

import org.bukkit.Bukkit;
import org.bukkit.permissions.ServerOperator;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 12.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public enum NotificationType {

  NONE() {
    @Override
    public void call(final String message) {
    }
  },
  CONSOLE() {
    @Override
    public void call(final String message) {
      Bukkit.getConsoleSender().sendMessage(message);
    }
  },
  BROADCAST() {
    @Override
    public void call(final String message) {
      Bukkit.broadcastMessage(message);
    }
  },
  OP() {
    @Override
    public void call(final String message) {
      Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(pl -> pl.sendMessage(message));
    }
  },
  OP_AND_CONSOLE() {
    @Override
    public void call(final String message) {
      Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(pl -> pl.sendMessage(message));
      Bukkit.getConsoleSender().sendMessage(message);
    }
  };

  public abstract void call(String message);

}
