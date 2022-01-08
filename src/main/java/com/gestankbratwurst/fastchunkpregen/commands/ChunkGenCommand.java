package com.gestankbratwurst.fastchunkpregen.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.gestankbratwurst.fastchunkpregen.chunkloading.ChunkLoadingManager;
import com.gestankbratwurst.fastchunkpregen.generation.GeneratorManager;
import com.gestankbratwurst.fastchunkpregen.util.Msg;
import com.gestankbratwurst.fastchunkpregen.util.UtilChunk;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.mutable.MutableInt;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 12.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
@RequiredArgsConstructor
@CommandAlias("chunkgen|fcp")
@CommandPermission("fcp.commands")
public class ChunkGenCommand extends BaseCommand {

  private final GeneratorManager generatorManager;
  private final ChunkLoadingManager chunkLoadingManager;

  @Default
  public void onDefault(final CommandSender sender) {
    Msg.send(sender, "§6 > Commands §e[§f/fcp §eor §f/chunkgen§e]§f:");
    Msg.send(sender, "§f   # Starts generation in current world. Radius in Blocks.");
    Msg.send(sender, "§f   # Player -> Player location is middle");
    Msg.send(sender, "§f   # Server -> World spawn is middle");
    Msg.send(sender, "§e   /fcp start <radius> [world] [chunkX] [chunkZ]");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Fills the vanilla world border.");
    Msg.send(sender, "§f   # Buffer is in chunks.");
    Msg.send(sender, "§e   /fcp fillvanilla <chunkbuffer> [world]");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Pauses generation.");
    Msg.send(sender, "§e   /fcp pause");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Resumes generation.");
    Msg.send(sender, "§e   /fcp resume");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Cancels current task and jumps to next one.");
    Msg.send(sender, "§e   /fcp cancel");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Lists information about all tasks.");
    Msg.send(sender, "§e   /fcp pending");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Sends all commands for force loading/viewing chunks.");
    Msg.send(sender, "§e   /fcp forced");
  }

  @Subcommand("forced")
  public void onForced(final CommandSender sender) {
    Msg.send(sender, "§6 > Commands §e[§f/fcp forced§e]§f:");
    Msg.send(sender, "§f   # Forceloads chunks in this radius.");
    Msg.send(sender, "§f   # They will remain loaded until you unload them.");
    Msg.send(sender, "§e   /fcp forced load <chunkradius>");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Frees all chunks in a radius.");
    Msg.send(sender, "§e   /fcp forced free <chunkradius>");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Displays world or total info about force loaded chunks.");
    Msg.send(sender, "§e   /fcp forced worldinfo/totalinfo");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Toggles chat map for loaded chunks.");
    Msg.send(sender, "§e   /fcp forced view on/off");
    Msg.send(sender, "");
    Msg.send(sender, "§f   # Shows chat map once for current position.");
    Msg.send(sender, "§e   /fcp forced view once");
  }

  @Subcommand("forced load")
  @CommandCompletion("<chunkradius>")
  @Syntax("<chunkradius>")
  public void onForceLoad(final Player sender, final int radius) {
    final World targetWorld = sender.getWorld();
    final Chunk middle = sender.getLocation().getChunk();
    final int amount = UtilChunk.apply(middle, radius, c -> this.chunkLoadingManager.forceLoad(targetWorld, c.getX(), c.getZ()));
    Msg.send(sender, "Successfully force loaded §e" + amount + "§7 chunks.");
    Msg.send(sender, "These chunks will stay loaded until you unload them again.");
  }

  @Subcommand("forced free")
  @CommandCompletion("<chunkradius> @worlds")
  @Syntax("<chunkradius> [world]")
  public void onFree(final Player sender, final int radius) {
    final World targetWorld = sender.getWorld();
    final Chunk middle = sender.getLocation().getChunk();
    final MutableInt counter = new MutableInt(0);
    UtilChunk.apply(middle, radius, c -> {
      if (this.chunkLoadingManager.freeChunk(targetWorld, c.getX(), c.getZ())) {
        counter.increment();
      }
    });
    if (counter.intValue() == 0) {
      Msg.send(sender, "There were no chunks to free here.");
      return;
    }
    Msg.send(sender, "Successfully freed §e" + counter.intValue() + "§7 chunks.");
  }

  @Subcommand("forced worldinfo")
  @CommandCompletion("@worlds")
  @Syntax("[world]")
  public void onWorldInfo(final CommandSender sender, @Optional World world) {
    if (world == null) {
      if (sender instanceof Player) {
        world = ((Player) sender).getWorld();
      } else {
        Msg.send(sender, "You need to specify a world when using a console.");
        return;
      }
    }

    final int amount = world.getForceLoadedChunks().size();
    final int fcpAmount = this.chunkLoadingManager.getLoadedChunkAmount(world.getUID());
    Msg.send(sender, "There are §e" + amount + "§7 force loaded Chunks in this world.");
    Msg.send(sender, "From those, §e" + fcpAmount + "§7 are loaded by FCP.");
  }

  @Subcommand("forced totalinfo")
  public void onTotalInfo(final CommandSender sender) {

    int total = 0;
    for (final World world : Bukkit.getWorlds()) {
      total += world.getForceLoadedChunks().size();
    }

    final int fcpAmount = this.chunkLoadingManager.getLoadedChunkAmount();
    Msg.send(sender, "There are §e" + total + "§7 force loaded Chunks on this server.");
    Msg.send(sender, "From those, §e" + fcpAmount + "§7 are loaded by FCP.");
  }

  @Subcommand("forced view on")
  public void onViewOn(final Player sender) {
    this.chunkLoadingManager.addWatcher(sender.getUniqueId());
  }

  @Subcommand("forced view off")
  public void onViewOff(final Player sender) {
    this.chunkLoadingManager.removeWatcher(sender.getUniqueId());
  }

  @Subcommand("forced view once")
  public void onViewOnce(final Player sender) {
    this.chunkLoadingManager.sendFormattedChunks(sender);
  }

  @Subcommand("start")
  @CommandCompletion("<radius> @worlds [chunkX] [chunkZ]")
  @Syntax("<radius> [world] [chunkX] [chunkZ]")
  public void onStart(final CommandSender sender, final int radius, @Optional World world, @Optional final String cx,
      @Optional final String cz) {

    if (radius >= 300000) {
      sender.sendMessage("§cThe JVM Heap is not sufficient for this size.");
      return;
    }

    if (world == null) {
      if (sender instanceof Player) {
        final Player player = (Player) sender;
        world = player.getWorld();
      } else {
        Msg.send(sender, "You need to specify a world if you are on console.");
        return;
      }
    }

    Msg.send(sender, "World: " + world.getName());

    final int midX;
    final int midZ;

    if (cx == null || cz == null) {
      if (sender instanceof Player) {
        final Player player = (Player) sender;
        final Chunk mid = player.getLocation().getChunk();
        midX = mid.getX();
        midZ = mid.getZ();
        Msg.send(sender, "Using player location as middle.");
      } else {
        final Chunk mid = world.getSpawnLocation().getChunk();
        midX = mid.getX();
        midZ = mid.getZ();
        Msg.send(sender, "Using world spawn as middle.");
      }
    } else {
      try {
        midX = Integer.parseInt(cx) / 16;
        midZ = Integer.parseInt(cz) / 16;
        Msg.send(sender, "Using " + midX + "|" + midZ + " as middle.");
      } catch (final NumberFormatException e) {
        Msg.send(sender, "Cx or cz is not a valid integer value.");
        return;
      }
    }

    this.generatorManager.start(world, midX, midZ, radius / 16);
    Msg.send(sender, "Generation task scheduled.");
  }

  @Subcommand("fillvanilla")
  @CommandCompletion("<chunkbuffer> @worlds")
  @Syntax("<chunkbuffer> [world]")
  public void onFill(final CommandSender sender, final int chunkbuffer, @Optional World world) {
    if (world == null) {
      if (sender instanceof Player) {
        world = ((Player) sender).getWorld();
        Msg.send(sender, "No world specified. Using your world.");
      } else {
        Msg.send(sender, "You need to specify a world if you are on console.");
        return;
      }
    }

    final int radius = (int) (world.getWorldBorder().getSize() / 32) + chunkbuffer;
    if (radius >= 22360) {
      sender.sendMessage("§cThe JVM Heap is not sufficient for this border size.");
      return;
    }
    final Chunk mid = world.getWorldBorder().getCenter().getChunk();
    this.generatorManager.start(world, mid.getX(), mid.getZ(), radius);
    Msg.send(sender, "Started world border fill with a buffer of " + chunkbuffer + " chunks.");
  }

  @Subcommand("pause")
  public void onPause(final CommandSender sender) {
    if (!this.generatorManager.isRunning()) {
      Msg.send(sender, "There is nothing to pause.");
      return;
    }
    this.generatorManager.pause();
    Msg.send(sender, "Paused pending generation task.");
  }

  @Subcommand("resume")
  public void onResume(final CommandSender sender) {
    if (!this.generatorManager.isRunning() || !this.generatorManager.isPaused()) {
      Msg.send(sender, "There is nothing to resume.");
      return;
    }
    this.generatorManager.resume();
    Msg.send(sender, "Resumed generation task.");
  }

  @Subcommand("cancel")
  public void onStop(final CommandSender sender) {
    if (!this.generatorManager.isRunning()) {
      Msg.send(sender, "There is nothing to cancel.");
      return;
    }
    this.generatorManager.stop();
    Msg.send(sender, "Current task was canceled. Proceed with next one if present.");
  }

  @Subcommand("pending")
  public void onPending(final CommandSender sender) {
    if (this.generatorManager.isRunning()) {

      for (final String line : this.generatorManager.getPending()) {
        Msg.send(sender, line);
      }

      Msg.send(sender, "Current Progress:");
      sender.sendMessage(this.generatorManager.info());
    } else {
      Msg.send(sender, "There are no pending tasks.");
    }
  }

}
