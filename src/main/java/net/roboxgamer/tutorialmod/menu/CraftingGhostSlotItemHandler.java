package net.roboxgamer.tutorialmod.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class CraftingGhostSlotItemHandler extends SlotItemHandler {
  public CraftingGhostSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
    super(itemHandler, index, xPosition, yPosition);
  }
  
  @Override
  public int getMaxStackSize() {
    return 0;
  }
  
  @Override
  public int getMaxStackSize(@NotNull ItemStack stack) {
    return 1;
  }
  
  @Nonnull
  @Override
  public ItemStack remove(int amount) {
    super.remove(amount);
    return ItemStack.EMPTY;
  }
  
  @Override
  public boolean mayPickup(@NotNull Player playerIn) {
    return true;
  }
  
  @Override
  public boolean mayPlace(@NotNull ItemStack stack) {
    return true;
  }
  
  @Override
  public void set(@NotNull ItemStack stack) {
    if (!stack.isEmpty()) {
      stack.setCount(1);
    }
    super.set(stack);
  }
  
  @Override
  public @NotNull ItemStack safeInsert(ItemStack stack, int increment) {
    super.safeInsert(stack, increment);
    stack.grow(1);
    return stack;
  }
  
  public SLOT_TYPE getType() {
    return SLOT_TYPE.GHOST;
  }
}