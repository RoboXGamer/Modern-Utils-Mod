package net.roboxgamer.modernutils.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CustomItemStackHandler extends ItemStackHandler {
  protected final BlockEntity blockEntity;
  
  public CustomItemStackHandler(int size, BlockEntity blockEntity) {
    super(size);
    this.blockEntity = blockEntity;
  }
  
  @Override
  protected void onContentsChanged(int slot) {
    blockEntity.setChanged();
  }
  
  public NonNullList<ItemStack> getStacks() {
    return this.stacks;
  }
  
  public NonNullList<ItemStack> getStacksCopy(int startIndex) {
    var t = NonNullList.withSize(this.stacks.size()-startIndex, ItemStack.EMPTY);
    for (int i = startIndex; i < this.stacks.size(); i++) {
      t.set(i - startIndex, this.stacks.get(i).copy());
    }
    return t;
  }
  
  public NonNullList<ItemStack> getStacksCopy() {
    return this.getStacksCopy(0);
  }
  
  public boolean isCompletelyEmpty() {
    // if all the slots are empty, return true
    boolean isEmpty = true;
    for (ItemStack stack : this.stacks) {
      if (!stack.isEmpty()) {
        isEmpty = false;
        break;
      }
    }
    return isEmpty;
  }
  
  public boolean isFull() {
    boolean isFull = true;
    for (ItemStack stack : this.stacks) {
      if (stack.getCount() < stack.getMaxStackSize()) {
        isFull = false;
        break;
      }
    }
    return isFull;
  }
  
  public boolean allDisabled() {
    return false;
  }
}
