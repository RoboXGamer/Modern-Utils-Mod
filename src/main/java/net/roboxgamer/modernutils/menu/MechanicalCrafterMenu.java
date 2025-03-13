package net.roboxgamer.modernutils.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.network.SideStatePayload;
import net.roboxgamer.modernutils.util.AddonManager;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.PackedButtonData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity.CRAFT_RECIPE_SLOTS;
import static net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity.RESULT_SLOT;

public class MechanicalCrafterMenu extends AbstractContainerMenu {
  
  private final MechanicalCrafterBlockEntity blockEntity;
  private final ContainerLevelAccess levelAccess;
  private final ContainerData containerData;

  public static final int INPUT_SLOTS_COUNT = 9;
  public static final int OUTPUT_SLOTS_COUNT = 9;
  
  public MechanicalCrafterMenu(int containerId, @NotNull Inventory playerInv,
  MechanicalCrafterBlockEntity blockEntity) {
    this(containerId, playerInv, blockEntity, new SimpleContainerData(10));
  }
  
  // Server Constructor
  public MechanicalCrafterMenu(int containerId, @NotNull Inventory playerInv, MechanicalCrafterBlockEntity blockEntity, ContainerData data) {
    super(ModMenuTypes.MECHANICAL_CRAFTER_MENU.get(), containerId);
    this.blockEntity = blockEntity;
    this.levelAccess = ContainerLevelAccess.create(Objects.requireNonNull(blockEntity.getLevel()),
                                                   blockEntity.getBlockPos());
    this.containerData = data;
    this.addDataSlots(data);
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

    // add result slot
    this.addSlot(new SlotItemHandler(craftingItemHandler, RESULT_SLOT, 98, 36) {
      @Override
      public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
      }
      
      @Override
      public boolean mayPickup(@NotNull Player playerIn) {
        return false;
      }
    });

    // add recipe slots
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        this.addSlot(
            new CraftingGhostSlotItemHandler(craftingItemHandler, CRAFT_RECIPE_SLOTS[row * 3 + col], 26 + col * 18,
                                             18 + row * 18));
      }
    }

    // add input slots
    for (int col = 0; col < INPUT_SLOTS_COUNT; col++) {
      this.addSlot(new SlotItemHandler(inputItemHandler, col, 8 + col * 18, inputSlotsYStart));
    }

    // add output slots
    for (int col = 0; col < OUTPUT_SLOTS_COUNT; col++) {
      this.addSlot(new OutputSlotItemHandler(outputItemHandler, col, 8 + col * 18, outputSlotsYStart, this));
    }
    
    // Create addon slots using AddonManager helper
    int addonStartX = 176;
    int addonStartY = 0;
    blockEntity.getAddonManager().createAddonSlots(addonStartX, addonStartY);
    
    // Add the addon slots to the menu
    for (AddonManager.AddonSlotItemHandler handler : blockEntity.getAddonManager().getAddonSlotHandlers()) {
      this.addSlot(handler);
    }
  }
  
  public boolean isSlotDisabled(int slot) {
    return slot > -1 && slot < 9 && this.containerData.get(slot) == 1;
  }

  // Client Constructor
  public MechanicalCrafterMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    this(containerId, playerInv,
         (MechanicalCrafterBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
  }
  
  private void updateSourceSlot(Slot slot, ItemStack remaining, ItemStack original) {
    if (remaining.isEmpty()) {
        slot.set(ItemStack.EMPTY);
    } else {
        slot.set(remaining);
        slot.setChanged();
    }
}

private ItemStack tryInsertIntoHandler(ItemStackHandler handler, ItemStack stack) {
    if (stack.isEmpty() || handler == null) {
        return stack;
    }

    ItemStack toInsert = stack.copy();
    
    // First try to fill existing stacks of the same type
    for (int i = 0; i < handler.getSlots() && !toInsert.isEmpty(); i++) {
        ItemStack slotStack = handler.getStackInSlot(i);
        if (!slotStack.isEmpty() && 
            ItemStack.isSameItemSameComponents(slotStack, toInsert) && 
            slotStack.getCount() < slotStack.getMaxStackSize()) {
            toInsert = handler.insertItem(i, toInsert, false);
        }
    }
    
    // Then try empty slots
    if (!toInsert.isEmpty()) {
        for (int i = 0; i < handler.getSlots() && !toInsert.isEmpty(); i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                toInsert = handler.insertItem(i, toInsert, false);
            }
        }
    }
    
    return toInsert;
}

@Override
public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    
    if (!slot.hasItem()) {
        return ItemStack.EMPTY;
    }

    ItemStack stackInSlot = slot.getItem();
    itemstack = stackInSlot.copy();
    
    int inputStartIndex = 10;
    int outputStartIndex = inputStartIndex + INPUT_SLOTS_COUNT;
    int addonStartIndex = outputStartIndex + OUTPUT_SLOTS_COUNT;
    int addonEndIndex = addonStartIndex + this.blockEntity.getAddonManager().ADDON_SLOTS_COUNT;
    int playerInventoryStart = addonEndIndex;
    
    // Moving from output slots needs special handling
    if (index >= outputStartIndex && index < addonStartIndex) {
        if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, playerInventoryStart + 36, true)) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stackInSlot); // Handle any special output slot behavior
    }
    // Moving from player inventory
    else if (index >= playerInventoryStart) {
        ItemStack remaining = stackInSlot.copy();
        boolean handled = false;
        boolean isAnAddonItem = blockEntity.getAddonManager().isAllowedItem(remaining.getItem());
        boolean addonSlotIsActive = this.blockEntity.getAddonManager().getAddonSlotHandlers().stream()
            .anyMatch(AddonManager.AddonSlotItemHandler::isActive);
        
        // Try addon slots first if valid upgrade and addon tab is active
        if (isAnAddonItem && addonSlotIsActive) {
            remaining = tryInsertIntoHandler(blockEntity.getAddonSlotsItemHandler(), remaining);
            handled = remaining.getCount() != stackInSlot.getCount();
        }
        
        // If not handled by addon slots or still has items, try input slots
        if (!handled || !remaining.isEmpty()) {
            remaining = tryInsertIntoHandler(blockEntity.getInputSlotsItemHandler(), remaining);
        }
        
        updateSourceSlot(slot, remaining, stackInSlot);
        if (remaining.getCount() == stackInSlot.getCount()) {
            return ItemStack.EMPTY; // Nothing was moved
        }
        
        slot.setChanged();
        return itemstack;
    }
    // Moving from addon/input slots to player inventory
    else if (index >= inputStartIndex) {
        if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, playerInventoryStart + 36, true)) {
            return ItemStack.EMPTY;
        }
        slot.setChanged();
    }

    return stackInSlot.isEmpty() ? itemstack : ItemStack.EMPTY;
}

@Override
public boolean stillValid(@NotNull Player player) {
    return stillValid(this.levelAccess, player, ModBlocks.MECHANICAL_CRAFTER_BLOCK.get());
}

public MechanicalCrafterBlockEntity getBlockEntity() {
    return this.blockEntity;
}

/**
 * Gets the current crafting progress as a value between 0.0 and 1.0.
 *
 * @return Current progress as a float where 0.0 = 0% and 1.0 = 100%
 */
public float getCraftingProgress() {
    int currentProgress = MechanicalCrafterBlockEntity.getProgressFromContainerData(this.containerData);
    int maxProgress = MechanicalCrafterBlockEntity.getMaxCraftingTime();
    return (float) currentProgress / maxProgress;
}

@Override
public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
    // Check if the clicked slot is a CraftingGhostSlotItemHandler
    if (slotId >= 0 && slotId < this.slots.size()) {
      Slot slot = this.slots.get(slotId);
      
      if (slot instanceof CraftingGhostSlotItemHandler ghostSlot) {
        ItemStack heldItem = this.getCarried(); // Item currently held by the player
        
        if (clickType == ClickType.PICKUP) {
          if (button == 0) { // Left-click (button 0)
            if (!heldItem.isEmpty()) {
              ghostSlot.increase(heldItem);
            }
          } else if (button == 1) { // Right-click (button 1)
            if (heldItem.isEmpty()) {
              // If the player is not holding an item, remove the ghost item
              ghostSlot.removeItem();
            } else if (ItemStack.isSameItemSameComponents(ghostSlot.getItem(), heldItem)) {
              // If the player is holding the same item, decrease the count
              ghostSlot.decrease(heldItem);
            }
          }
        }
        return; // No need to call super as we handle it
      }
    }
    
    // Call super to handle other slots normally
    super.clicked(slotId, button, clickType, player);
}

public void setSlotState(int slot, boolean enabled) {
    OutputSlotItemHandler outputSlot = (OutputSlotItemHandler) this.getSlot(slot);
    this.containerData.set(outputSlot.getSlotIndex(), enabled ? 0 : 1);
    this.blockEntity.setSlotState(outputSlot.getSlotIndex(), enabled ? 0 : 1);
    this.broadcastChanges();
}

public boolean toggleAddonSlots() {
    return this.blockEntity.getAddonManager().toggleAddonSlots();
}

@Override
public boolean clickMenuButton(Player player, int id) {
    if (id == AddonManager.ADDON_TAB_TOGGLE_BUTTON_ID) {
        return this.toggleAddonSlots();
    }
    
    PackedButtonData packedButtonData = PackedButtonData.fromId(id);
    Constants.Sides side = packedButtonData.side();
    var sideManager = this.blockEntity.getSideManager();
    sideManager.handleSideBtnClick(side, packedButtonData.shifted(),packedButtonData.clickAction());
    PacketDistributor.sendToPlayer((ServerPlayer) player,
                                   new SideStatePayload(side, this.blockEntity.getSideManager().getSideState(side),
                                                        this.blockEntity.getBlockPos()));
    return true;
}

}