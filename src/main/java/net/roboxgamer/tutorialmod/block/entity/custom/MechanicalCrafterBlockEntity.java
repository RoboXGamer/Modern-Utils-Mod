package net.roboxgamer.tutorialmod.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class MechanicalCrafterBlockEntity extends BlockEntity implements MenuProvider {
  public Component TITLE = Component.translatable("block.tutorialmod.mechanical_crafter_block");
  
  private int tc = 0;
  
  
  ItemStackHandler inputSlots = new ItemStackHandler(9) {
    @Override
    protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      MechanicalCrafterBlockEntity.this.setChanged();
    }
  };
  ItemStackHandler outputSlots = new ItemStackHandler(9) {
    @Override
    protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      MechanicalCrafterBlockEntity.this.setChanged();
    }
  };
  ItemStackHandler craftingSlots = new ItemStackHandler(10) {
    @Override
    protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      MechanicalCrafterBlockEntity.this.setChanged();
    }
  };
  
  public static final int INPUT_SLOTS_COUNT = 9;
  public static final int OUTPUT_SLOTS_COUNT = 9;
  public static final int CRAFT_RESULT_SLOT = 0;
  public static final int[] CRAFT_RECIPE_SLOTS = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
  
  Lazy<ItemStackHandler> inputSlotsLazy = Lazy.of(() -> new ItemStackHandler(inputSlots.getSlots()));
  Lazy<ItemStackHandler> outputSlotsLazy = Lazy.of(() -> new ItemStackHandler(outputSlots.getSlots()));
  Lazy<ItemStackHandler> craftingSlotsLazy = Lazy.of(() -> new ItemStackHandler(craftingSlots.getSlots()));
  
  
  
  public MechanicalCrafterBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.MECHANICAL_CRAFTER_BE.get(), pos, blockState);
  }

  public ItemStackHandler getInputSlotsItemHandler() {
    return this.inputSlots;
  }
  
  public ItemStackHandler getOutputSlotsItemHandler() {
    return this.outputSlots;
  }
  
  public ItemStackHandler getCraftingSlotsItemHandler() {
    return this.craftingSlots;
  }
  
  public SimpleContainer getInputContainer() {
    var t = new SimpleContainer(inputSlots.getSlots());
    for (int i = 0; i < inputSlots.getSlots(); i++) {
      t.setItem(i, inputSlots.getStackInSlot(i));
    }
    return t;
  }
  
  public SimpleContainer getOutputContainer() {
    var t = new SimpleContainer(outputSlots.getSlots());
    for (int i = 0; i < outputSlots.getSlots(); i++) {
      t.setItem(i, outputSlots.getStackInSlot(i));
    }
    return t;
  }

  @Override
  public @NotNull Component getDisplayName() {
    return TITLE;
  }


  public void tick() {
//    TODO: Ticking logic
    this.tc++;
    if (this.tc == 20 * 60) this.tc = 0;
    
    Level level = this.getLevel();
    if (level == null) return;
    if (!level.isClientSide()) {
      if (level instanceof ServerLevel slevel) {
        BlockEntity blockEntity = slevel.getBlockEntity(this.getBlockPos());
        if (everySecond()) {
          if (!(blockEntity instanceof MechanicalCrafterBlockEntity be)) return;
          //  TODO: Logic for crafting
          ItemStackHandler inputSlots = be.getInputSlotsItemHandler();
          ItemStackHandler outputSlots = be.getOutputSlotsItemHandler();
          ItemStack input = inputSlots.getStackInSlot(0);
          //  Assuming the crafting recipe of oak_logs to oak_planks
          boolean toCraft = false;
          var slot = -1;
          ItemStack result = new ItemStack(Blocks.OAK_PLANKS, 4);
          for (int i = 0; i < inputSlots.getSlots(); i++) {
            ItemStack stack = inputSlots.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() != Blocks.OAK_LOG.asItem()) continue;
            //  Assuming the crafting recipe of oak_logs to oak_planks
            toCraft = true;
            slot = i;
            break;
          }
          if (!toCraft) return;
          for (int i = 0; i < outputSlots.getSlots(); i++) {
            ItemStack stack = outputSlots.getStackInSlot(i);
            if (stack.isEmpty()) {
              outputSlots.setStackInSlot(i, result);
              toCraft = false;
              break;
            }
            if (stack.getItem() == result.getItem()) {
              if (stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(4);
                outputSlots.setStackInSlot(i, stack);
                toCraft = false;
                break;
              }
            }
          }
          if (toCraft) return;
          //  Consume input
          var iStack = inputSlots.getStackInSlot(slot);
          iStack.shrink(1);
          inputSlots.setStackInSlot(slot, iStack);
        }
      }
      //level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }
  }
  
  
  
  private boolean everySecond() {
    return this.tc % 20 == 0;
  }
  
  CompoundTag getTutorialModData(HolderLookup.Provider registries) {
    CompoundTag tutorialModData = new CompoundTag();
    tutorialModData.put("inputInv",
                        this.inputSlots.serializeNBT(registries));
    tutorialModData.put("outputInv",
                        this.outputSlots.serializeNBT(registries));
    tutorialModData.put("craftingInv",
                        this.craftingSlots.serializeNBT(registries));
    return tutorialModData;
  }

  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put(TutorialMod.MODID, getTutorialModData(registries));
  }

  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);
    CompoundTag tutorialmodData = tag.getCompound(TutorialMod.MODID);
    this.inputSlots.deserializeNBT
        (registries, tutorialmodData.getCompound("inputInv"));
    this.outputSlots.deserializeNBT
        (registries, tutorialmodData.getCompound("outputInv"));
    this.craftingSlots.deserializeNBT
        (registries, tutorialmodData.getCompound("craftingInv"));
  }
  
  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
    return getTutorialModData(registries);
  }
  
  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
  
  @Override
  public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
    return new MechanicalCrafterMenu(containerId, playerInventory, this);
  }
}
