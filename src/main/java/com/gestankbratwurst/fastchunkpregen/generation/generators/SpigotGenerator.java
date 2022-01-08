package com.gestankbratwurst.fastchunkpregen.generation.generators;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
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
public class SpigotGenerator implements ChunkGenerator {

  @Override
  public boolean canLoadAsync() {
    return false;
  }

  @Override
  public void generateSyncAt(final World world, final int x, final int z) {
    world.getChunkAt(x, z);
  }

  @Override
  public CompletableFuture<Chunk> generateAsyncAt(final World world, final int x, final int z) {
    throw new UnsupportedOperationException();
  }

}
