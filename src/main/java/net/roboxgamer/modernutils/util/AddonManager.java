package net.roboxgamer.modernutils.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.roboxgamer.modernutils.client.screen.AnimatedTab;
import net.roboxgamer.modernutils.client.screen.ExtendedButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * AddonManager handles addon-related functionality for blocks that support
 * addons.
 * It manages addon slots and provides methods for rendering and handling addon
 * slots.
 */
public class AddonManager {
  private final BlockEntity blockEntity;

  // Constants for addon slots
  public int ADDON_SLOTS_COUNT = 4;
  public static final int ADDON_SLOT_SIZE = 18;
  public static final int ADDON_SLOT_PADDING = 4;
  public static final int ADDON_TAB_BUTTON_PADDING = 24 + 2;
  public static final int ADDON_TAB_TOGGLE_BUTTON_ID = 9999;

  // Resource location for addon slot sprite
  public static final ResourceLocation ADDON_SLOT_LOCATION_SPRITE = ResourceLocation
      .withDefaultNamespace("container/slot");

  // Set of allowed items for this addon manager
  private final Set<Item> allowedItems;

  // Addon slots item handler
  private final CustomItemStackHandler addonSlots;

  // List of addon slot handlers for managing slot states
  private List<AddonSlotItemHandler> addonSlotHandlers;

  private AnimatedTab addonTab;
  private ExtendedButton addonConfigBtn;

  /**
   * Creates a new AddonManager with the specified addon slots count and allowed
   * items.
   */
  public AddonManager(BlockEntity blockEntity, int addonSlotCount, Set<Item> allowedItems) {
    this.blockEntity = blockEntity;
    this.ADDON_SLOTS_COUNT = addonSlotCount;
    this.allowedItems = allowedItems;
    // Initialize addon slots handler
    this.addonSlots = new CustomItemStackHandler(ADDON_SLOTS_COUNT, this.blockEntity) {
      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return isAllowedItem(stack.getItem());
      }

      @Override
      public int getSlotLimit(int slot) {
        return 1; // Limit to 1 item per addon slot
      }
    };

  }

  /**
   * Gets the addon slots item handler.
   */
  public CustomItemStackHandler getAddonSlots() {
    return this.addonSlots;
  }

  /**
   * Checks if an item is allowed in the addon slots.
   */
  public boolean isAllowedItem(Item item) {
    return allowedItems.contains(item);
  }

  /**
   * Saves the addon manager data to NBT.
   */
  public void saveToTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    // Save addon slots
    CompoundTag addonTag = addonSlots.serializeNBT(registries);
    tag.put("addonInv", addonTag);
  }

  /**
   * Loads the addon manager data from NBT.
   */
  public void loadFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    // Load addon slots if they exist
    if (tag.contains("addonInv")) {
      this.addonSlots.deserializeNBT(registries, tag.getCompound("addonInv"));
    }
  }

  /**
   * Creates addon slots in the menu at the specified position.
   */
  public void createAddonSlots(int startX, int startY) {
    this.addonSlotHandlers = new ArrayList<>();
    // Add addon slots in a 2x2 grid with positions matching the animated tab
    for (int row = 0; row < 2; row++) {
      for (int col = 0; col < 2; col++) {
        final int slotIndex = row * 2 + col;
        int xPos = startX + ADDON_SLOT_PADDING + col * (ADDON_SLOT_SIZE + ADDON_SLOT_PADDING);
        int yPos = startY + ADDON_TAB_BUTTON_PADDING + row * (ADDON_SLOT_SIZE + ADDON_SLOT_PADDING);
        var addonSlotHandler = new AddonSlotItemHandler(this.addonSlots, slotIndex, xPos, yPos);
        this.addonSlotHandlers.add(addonSlotHandler);
      }
    }
  }

  /**
   * Gets all addon slot handlers.
   */
  public List<AddonSlotItemHandler> getAddonSlotHandlers() {
    return this.addonSlotHandlers;
  }

  /**
   * Toggle addon slots active state.
   */
  public boolean toggleAddonSlots() {
    boolean newState = !this.addonSlotHandlers.getFirst().isActive();
    this.addonSlotHandlers.forEach(handler -> handler.setActive(newState));
    if (this.addonTab != null) {
      // Synchronize tab state with slot state
      if (this.addonTab.isOpen() != newState) {
        this.addonTab.toggleOpen();
      }
    }
    return true;
  }

  /**
   * Create the addon tab and config button for the screen.
   */
  public void createAddonTab(Player player, AbstractContainerScreen<?> screen) {
    // Create addon tab in top right corner
    this.addonTab = new AnimatedTab(46, 68, null, ExtendedButton.WidgetPosition.TOP_RIGHT);
    this.addonSlotHandlers.forEach(handler -> handler.setActive(false));

    // Create addon config button in top right
    this.addonConfigBtn = new ExtendedButton(
        "AddonConfig_Btn",
        24, 24,
        Component.literal("Addons"),
        true,
        ExtendedButton.WidgetPosition.TOP_RIGHT,
        (button, clickAction, mouseX, mouseY) -> {
          if (this.addonTab != null) {

            boolean isOpen = this.addonTab.toggleOpen();
            this.setAddonSlotHandlerStates(isOpen);
            screen.getMinecraft().gameMode
                .handleInventoryButtonClick(
                    screen.getMenu().containerId,
                    AddonManager.ADDON_TAB_TOGGLE_BUTTON_ID);
          }
        },
        player) {
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
          ExtendedButton extendedButton) {
        float scale = 1;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(Items.DIAMOND_BLOCK.getDefaultInstance(),
            0,
            0);
        guiGraphics.pose().popPose();
      }
    };
  }

  private void setAddonSlotHandlerStates(boolean isOpen) {
    this.addonSlotHandlers.forEach(handler -> handler.setActive(isOpen));
  }

  /**
   * Render an addon slot in the GUI.
   */
  public void renderAddonSlot(GuiGraphics guiGraphics, Slot slot) {
    if (addonTab != null && addonTab.isOpen() && slot.isActive()) {
      guiGraphics.blitSprite(ADDON_SLOT_LOCATION_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
    }
  }

  /**
   * Get the addon tab widget.
   */
  public AnimatedTab getAddonTab() {
    return this.addonTab;
  }

  /**
   * Get the addon config button widget.
   */
  public ExtendedButton getAddonConfigButton() {
    return this.addonConfigBtn;
  }

  /**
   * Represents a slot that can hold addon items.
   */
  public static class AddonSlotItemHandler extends SlotItemHandler {
    private boolean active = false;

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
      return this.getItemHandler().isItemValid(this.getSlotIndex(), stack);
    }

    @Override
    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
      this.setChanged();
    }

    public AddonSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
      super(itemHandler, index, xPosition, yPosition);
    }
  }
}
