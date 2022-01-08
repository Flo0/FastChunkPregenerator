package com.gestankbratwurst.fastchunkpregen.generation.tasks;

import com.gestankbratwurst.fastchunkpregen.util.MutableIntPair;
import com.gestankbratwurst.fastchunkpregen.util.UtilChunk;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
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
public class CoordinateFetcher implements TaskLoad {

  public CoordinateFetcher(final JsonObject jsonObject) {
    this.worldID = UUID.fromString(jsonObject.get("WorldID").getAsString());
    final JsonArray locationArray = jsonObject.get("Locations").getAsJsonArray();
    this.locations = new LongLinkedOpenHashSet(locationArray.size());
    for (int i = 0; i < locationArray.size(); i++) {
      this.locations.add(locationArray.get(i).getAsLong());
    }
    this.targetSize = jsonObject.get("TargetSize").getAsInt();
    this.centerX = jsonObject.get("CenterX").getAsInt();
    this.centerZ = jsonObject.get("CenterZ").getAsInt();
    this.cursor = MutableIntPair.fromString(jsonObject.get("Cursor").getAsString());
    this.locationIndex = jsonObject.get("LocationIndex").getAsInt();
    this.done = jsonObject.get("Done").getAsBoolean();
    this.currentR = jsonObject.get("CurrentR").getAsInt();
    this.direction = Direction.valueOf(jsonObject.get("Direction").getAsString());
  }

  public CoordinateFetcher(final World world, final int centerX, final int centerZ, final int radius) {
    this.currentR = 1;
    this.centerX = centerX;
    this.centerZ = centerZ;
    this.cursor = new MutableIntPair(-1, -1);
    final int sideLength = 1 + 2 * radius;
    this.targetSize = sideLength * sideLength;
    this.locations = new LongLinkedOpenHashSet(this.targetSize);
    this.worldID = world.getUID();
  }

  @Getter(AccessLevel.PACKAGE)
  private final UUID worldID;
  @Getter(AccessLevel.PACKAGE)
  private final LongLinkedOpenHashSet locations;
  private final int targetSize;
  private final int centerX;
  private final int centerZ;
  private int locationIndex;
  private final MutableIntPair cursor;
  private boolean done;
  private int currentR;
  private Direction direction = Direction.RIGHT;

  private double percent() {
    return ((int) ((100D / this.targetSize * (this.locationIndex)) * 100D)) / 100D;
  }

  @Override
  public void proceed() {
    if (this.locationIndex == this.targetSize) {
      this.done = true;
      return;
    }
    final int x = this.cursor.getXValue();
    final int z = this.cursor.getZValue();
    this.locations.add(UtilChunk.getChunkKey(x + this.centerX, z + this.centerZ));
    this.locationIndex++;
    this.direction.rotationAction.accept(this.cursor);
    if (this.direction.rotationDecider.apply(this.cursor, this.currentR)) {
      this.direction = this.direction.next();
      if (this.direction == Direction.RIGHT) {
        this.currentR++;
        this.cursor.setValues(-this.currentR, -this.currentR);
      }
    }
  }

  @Override
  public void onCompletion(final GeneratorTask generatorTask) {
    final World world = Bukkit.getWorld(this.worldID);
    if (world == null) {
      throw new IllegalStateException("Generated world is no longer loaded.");
    }
    generatorTask.queueTaskLoad(new RegionFileFetcher(this));
  }

  @Override
  public String summary(final long millisDelta) {
    return "§6[§f" + this.getWorldName() + "§6]§e Fetching Coordinates: §f" + this.locationIndex + " §eof §f" + this.targetSize + " §e[§f"
        + this.percent()
        + "%§e]";
  }

  @Override
  public String getWorldName() {
    return Bukkit.getWorld(this.worldID).getName();
  }

  @Override
  public boolean isDone() {
    return this.done;
  }

  @Override
  public JsonObject getAsJson() {
    final JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("WorldID", this.worldID.toString());
    final JsonArray locationsArray = new JsonArray();
    for (final long key : this.locations) {
      locationsArray.add(key);
    }
    jsonObject.add("Locations", locationsArray);
    jsonObject.addProperty("TargetSize", this.targetSize);
    jsonObject.addProperty("CenterX", this.centerX);
    jsonObject.addProperty("CenterZ", this.centerZ);
    jsonObject.addProperty("Cursor", this.cursor.toString());
    jsonObject.addProperty("LocationIndex", this.locationIndex);
    jsonObject.addProperty("Done", this.done);
    jsonObject.addProperty("CurrentR", this.currentR);
    jsonObject.addProperty("Direction", this.direction.toString());

    return jsonObject;
  }

  @Override
  public TaskLoadType getLoadType() {
    return TaskLoadType.COORD_FETCHER;
  }

  @Override
  public void setPaused(boolean value) {
    
  }

  @Override
  public boolean isPaused() {
    return false;
  }

  @AllArgsConstructor
  private enum Direction {

    RIGHT(MutableIntPair::incrementX, (cursor, R) -> cursor.getXValue() == R),
    DOWN(MutableIntPair::incrementZ, (cursor, R) -> cursor.getZValue() == R),
    LEFT(MutableIntPair::decrementX, (cursor, R) -> cursor.getXValue() == -R),
    UP(MutableIntPair::decrementZ, (cursor, R) -> cursor.getZValue() == -R);

    private final Consumer<MutableIntPair> rotationAction;
    private final BiFunction<MutableIntPair, Integer, Boolean> rotationDecider;

    public Direction next() {
      if (this == UP) {
        return RIGHT;
      }
      return Direction.values()[this.ordinal() + 1];
    }

  }

}
