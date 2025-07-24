package net.roboxgamer.modernutils.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.roboxgamer.modernutils.client.screen.AnimatedTab;
import net.roboxgamer.modernutils.client.screen.ExtendedButton;

/**
 * AddonClientManager handles client-side addon UI functionality.
 * Manages the addon tab and config button rendering and interactions.
 */
public class AddonClientManager {
    private final AddonManager addonManager;
    private AnimatedTab addonTab;
    private ExtendedButton addonConfigBtn;

    public AddonClientManager(AddonManager addonManager) {
        this.addonManager = addonManager;
    }

    /**
     * Create the addon tab and config button for the screen.
     */
    public void createAddonTab(Player player, AbstractContainerScreen<?> screen) {
        // Create addon tab in top right corner
        this.addonTab = new AnimatedTab(46, 68, null, ExtendedButton.WidgetPosition.TOP_RIGHT);
        this.addonManager.getAddonSlotHandlers().forEach(handler -> handler.setActive(false));

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
                float offset = (extendedButton.getWidth() - (16 * scale)) / 2;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
                guiGraphics.pose().scale(scale, scale, 1);
                guiGraphics.renderFakeItem(Items.DIAMOND_BLOCK.getDefaultInstance(), 0, 0);
                guiGraphics.pose().popPose();
            }
        };
    }

    private void setAddonSlotHandlerStates(boolean isOpen) {
        this.addonManager.getAddonSlotHandlers().forEach(handler -> handler.setActive(isOpen));
    }

    /**
     * Render an addon slot in the GUI.
     */
    public void renderAddonSlot(GuiGraphics guiGraphics, Slot slot) {
        if (addonTab != null && addonTab.isOpen() && slot.isActive()) {
            guiGraphics.blitSprite(AddonManager.ADDON_SLOT_LOCATION_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
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
     * Toggle addon slots active state and synchronize with tab.
     */
    public boolean toggleAddonSlots() {
        boolean newState = !this.addonManager.getAddonSlotHandlers().getFirst().isActive();
        this.addonManager.getAddonSlotHandlers().forEach(handler -> handler.setActive(newState));
        if (this.addonTab != null) {
            // Synchronize tab state with slot state
            if (this.addonTab.isOpen() != newState) {
                this.addonTab.toggleOpen();
            }
        }
        return true;
    }
}