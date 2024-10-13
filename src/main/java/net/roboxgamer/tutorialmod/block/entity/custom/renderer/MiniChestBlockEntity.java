package net.roboxgamer.tutorialmod.block.entity.custom.renderer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import net.roboxgamer.tutorialmod.menu.MiniChestMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniChestBlockEntity extends BlockEntity implements MenuProvider {
  private static final Component TITLE = Component.translatable("block.tutorialmod.mini_chest_block");
  public MiniChestBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.MINI_CHEST_BLOCK_ENTITY.get(), pos, blockState);
  }
  
  private final ItemStackHandler inv = new ItemStackHandler(1){
    @Override
    protected void onContentsChanged(int slot) {
      MiniChestBlockEntity.this.setChanged();
    }
  };
  
  public ItemStackHandler getInv() {
    return this.inv;
  }
  
  public SimpleContainer getInvContainer() {
    var t = new SimpleContainer(inv.getSlots());
    for (int i = 0; i < inv.getSlots(); i++) {
      t.setItem(i, inv.getStackInSlot(i));
    }
    return t;
  }
  
  //  Saving and loading
  
  CompoundTag getBEData(HolderLookup.Provider registries) {
    CompoundTag beData = new CompoundTag();
    // Serialize the inventory
    beData.put("inv", this.inv.serializeNBT(registries));
    return beData;
  }
  
  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    CompoundTag tutorialModData = getBEData(registries);
    tag.put(TutorialMod.MODID, tutorialModData);
  }
  
  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);
    // Check if we are on the client side
    if (level != null && level.isClientSide()) {
      // Deserialize data from the tag for client-side
      deserializeFromTag(tag, registries);
    } else {
      CompoundTag tutorialModData = tag.getCompound(TutorialMod.MODID);
      deserializeFromTag(tutorialModData, registries);
    }
  }
  
  private void deserializeFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    // Deserialize the inventory
    this.inv.deserializeNBT(registries, tag.getCompound("inv"));
  }
  
  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
    return getBEData(registries);
  }
  
  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
  
  
  
  
  
  // Menu
  @Override
  public @Nullable AbstractContainerMenu createMenu
  (int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
    return new MiniChestMenu(containerId, playerInventory, this);
  }
  
  @Override
  public @NotNull Component getDisplayName() {
    return TITLE;
  }
}
