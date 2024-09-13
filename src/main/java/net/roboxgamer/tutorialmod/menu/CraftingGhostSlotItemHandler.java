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
  public boolean mayPlace(ItemStack stack) {
    // Prevent real item placement, only allow ghost items (for visual representation)
    return false;
  }
  
  @Override
  public boolean mayPickup(Player player) {
    // Prevent real item pickup, acting as a ghost slot
    return false;
  }
  
  @Override
  public @NotNull ItemStack remove(int amount) {
    // Always return an empty stack to simulate a fake slot
    return ItemStack.EMPTY;
  }
  
  @Override
  public void set(ItemStack stack) {
    // This will set a ghost item in the slot, so we make a copy of the stack
    if (!stack.isEmpty()) {
      stack = stack.copy();
    }
    super.set(stack);
  }
  
  // Method to increase ghost item stack or set a new item if it's not a ghost item yet
  public void increase(ItemStack is) {
    ItemStack current = getItem();
    if (current.isEmpty()) {
      // If the slot is empty, set the current item as the ghost item with count 1
      ItemStack newStack = is.copy();
      newStack.setCount(1);
      set(newStack);
    } else if (ItemStack.isSameItemSameComponents(current, is)) {
      // If the item in hand is the same as the one in the ghost slot, increase the count
      current.grow(1);
      set(current);
    }
  }
  
  // Method to decrease ghost item stack or reset it
  public void decrease(ItemStack is) {
    ItemStack current = getItem();
    if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, is)) {
      // If the item in hand is the same, decrease the count
      current.shrink(1);
      if (current.getCount() <= 0) {
        // If the count reaches 0, reset the slot to empty
        set(ItemStack.EMPTY);
      } else {
        // Otherwise, set the new decreased stack
        set(current);
      }
    }
  }
  
  // Method to remove the item when right-clicking with an empty hand
  public void removeItem() {
    set(ItemStack.EMPTY);
  }
  
  @Override
  public boolean isActive() {
    // You can control whether this slot is active or not
    return true;
  }
  
  public SLOT_TYPE getType() {
    return SLOT_TYPE.GHOST;
  }
  
  // Example method for setting a ghost item as filter
  public void setFilterTo(ItemStack stack) {
    if (!stack.isEmpty()) {
      set(stack);
    }
  }
}