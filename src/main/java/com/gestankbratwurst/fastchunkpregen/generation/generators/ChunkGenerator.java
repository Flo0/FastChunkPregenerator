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
public interface ChunkGenerator {

  boolean canLoadAsync();
  void generateSyncAt(World world, int x, int z);
  CompletableFuture<Chunk> generateAsyncAt(World world, int x, int z);

}
