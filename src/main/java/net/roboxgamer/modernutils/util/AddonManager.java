package net.roboxgamer.modernutils.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * AddonManager handles addon-related functionality for blocks that support addons.
 * It manages addon slots and provides methods for rendering and handling addon slots.
 */
public class AddonManager {
    private final BlockEntity blockEntity;
    
    // Constants for addon slots
    public int ADDON_SLOTS_COUNT = 4;
    public static final int ADDON_SLOT_SIZE = 18;
    public static final int ADDON_SLOT_PADDING = 4;
    public static final int ADDON_TAB_BUTTON_PADDING = 24 + 2;
    
    // Resource location for addon slot sprite
    public static final ResourceLocation ADDON_SLOT_LOCATION_SPRITE = 
        ResourceLocation.withDefaultNamespace("container/slot");
    
    // Set of allowed items for this addon manager
    private final Set<Item> allowedItems;
    
    // Addon slots item handler
    private final CustomItemStackHandler addonSlots;
    
    /**
     * Creates a new AddonManager with the specified addon slots count and allowed items.
     * @param blockEntity The block entity this manager belongs to
     * @param addonSlotCount The number of addon slots
     * @param allowedItems The items that are allowed in the addon slots
     */
    public AddonManager(BlockEntity blockEntity, int addonSlotCount, Set<Item> allowedItems) {
        this.blockEntity = blockEntity;
        this.ADDON_SLOTS_COUNT = addonSlotCount;
        this.allowedItems = allowedItems;
        
        // Initialize addon slots handler
        this.addonSlots = new CustomItemStackHandler(ADDON_SLOTS_COUNT,blockEntity) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                // Only allow items from the allowed items set
                return allowedItems.contains(stack.getItem());
            }
            
            @Override
            public int getSlotLimit(int slot) {
                // Limit to 1 item per addon slot
                return 1;
            }
        };
    }
    
    /**
     * Gets the addon slots item handler.
     * @return The addon slots item handler
     */
    public CustomItemStackHandler getAddonSlots() {
        return this.addonSlots;
    }
    
    /**
     * Checks if an item is allowed in the addon slots.
     * @param item The item to check
     * @return True if the item is allowed
     */
    public boolean isAllowedItem(Item item) {
        return allowedItems.contains(item);
    }
    
    /**
     * Saves the addon manager data to NBT.
     * @param tag The tag to save to
     */
    public void saveToTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        // Save addon slots
        CompoundTag addonTag = addonSlots.serializeNBT(registries);
        tag.put("addonInv", addonTag);
    }
    
    /**
     * Loads the addon manager data from NBT.
     * @param tag The tag to load from
     */
    public void loadFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        // Load addon slots if they exist
        if (tag.contains("addonInv")) {
            this.addonSlots.deserializeNBT(registries, tag.getCompound("addonInv"));
        }
    }
} 