package net.roboxgamer.tutorialmod.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.roboxgamer.tutorialmod.block.ModBlocks;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity.*;

public class MechanicalCrafterMenu extends AbstractContainerMenu {
  
  private final MechanicalCrafterBlockEntity blockEntity;
  private final ContainerLevelAccess levelAccess;
  //Server Constructor
  public MechanicalCrafterMenu(int containerId, @NotNull Inventory playerInv, BlockEntity blockEntity) {
    super(ModMenuTypes.MECHANICAL_CRAFTER_MENU.get(), containerId);
    if (blockEntity instanceof MechanicalCrafterBlockEntity mechanicalCrafterBlockEntity) {
      this.blockEntity = mechanicalCrafterBlockEntity;
    }
    else {
      throw new IllegalArgumentException("BlockEntity is not a MechanicalCrafterBlockEntity");
    }
    
    this.levelAccess = ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()), blockEntity.getBlockPos());
    createBlockEntityInventory(this.blockEntity);
    
    createPlayerHotbar(playerInv);
    createPlayerInventory(playerInv);
  }
  
  
  
  private void createPlayerInventory(@NotNull Inventory playerInv) {
    var playerInvYStart = 154;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 9; ++col) {
        this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvYStart + row * 18));
      }
    }
  }
  
  private void createPlayerHotbar(@NotNull Inventory playerInv) {
    var hotbarYStart = 212;
    for (int col = 0; col < 9; ++col) {
      this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarYStart));
    }
  }
  
  private void createBlockEntityInventory(MechanicalCrafterBlockEntity blockEntity) {
    var inputSlotsYStart = 90;
    var outputSlotsYStart = 122;
    ItemStackHandler inputItemHandler = blockEntity.getInputSlotsItemHandler();
    ItemStackHandler outputItemHandler = blockEntity.getOutputSlotsItemHandler();
    MechanicalCrafterBlockEntity.CraftingSlotHandler craftingItemHandler = blockEntity.getCraftingSlotsItemHandler();
    //add result slot
    this.addSlot(new SlotItemHandler(craftingItemHandler, CRAFT_RESULT_SLOT, 98, 36){
      @Override
      public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
      }
    });
    //add recipe slots
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        this.addSlot(
            new CraftingGhostSlotItemHandler(craftingItemHandler, CRAFT_RECIPE_SLOTS[row * 3 + col], 26 + col * 18, 18 + row * 18));
      }
    }
    //add input slots
    for (int col = 0; col < INPUT_SLOTS_COUNT; col++) {
      this.addSlot(new SlotItemHandler(inputItemHandler, col, 8 + col * 18, inputSlotsYStart));
    }
    // add output slots
    for (int col = 0; col < OUTPUT_SLOTS_COUNT; col++) {
      this.addSlot(new SlotItemHandler(outputItemHandler, col, 8 + col * 18, outputSlotsYStart));
    }
  }
  
  //Client Constructor
  public MechanicalCrafterMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    this(containerId, playerInv, playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
  }
  
  @Override
  public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
    return ItemStack.EMPTY;
  }
  
  @Override
  public boolean stillValid(@NotNull Player player) {
    return stillValid(this.levelAccess, player, ModBlocks.MECHANICAL_CRAFTER_BLOCK.get());
  }
  
  public MechanicalCrafterBlockEntity getBlockEntity() {
    return this.blockEntity;
  }
  
  
}