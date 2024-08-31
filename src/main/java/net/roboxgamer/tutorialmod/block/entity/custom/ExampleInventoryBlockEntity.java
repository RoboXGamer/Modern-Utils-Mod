package net.roboxgamer.tutorialmod.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExampleInventoryBlockEntity extends BlockEntity {
  private final ItemStackHandler inventory = new ItemStackHandler(){
    @Override
    protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      ExampleInventoryBlockEntity.this.setChanged();
    }
  };
  
  private BlockCapabilityCache<IItemHandler, @Nullable Direction> capCache;
  
  @Override
  public void onLoad() {
    super.onLoad();
    Level level = this.getLevel();
    if (!(level instanceof ServerLevel slevel)) return;
    BlockPos pos = this.getBlockPos();
    this.capCache = BlockCapabilityCache.create(
        Capabilities.ItemHandler.BLOCK,
        slevel,
        pos,
        null
    );
  }
  
  public ExampleInventoryBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.EXAMPLE_INVENTORY_BE.get(), pos, blockState);
  }

  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    CompoundTag tutorialmodData = tag.getCompound(TutorialMod.MODID);
    this.inventory.deserializeNBT
        (registries, tutorialmodData.getCompound("inventory"));

  }

  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    var tutorialmodData = new CompoundTag();
    tutorialmodData.put("inventory",
                        this.inventory.serializeNBT(registries));
    tag.put(TutorialMod.MODID, tutorialmodData);
  }
  
  public ItemStackHandler getInventory() {
    return this.inventory;
  }
  
  public ItemStack getItem() {
    return this.inventory.getStackInSlot(0);
  }
  
  public void setItem(ItemStack stack) {
    this.inventory.setStackInSlot(0, stack);
  }
  
  
}

