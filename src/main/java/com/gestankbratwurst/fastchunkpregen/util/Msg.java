package com.gestankbratwurst.fastchunkpregen.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 23.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class Msg {

  private static final String PREFIX = "§6[§fFCP§6] §7";

  public static void send(final Player player, final Object message) {
    player.sendMessage(PREFIX + message.toString());
  }

  public static void send(final CommandSender sender, final Object message) {
    sender.sendMessage(PREFIX + message.toString());
  }

}
