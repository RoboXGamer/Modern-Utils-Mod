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
import net.roboxgamer.tutorialmod.block.entity.custom.renderer.MiniChestBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity.*;

public class MiniChestMenu extends AbstractContainerMenu {
  
  private final MiniChestBlockEntity blockEntity;
  private final ContainerLevelAccess levelAccess;
  
  //Server Constructor
  public MiniChestMenu(int containerId, @NotNull Inventory playerInv, BlockEntity blockEntity) {
    super(ModMenuTypes.MINI_CHEST_MENU.get(), containerId);
    if (blockEntity instanceof MiniChestBlockEntity be) {
      this.blockEntity = be;
    } else {
      throw new IllegalArgumentException("BlockEntity is not a MechanicalCrafterBlockEntity");
    }
    
    this.levelAccess = ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()),
                                                   blockEntity.getBlockPos());
    
    createPlayerHotbar(playerInv);
    createPlayerInventory(playerInv);
    
    createBlockEntityInventory(this.blockEntity);
  }
  
  
  private void createPlayerInventory(@NotNull Inventory playerInv) {
    var playerInvYStart = 140;
    for (int row = 0; row < 3; ++row) {
      for (int col = 0; col < 9; ++col) {
        this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvYStart + row * 18));
      }
    }
  }
  
  private void createPlayerHotbar(@NotNull Inventory playerInv) {
    var hotbarYStart = 198;
    for (int col = 0; col < 9; ++col) {
      this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarYStart));
    }
  }
  
  private void createBlockEntityInventory(MiniChestBlockEntity blockEntity){
    int centerX = 80;
    for (int i = 0; i < blockEntity.getInv().getSlots(); i++){
      this.addSlot(
          new SlotItemHandler(blockEntity.getInv(), i, i * 18 + centerX, 54)
      );
    }
  }
  
  //Client Constructor
  public MiniChestMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    this(containerId, playerInv, playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
  }
  
  
  // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
  // must assign a slot number to each of the slots used by the GUI.
  // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
  // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
  //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
  //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
  //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
  private static final int HOTBAR_SLOT_COUNT = 9;
  private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
  private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
  private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
  private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
  private static final int VANILLA_FIRST_SLOT_INDEX = 0;
  private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
  
  // THIS YOU HAVE TO DEFINE!
  private static final int TE_INVENTORY_SLOT_COUNT = 1;  // must be the number of slots you have!
  @Override
  public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int pIndex) {
    Slot sourceSlot = slots.get(pIndex);
    if (!sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
    ItemStack sourceStack = sourceSlot.getItem();
    ItemStack copyOfSourceStack = sourceStack.copy();
    
    // Check if the slot clicked is one of the vanilla container slots
    if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
      // This is a vanilla container slot so merge the stack into the tile inventory
      if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
          + TE_INVENTORY_SLOT_COUNT, false)) {
        return ItemStack.EMPTY;  // EMPTY_ITEM
      }
    } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
      // This is a TE slot so merge the stack into the players inventory
      if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
        return ItemStack.EMPTY;
      }
    } else {
      System.out.println("Invalid slotIndex:" + pIndex);
      return ItemStack.EMPTY;
    }
    // If stack size == 0 (the entire stack was moved) set slot contents to null
    if (sourceStack.getCount() == 0) {
      sourceSlot.set(ItemStack.EMPTY);
    } else {
      sourceSlot.setChanged();
    }
    sourceSlot.onTake(playerIn, sourceStack);
    return copyOfSourceStack;
  }
  
  @Override
  public boolean stillValid(@NotNull Player player) {
    return stillValid(this.levelAccess, player, ModBlocks.MINI_CHEST_BLOCK.get());
  }
  
  public MiniChestBlockEntity getBlockEntity() {
    return this.blockEntity;
  }
}