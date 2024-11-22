package net.roboxgamer.modernutils.util;

import net.minecraft.world.inventory.ClickAction;

public record PackedButtonData(Constants.Sides side, boolean shifted, ClickAction clickAction) {
  
  // Create ID from components
  public int toId() {
    return (side.ordinal() & 0b111) |
        ((shifted ? 1 : 0) << 3) |
        (clickAction.ordinal() << 4);
  }
  
  // Create from ID
  public static PackedButtonData fromId(int id) {
    return new PackedButtonData(
        Constants.Sides.values()[id & 0b111],
        ((id >> 3) & 1) == 1,
        ClickAction.values()[id >> 4]
    );
  }
  
  // Utility factory method
  public static PackedButtonData create(Constants.Sides side, boolean shifted, ClickAction clickAction) {
    return new PackedButtonData(side, shifted, clickAction);
  }
}
