package net.roboxgamer.tutorialmod.menu;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OutputSlotItemHandler extends SlotItemHandler {
  private final MechanicalCrafterMenu menu;
  public OutputSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition,MechanicalCrafterMenu menu) {
    super(itemHandler, index, xPosition, yPosition);
    this.menu = menu;
  }
  
  @Override
  public boolean mayPlace(@NotNull ItemStack stack) {
    return !this.menu.isSlotDisabled(this.index) && super.mayPlace(stack);
  }
}
