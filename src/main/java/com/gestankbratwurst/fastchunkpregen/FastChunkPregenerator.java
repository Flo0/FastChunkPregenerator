package com.gestankbratwurst.fastchunkpregen;

import co.aikar.commands.BukkitCommandManager;
import com.gestankbratwurst.fastchunkpregen.chunkloading.ChunkLoadingManager;
import com.gestankbratwurst.fastchunkpregen.commands.ChunkGenCommand;
import com.gestankbratwurst.fastchunkpregen.generation.GeneratorManager;
import com.gestankbratwurst.fastchunkpregen.generation.tasks.ChunkLoadTaskListener;
import com.gestankbratwurst.fastchunkpregen.io.FCPIO;
import com.gestankbratwurst.fastchunkpregen.util.TaskManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class FastChunkPregenerator extends JavaPlugin {

  @Getter
  private FCPIO fcpio;
  @Getter
  private GeneratorManager generatorManager;

  private ChunkLoadingManager chunkLoadingManager;

  @Override
  public void onEnable() {
    this.fcpio = new FCPIO(this);
    this.fcpio.loadConfiguration();
    this.checkForPaper();

    this.generatorManager = new GeneratorManager(this);

    this.chunkLoadingManager = new ChunkLoadingManager(this.fcpio);
    Bukkit.getPluginManager().registerEvents(this.chunkLoadingManager, this);
    if (this.fcpio.getFcpConfiguration().isPauseOnPlayerLogin()) {
      Bukkit.getPluginManager().registerEvents(new ChunkLoadTaskListener(this.generatorManager), this);
    }
    TaskManager.init(this);
    this.registerCommands();
    this.generatorManager.enableGenerator();
    new Metrics(this, 6290);
  }

  private void checkForPaper() {
    try {
      Class.forName("com.destroystokyo.paper.PaperConfig");
      this.getLogger().info(">> Paper was found.");
      this.getLogger().info(">> Async chunk loading support is enabled.");
    } catch (final ClassNotFoundException e) {
      if (!(Bukkit.getVersion().contains("Tuinity") || Bukkit.getVersion().contains("tuinity"))) {
        if (this.fcpio.getFcpConfiguration().isAsyncChunkLoadingEnabled()) {
          this.getLogger().severe(">> Using async chunk loading is only supported on Paper.");
          this.getLogger().severe(">> Shutting down.");
          Bukkit.shutdown();
          return;
        }
        this.getLogger().info(">> You are not using Paper. (why?)");
        this.getLogger().info(">> Async chunk loading support is disabled.");
      }
    }
  }

  @Override
  public void onDisable() {
    this.generatorManager.disableGenerator();
    this.chunkLoadingManager.flush();
  }

  private void registerCommands() {
    final BukkitCommandManager commandManager = new BukkitCommandManager(this);
    commandManager.registerCommand(new ChunkGenCommand(this.generatorManager, this.chunkLoadingManager));
  }

}
