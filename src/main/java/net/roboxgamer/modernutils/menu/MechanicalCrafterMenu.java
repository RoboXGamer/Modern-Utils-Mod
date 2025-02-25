package net.roboxgamer.modernutils.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

import static net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity.*;

public class MechanicalCrafterMenu extends AbstractContainerMenu {
  
  private final MechanicalCrafterBlockEntity blockEntity;
  private final ContainerLevelAccess levelAccess;
  private final ContainerData containerData;
  
  // Constants for addon slots positions relative to the animated tab in the screen
  // These need to match the visual positions in the animated tab
  public static final int ADDON_SLOT_SIZE = AddonManager.ADDON_SLOT_SIZE; // Now using AddonManager's constant
  public static final int ADDON_SLOT_PADDING = AddonManager.ADDON_SLOT_PADDING; // Now using AddonManager's constant
  public static final int ADDON_TAB_BUTTON_PADDING = AddonManager.ADDON_TAB_BUTTON_PADDING; // Now using AddonManager's constant
  public static final int INPUT_SLOTS_COUNT = 9;
  public static final int OUTPUT_SLOTS_COUNT = 9;
  
  public MechanicalCrafterMenu(int containerId, @NotNull Inventory playerInv, MechanicalCrafterBlockEntity blockEntity){
    this(containerId, playerInv, blockEntity, new SimpleContainerData(10));
  }
  //Server Constructor
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
    ItemStackHandler addonItemHandler = blockEntity.getAddonSlotsItemHandler(); // Get addon handler
    MechanicalCrafterBlockEntity.CraftingSlotHandler craftingItemHandler = blockEntity.getCraftingSlotsItemHandler();
    //add result slot
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
    //add recipe slots
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 3; col++) {
        this.addSlot(
            new CraftingGhostSlotItemHandler(craftingItemHandler, CRAFT_RECIPE_SLOTS[row * 3 + col], 26 + col * 18,
                                             18 + row * 18));
      }
    }
    //add input slots
    for (int col = 0; col < INPUT_SLOTS_COUNT; col++) {
      this.addSlot(new SlotItemHandler(inputItemHandler, col, 8 + col * 18, inputSlotsYStart));
    }
    // add output slots
    for (int col = 0; col < OUTPUT_SLOTS_COUNT; col++) {
      this.addSlot(new OutputSlotItemHandler(outputItemHandler, col, 8 + col * 18, outputSlotsYStart,this));
    }
    
    // Add addon slots in a 2x2 grid with positions matching the animated tab
    // These will be rendered in the top-right corner when the addon tab is opened
    int addonStartX = 176;
    int addonStartY = 0;
    
    for (int row = 0; row < 2; row++) {
      for (int col = 0; col < 2; col++) {
        final int slotIndex = row * 2 + col;
        int xPos = addonStartX + ADDON_SLOT_PADDING + col * (ADDON_SLOT_SIZE + ADDON_SLOT_PADDING);
        int yPos = addonStartY + ADDON_TAB_BUTTON_PADDING + row * (ADDON_SLOT_SIZE + ADDON_SLOT_PADDING);
        
        this.addSlot(new SlotItemHandler(addonItemHandler, slotIndex, xPos, yPos) {
          @Override
          public boolean mayPlace(@NotNull ItemStack stack) {
            // Allow all valid speed upgrades
            return stack.getItem() == Items.COAL_BLOCK ||
                   stack.getItem() == Items.IRON_BLOCK ||
                   stack.getItem() == Items.GOLD_BLOCK ||
                   stack.getItem() == Items.REDSTONE_BLOCK ||
                   stack.getItem() == Items.DIAMOND_BLOCK ||
                   stack.getItem() == Items.NETHERITE_BLOCK ||
                   stack.getItem() == Items.AMETHYST_BLOCK;
          }
        });
      }
    }
  }
  
  public boolean isSlotDisabled(int slot) {
    return slot > -1 && slot < 9 && this.containerData.get(slot) == 1;
  }
  
  //Client Constructor
  public MechanicalCrafterMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    this(containerId, playerInv,
         (MechanicalCrafterBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
  }
  
  @Override
  public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);
    
    if (slot.hasItem()) {
      ItemStack stackInSlot = slot.getItem();
      itemstack = stackInSlot.copy();
      
      int playerInventoryStart = INPUT_SLOTS_COUNT + OUTPUT_SLOTS_COUNT + this.blockEntity.getAddonManager().ADDON_SLOTS_COUNT + 10;  // Adjust for addon slots
      int playerHotbarStart = playerInventoryStart + 27;
      int playerHotbarEnd = playerHotbarStart + 9;
      
      // Calculate the range of addon slot indices
      int addonStartIndex = INPUT_SLOTS_COUNT + OUTPUT_SLOTS_COUNT + 10;
      int addonEndIndex = addonStartIndex + this.blockEntity.getAddonManager().ADDON_SLOTS_COUNT;
      
      // Moving from addon slots to player inventory
      if (index >= addonStartIndex && index < addonEndIndex) {
        if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, playerHotbarEnd, true)) {
          return ItemStack.EMPTY;
        }
        slot.onQuickCraft(stackInSlot, itemstack);
      }
      // Moving from output slots to player inventory
      else if (index >= INPUT_SLOTS_COUNT + 10 && index < INPUT_SLOTS_COUNT + OUTPUT_SLOTS_COUNT + 10) {
        if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, playerHotbarEnd, true)) {
          return ItemStack.EMPTY;
        }
        slot.onQuickCraft(stackInSlot, itemstack);
      }
      // Moving from input slots back to player inventory
      else if (index >= 10 && index < INPUT_SLOTS_COUNT + 10) {
        if (!this.moveItemStackTo(stackInSlot, playerInventoryStart, playerHotbarEnd, false)) {
          return ItemStack.EMPTY;
        }
      }
      // Moving from player inventory to input slots or addon slots
      else if (index >= playerInventoryStart) {
        // Try to move to addon slots if it's a valid upgrade
        if (blockEntity.getAddonManager().isAllowedItem(stackInSlot.getItem())) {
          if (this.moveItemStackTo(stackInSlot, addonStartIndex, addonEndIndex, false)) {
            // Success
          } else if (!this.moveItemStackTo(stackInSlot, 10, INPUT_SLOTS_COUNT + 10, false)) {
            return ItemStack.EMPTY;
          }
        } else if (!this.moveItemStackTo(stackInSlot, 10, INPUT_SLOTS_COUNT + 10, false)) {
          return ItemStack.EMPTY;
        }
      }
      
      if (stackInSlot.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
      
      if (stackInSlot.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }
      
      slot.onTake(player, stackInSlot);
    }
    
    return itemstack;
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
          if (button == 0) {  // Left-click (button 0)
            if (!heldItem.isEmpty()) {
              ghostSlot.increase(heldItem);
            }
          } else if (button == 1) {  // Right-click (button 1)
            if (heldItem.isEmpty()) {
              // If the player is not holding an item, remove the ghost item
              ghostSlot.removeItem();
            } else if (ItemStack.isSameItemSameComponents(ghostSlot.getItem(), heldItem)) {
              // If the player is holding the same item, decrease the count
              ghostSlot.decrease(heldItem);
            }
          }
        }
        return;  // No need to call super as we handle it
      }
    }
    
    // Call super to handle other slots normally
    super.clicked(slotId, button, clickType, player);
  }
  
  public void setSlotState(int slot, boolean enabled) {
    OutputSlotItemHandler outputSlot = (OutputSlotItemHandler)this.getSlot(slot);
    this.containerData.set(outputSlot.getSlotIndex(), enabled ? 0 : 1);
    this.blockEntity.setSlotState(outputSlot.getSlotIndex(), enabled ? 0 : 1);
    this.broadcastChanges();
  }
  
  @Override
  public boolean clickMenuButton(Player player, int id) {
    PackedButtonData packedButtonData = PackedButtonData.fromId(id);
    Constants.Sides side = packedButtonData.side();
    this.blockEntity.getSideManager().handleSideBtnClick(side, packedButtonData.shifted(), packedButtonData.clickAction());
    PacketDistributor.sendToPlayer((ServerPlayer) player,
                                   new SideStatePayload(side, this.blockEntity.getSideManager().getSideState(side), this.blockEntity.getBlockPos())
          );
    return true;
  }
}