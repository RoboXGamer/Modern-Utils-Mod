package net.roboxgamer.modernutils.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalFurnaceBlockEntity;
import net.roboxgamer.modernutils.network.SideStatePayload;
import net.roboxgamer.modernutils.util.AddonManager;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.Constants.ISidedMachine;
import net.roboxgamer.modernutils.util.PackedButtonData;

import org.jetbrains.annotations.NotNull;

public class MechanicalFurnaceMenu extends AbstractContainerMenu {
  public static final int ADDON_TAB_TOGGLE_BUTTON_ID = 9999;
  public final MechanicalFurnaceBlockEntity blockEntity;
  private final Level level;
  private final ContainerData data;
  public static final int SLOT_COUNT = 17; // 8 input + 8 output + 1 fuel
  public static final int INPUT_SLOTS_START = 0;
  public static final int INPUT_SLOTS_COUNT = 8;
  public static final int OUTPUT_SLOTS_START = 8;
  public static final int OUTPUT_SLOTS_COUNT = 8;
  public static final int FUEL_SLOT = 16;
  private static final int CONTAINER_PADDING = 4;
  private static final int SLOT_SPACING = 2;
  private static final int SLOT_SIZE = 18;
  private static final int TITLE_HEIGHT = 22;

  // Client Constructor
  public MechanicalFurnaceMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
    this(containerId, playerInv,
        (MechanicalFurnaceBlockEntity) playerInv.player.level().getBlockEntity(extraData.readBlockPos()));
  }

  public MechanicalFurnaceMenu(int containerId, @NotNull Inventory playerInv,
      MechanicalFurnaceBlockEntity blockEntity) {
    this(containerId, playerInv, blockEntity, new SimpleContainerData(10));
  }

  public MechanicalFurnaceMenu(int id, Inventory playerInv, MechanicalFurnaceBlockEntity be, ContainerData data) {
    super(ModMenuTypes.MECHANICAL_FURNACE_MENU.get(), id);
    checkContainerSize(playerInv, SLOT_COUNT);
    this.blockEntity = be;
    this.level = playerInv.player.level();
    this.data = data;

    var inputHandler = blockEntity.getInputHandler();
    var outputHandler = blockEntity.getOutputHandler();
    var fuelHandler = blockEntity.getFuelHandler();

    // Input slots in a row (top)
    for (int i = 0; i < INPUT_SLOTS_COUNT; i++) {
      int slotX = 1 + SLOT_SIZE + CONTAINER_PADDING * 2 + (i * (SLOT_SIZE + SLOT_SPACING));
      int slotY =  1 + TITLE_HEIGHT + CONTAINER_PADDING;
      this.addSlot(new SlotItemHandler(inputHandler, i, slotX, slotY));
    }
    
    // Create output slots
    for (int i = 0; i < OUTPUT_SLOTS_COUNT; i++) {
      int slotX = 1 + SLOT_SIZE + CONTAINER_PADDING * 2 + (i * (SLOT_SIZE + SLOT_SPACING));
      int slotY =  1 + TITLE_HEIGHT + CONTAINER_PADDING;
      int outputSlotY = slotY + SLOT_SIZE + SLOT_SPACING + 14 + SLOT_SPACING;
      this.addSlot(new SlotItemHandler(outputHandler, i, slotX, outputSlotY));
    }
    
    this.addSlot(new SlotItemHandler(fuelHandler, 0, 1 + CONTAINER_PADDING + SLOT_SPACING,  1 + TITLE_HEIGHT + CONTAINER_PADDING + SLOT_SIZE));

    // Create addon slots
    int addonStartX = 190;
    int addonStartY = 0;
    blockEntity.getAddonManager().createAddonSlots(addonStartX, addonStartY);
    
    // Add the addon slots to the menu
    for (AddonManager.AddonSlotItemHandler handler : blockEntity.getAddonManager().getAddonSlotHandlers()) {
      this.addSlot(handler);
    }

    addPlayerInventory(playerInv);
    addPlayerHotbar(playerInv);

    addDataSlots(data);
  }

  public int getScaledProgress(int slot) {
    if (slot < 0 || slot >= INPUT_SLOTS_COUNT)
      return 0;
    int progress = this.data.get(slot);
    int maxProgress = this.blockEntity.getMaxProgress();
    return progress != 0 ? progress * 100 / maxProgress : 0;
  }

  public boolean hasFuel() {
    return this.data.get(8) > 0; // Burn time
  }

  public int getFuelScaled() {
    int burnTime = this.data.get(8);
    int maxBurnTime = this.data.get(9);
    return maxBurnTime != 0 ? (burnTime * 100) / maxBurnTime : 0;
  }

  public int getSlots() {
    return INPUT_SLOTS_COUNT; // Returns 8, which matches the number of input slots
  }

  @Override
  public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
    ItemStack itemstack = ItemStack.EMPTY;
    Slot slot = this.slots.get(index);

    if (slot.hasItem()) {
      ItemStack slotStack = slot.getItem();
      itemstack = slotStack.copy();

      // Moving from furnace slots
      if (index < SLOT_COUNT) {
        // From output slots (8-15) to player inventory
        if (index >= OUTPUT_SLOTS_START && index < OUTPUT_SLOTS_START + OUTPUT_SLOTS_COUNT) {
          if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 36, true)) {
            return ItemStack.EMPTY;
          }
          slot.onQuickCraft(slotStack, itemstack);
        }
        // From fuel slot (16) to player inventory
        else if (index == FUEL_SLOT) {
          if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 36, false)) {
            return ItemStack.EMPTY;
          }
        }
        // From input slots (0-7) to player inventory
        else if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 36, false)) {
          return ItemStack.EMPTY;
        }
      }
      // Moving from player inventory
      else {
        // Try to move fuel items
        if (slotStack.getBurnTime(RecipeType.SMELTING) > 0) {
          // First try fuel slot
          if (!this.moveItemStackTo(slotStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
            // If fuel slot is full, try input slots
            if (!this.moveItemStackTo(slotStack, INPUT_SLOTS_START, INPUT_SLOTS_START + INPUT_SLOTS_COUNT,
                false)) {
              // If input slots are full, try moving between hotbar and main inventory
              if (index < SLOT_COUNT + 27) {
                // From main inventory to hotbar
                if (!this.moveItemStackTo(slotStack, SLOT_COUNT + 27, SLOT_COUNT + 36, false)) {
                  return ItemStack.EMPTY;
                }
              } else {
                // From hotbar to main inventory
                if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 27, false)) {
                  return ItemStack.EMPTY;
                }
              }
            }
          }
        }
        // Non-fuel items
        else {
          // Try input slots first
          if (!this.moveItemStackTo(slotStack, INPUT_SLOTS_START, INPUT_SLOTS_START + INPUT_SLOTS_COUNT,
              false)) {
            // If input slots are full, try moving between hotbar and main inventory
            if (index < SLOT_COUNT + 27) {
              // From main inventory to hotbar
              if (!this.moveItemStackTo(slotStack, SLOT_COUNT + 27, SLOT_COUNT + 36, false)) {
                return ItemStack.EMPTY;
              }
            } else {
              // From hotbar to main inventory
              if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 27, false)) {
                return ItemStack.EMPTY;
              }
            }
          }
        }
      }

      if (slotStack.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }

      if (slotStack.getCount() == itemstack.getCount()) {
        return ItemStack.EMPTY;
      }

      slot.onTake(player, slotStack);
    }

    return itemstack;
  }

  @Override
  public boolean stillValid(@NotNull Player player) {
    return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
        player, ModBlocks.MECHANICAL_FURNACE_BLOCK.get());
  }

  private void addPlayerInventory(Inventory inventory) {
    int tW = SLOT_SIZE * 9;
    int rW = 190 - tW - CONTAINER_PADDING * 2;
    int s = rW / 2;
    
    // Create player inventory slots
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 9; col++) {
        int x = CONTAINER_PADDING + s + col * (SLOT_SIZE);
        int y = 86 + 12 + row * 18;
        this.addSlot(new Slot(inventory, col + row * 9 + 9, 1 + x, 1 + y));
      }
    }
    
  }

  private void addPlayerHotbar(Inventory inventory) {
    int tW = SLOT_SIZE * 9;
    int rW = 190 - tW - CONTAINER_PADDING * 2;
    int s = rW / 2;
    int tH = SLOT_SIZE * 3;
    for (int col = 0; col < 9; col++) {
      int x = CONTAINER_PADDING + s + col * (SLOT_SIZE);
      int y = 86 + 12 + tH + SLOT_SPACING * 2;
      this.addSlot(new Slot(inventory, col, 1 + x, 1 + y));
    }
  }

  @Override
  public boolean clickMenuButton(@NotNull Player player, int id) {
    if (id == AddonManager.ADDON_TAB_TOGGLE_BUTTON_ID) {
      return this.blockEntity.getAddonManager().toggleAddonSlots();
    }

    // Handle side/automation buttons
    if (blockEntity instanceof ISidedMachine sidedMachine) {
      PackedButtonData packedButtonData = PackedButtonData.fromId(id);
      Constants.Sides side = packedButtonData.side();
      sidedMachine.getSideManager().handleSideBtnClick(side, packedButtonData.shifted(),
          packedButtonData.clickAction());
      PacketDistributor.sendToPlayer((ServerPlayer) player,
          new SideStatePayload(side, sidedMachine.getSideManager().getSideState(side),
              this.blockEntity.getBlockPos()));
    }
    return true;
  }

  public MechanicalFurnaceBlockEntity getBlockEntity() {
    return this.blockEntity;
  }
}
