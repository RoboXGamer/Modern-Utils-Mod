package net.roboxgamer.tutorialmod.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.roboxgamer.tutorialmod.TutorialMod;

import java.util.Map;

public class RedstoneManager {
  BlockEntity blockEntity;
  public RedstoneManager(BlockEntity be) {
    this.blockEntity = be;
  }
  
  public enum RedstoneMode {
    ALWAYS_ON,
    REDSTONE_ON,
    REDSTONE_OFF
  }
  
  // I want a map to map int to redstone mode
  public static final Map<Integer, RedstoneMode> REDSTONE_MODE_MAP = Map.of(
      0, RedstoneMode.ALWAYS_ON,
      1, RedstoneMode.REDSTONE_ON,
      2, RedstoneMode.REDSTONE_OFF
  );
  
  private RedstoneMode redstoneMode = RedstoneMode.ALWAYS_ON;
  
  public RedstoneMode getRedstoneMode() {
    return this.redstoneMode;
  }
  
  public void setRedstoneMode(RedstoneMode mode) {
    this.redstoneMode = mode;
    TutorialMod.LOGGER.debug("RedstoneMode: {}", this.redstoneMode);
    this.blockEntity.setChanged();
  }
  
  public RedstoneMode getNextRedstoneMode() {
    return RedstoneMode.values()[(this.redstoneMode.ordinal() + 1) % RedstoneMode.values().length];
  }
}
