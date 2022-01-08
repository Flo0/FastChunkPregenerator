package com.gestankbratwurst.fastchunkpregen.generation.generators;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.World;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 30.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class PaperUrgentGenerator extends PaperGenerator {

  @Override
  public CompletableFuture<Chunk> generateAsyncAt(final World world, final int x, final int z) {
    return world.getChunkAtAsyncUrgently(x, z);
  }

}
