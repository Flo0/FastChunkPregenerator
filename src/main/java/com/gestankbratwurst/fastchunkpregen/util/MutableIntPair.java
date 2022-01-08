package com.gestankbratwurst.fastchunkpregen.util;

import lombok.Getter;
import lombok.Setter;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of FastChunkPregenerator and was created at the 13.07.2020
 *
 * FastChunkPregenerator can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class MutableIntPair {

  public MutableIntPair(final int x, final int z) {
    this.xValue = x;
    this.zValue = z;
  }

  @Getter
  @Setter
  private int xValue;
  @Getter
  @Setter
  private int zValue;

  public void setValues(final int x, final int z) {
    this.xValue = x;
    this.zValue = z;
  }

  public void incrementX() {
    this.xValue++;
  }

  public void incrementZ() {
    this.zValue++;
  }

  public void decrementX() {
    this.xValue--;
  }

  public void decrementZ() {
    this.zValue--;
  }

  public void addX(final int value) {
    this.xValue += value;
  }

  public void addZ(final int value) {
    this.zValue += value;
  }

  @Override
  public String toString() {
    return this.xValue + "#" + this.zValue;
  }

  public static MutableIntPair fromString(final String value) {
    final String[] split = value.split("#");
    return new MutableIntPair(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }

}
