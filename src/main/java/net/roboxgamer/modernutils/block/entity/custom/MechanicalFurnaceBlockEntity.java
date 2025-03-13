package net.roboxgamer.modernutils.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.block.custom.MechanicalFurnaceBlock;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import net.roboxgamer.modernutils.menu.MechanicalFurnaceMenu;
import net.roboxgamer.modernutils.util.*;
import net.roboxgamer.modernutils.util.Constants.IAddonSupport;
import net.roboxgamer.modernutils.util.Constants.IRedstoneConfigurable;
import net.roboxgamer.modernutils.util.Constants.ISidedMachine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MechanicalFurnaceBlockEntity extends BlockEntity
    implements MenuProvider, ISidedMachine, IRedstoneConfigurable, IAddonSupport {
  private static final int MAX_IMPORT_PER_OPERATION = 64;
  private CustomItemStackHandler inputSlots;
  private CustomItemStackHandler outputSlots;
  private CustomItemStackHandler fuelSlot;

  private CombinedInvWrapper combinedInvHandler;

  private final RedstoneManager redstoneManager = new RedstoneManager(this);
  private final SideManager sideManager = new SideManager(this);
  private final AddonManager addonManager = new AddonManager(this, 4, Constants.ALLOWED_FURNACE_ADDONS){
    public boolean isAllowedItem(net.minecraft.world.item.Item item) {
      boolean isValidAddon = super.isAllowedItem(item);
      if (!isValidAddon) return false;
      if (Items.SMOKER.equals(item)){
        return isValidAddon && !hasBlastFurnaceAddon() && !hasSmokerFurnaceAddon();
      }
      if (Items.BLAST_FURNACE.equals(item)){
        return isValidAddon && !hasSmokerFurnaceAddon() && !hasBlastFurnaceAddon();
      }
      return isValidAddon;
    };
  };

  private ContainerData data;

  private final int[] slotProgress = new int[8]; // Track progress for each slot separately
  private int maxProgress = 200; // 10 seconds (200 ticks) - base maxProgress
  private int burnTime = 0;
  private int maxBurnTime = 0;
  private int tc = 0;
  private int importCount = 0;
  private boolean lastRedstoneState = false;
  private boolean pulseProcessing = false;
  private boolean completedPulseItem = false;
  private int processingSpeed = 1; // Add processing speed field
  
  public int getMaxProgress() {
    return this.maxProgress;
  }
  
  public enum FurnaceState {
    RUNNING,
    IDLE,
    DISABLED,
    ERROR
  }

  private FurnaceState furnaceState = FurnaceState.IDLE;

  public MechanicalFurnaceBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.MECHANICAL_FURNACE_BE.get(), pos, state);
    this.inputSlots = new CustomItemStackHandler(8, this) {
      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
      }
    };
    this.outputSlots = new CustomItemStackHandler(8, this) {
      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false; // Output slots should not accept manual insertion
      }
    };
    this.fuelSlot = new CustomItemStackHandler(1, this) {
      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
      }
    };
    this.combinedInvHandler = new CombinedInvWrapper(inputSlots, outputSlots, fuelSlot) {
      @Override
      public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        int inputSlotCount = inputSlots.getSlots();
        int outputSlotCount = outputSlots.getSlots();

        if (slot < 0 || slot >= getSlots()) {
          return ItemStack.EMPTY;
        }

        if (slot < inputSlotCount) {
          return inputSlots.extractItem(slot, amount, simulate);
        } else if (slot < inputSlotCount + outputSlotCount) {
          return outputSlots.extractItem(slot - inputSlotCount, amount, simulate);
        } else {
          return fuelSlot.extractItem(0, amount, simulate);
        }
      }

      @Override
      public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        int inputSlotCount = inputSlots.getSlots();
        int outputSlotCount = outputSlots.getSlots();

        if (slot < 0 || slot >= getSlots()) {
          return stack;
        }

        if (slot < inputSlotCount) {
          return inputSlots.insertItem(slot, stack, simulate);
        } else if (slot < inputSlotCount + outputSlotCount) {
          return outputSlots.insertItem(slot - inputSlotCount, stack, simulate);
        } else {
          return fuelSlot.insertItem(0, stack, simulate);
        }
      }

      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        int inputSlotCount = inputSlots.getSlots();
        int outputSlotCount = outputSlots.getSlots();

        if (slot < 0 || slot >= getSlots()) {
          return false;
        }

        if (slot < inputSlotCount) {
          return inputSlots.isItemValid(slot, stack);
        } else if (slot < inputSlotCount + outputSlotCount) {
          return outputSlots.isItemValid(slot - inputSlotCount, stack);
        } else {
          return fuelSlot.isItemValid(0, stack);
        }
      }
    };
    this.data = new ContainerData() {
      @Override
      public int get(int index) {
        if (index < 8) {
          return slotProgress[index]; // Progress for each slot
        }
        return switch (index) {
          case 8 -> burnTime; // Current burn time
          case 9 -> maxBurnTime; // Max burn time
          default -> 0;
        };
      }

      @Override
      public void set(int index, int value) {
        if (index < 8) {
          slotProgress[index] = value;
        } else {
          switch (index) {
            case 8 -> burnTime = value;
            case 9 -> maxBurnTime = value;
          }
        }
      }

      @Override
      public int getCount() {
        return 10; // 8 progress + 2 fuel data
      }
    };
  }

  public IItemHandlerModifiable getCapabilityHandler(@Nullable Direction side) {
    if (side == null) {
      return combinedInvHandler;
    }

    if (sideManager.isImportDirection(side)) {
      return inputSlots;
    } else if (sideManager.isExportDirection(side)) {
      return outputSlots;
    }

    return null;
  }

  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    var modData = getModData(registries);
    tag.put(ModernUtilsMod.MODID, modData);
  }

  private void deserializeFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    // Load machine state
    burnTime = tag.getInt("BurnTime");
    maxBurnTime = tag.getInt("MaxBurnTime");
    importCount = tag.getInt("importCount");
    processingSpeed = tag.getInt("processingSpeed"); // Load processing speed

    // Load progress for each slot
    int[] progressArray = tag.getIntArray("SlotProgress");
    System.arraycopy(progressArray, 0, slotProgress, 0, Math.min(progressArray.length, 8));

    // Load inventories
    inputSlots.deserializeNBT(registries, tag.getCompound("input_inventory"));
    outputSlots.deserializeNBT(registries, tag.getCompound("output_inventory"));
    fuelSlot.deserializeNBT(registries, tag.getCompound("fuel_inventory"));

    // Load side configuration
    sideManager.loadFromTag(tag);
    redstoneManager.loadFromTag(tag);
    addonManager.loadFromTag(tag, registries);

    // Load last redstone state
    lastRedstoneState = tag.getBoolean("LastRedstoneState");

    pulseProcessing = tag.getBoolean("PulseProcessing");
    completedPulseItem = tag.getBoolean("CompletedPulseItem");
  }

  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);

    // Check if we are on the client side
    if (level != null && level.isClientSide()) {
      // Deserialize data from the tag for client-side
      deserializeFromTag(tag, registries);
    } else {
      CompoundTag modData = tag.getCompound(ModernUtilsMod.MODID);
      deserializeFromTag(modData, registries);
    }
    return;
  }

  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
    return getModData(registries);
  }

  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  private @NotNull CompoundTag getModData(HolderLookup.Provider registries) {
    CompoundTag modData = new CompoundTag();

    // Save existing data
    modData.putInt("BurnTime", burnTime);
    modData.putInt("MaxBurnTime", maxBurnTime);
    modData.putInt("importCount", importCount);
    modData.putInt("processingSpeed", processingSpeed); // Save processing speed

    // Save progress for each slot
    int[] progressArray = new int[8];
    System.arraycopy(slotProgress, 0, progressArray, 0, 8);
    modData.putIntArray("SlotProgress", progressArray);

    // Save inventories
    modData.put("input_inventory", inputSlots.serializeNBT(registries));
    modData.put("output_inventory", outputSlots.serializeNBT(registries));
    modData.put("fuel_inventory", fuelSlot.serializeNBT(registries));

    // Save side configuration
    sideManager.saveToTag(modData);
    redstoneManager.saveToTag(modData);
    addonManager.saveToTag(modData, registries);

    // Save last redstone state
    modData.putBoolean("LastRedstoneState", lastRedstoneState);

    modData.putBoolean("PulseProcessing", pulseProcessing);
    modData.putBoolean("CompletedPulseItem", completedPulseItem);

    return modData;
  }

  public void drops() {
    SimpleContainer inventory = new SimpleContainer(combinedInvHandler.getSlots());
    for (int i = 0; i < combinedInvHandler.getSlots(); i++) {
      inventory.setItem(i, combinedInvHandler.getStackInSlot(i));
    }
    SimpleContainer i2 = new SimpleContainer(addonManager.getAddonSlots().getSlots());
    for (int i = 0; i < addonManager.ADDON_SLOTS_COUNT; i++) {
      i2.setItem(i, addonManager.getAddonSlots().getStackInSlot(i));
    }
    Containers.dropContents(this.level, this.worldPosition, inventory);
    Containers.dropContents(this.level, this.worldPosition, i2);
  }

  private void autoImport() {
    // First try to fill the fuel slot if it's empty or low - this is independent of
    // input slot importing
    ItemStack currentFuel = fuelSlot.getStackInSlot(0);
    if (currentFuel.isEmpty() || currentFuel.getCount() < currentFuel.getMaxStackSize()) {
      int fuelSpace = currentFuel.isEmpty() ? currentFuel.getMaxStackSize()
          : currentFuel.getMaxStackSize() - currentFuel.getCount();

      if (fuelSpace > 0) {
        sideManager.autoImportWithPredicate(fuelSlot,
            stack -> stack.getBurnTime(RecipeType.SMELTING) > 0,
            fuelSpace);
      }
    }

    int totalImported = 0;

    // Pre-scan input slots to build a map of what we need
    record SlotNeed(int slot, ItemStack stack, int space) {
    }
    List<SlotNeed> slotNeeds = new ArrayList<>();

    // First pass: collect all slot needs
    for (int slot = 0; slot < inputSlots.getSlots(); slot++) {
      ItemStack currentStack = inputSlots.getStackInSlot(slot);
      if (currentStack.isEmpty()) {
        slotNeeds.add(new SlotNeed(slot, ItemStack.EMPTY, MAX_IMPORT_PER_OPERATION - totalImported));
      } else if (currentStack.getCount() < currentStack.getMaxStackSize()) {
        ItemStack recipeOutput = getRecipeResult(currentStack);
        if (!recipeOutput.isEmpty()) {
          int space = Math.min(
              currentStack.getMaxStackSize() - currentStack.getCount(),
              MAX_IMPORT_PER_OPERATION - totalImported);
          if (space > 0) {
            slotNeeds.add(new SlotNeed(slot, currentStack.copy(), space));
          }
        }
      }
    }

    // If no needs, return early
    if (slotNeeds.isEmpty())
      return;

    // Second pass: process each slot need
    for (SlotNeed need : slotNeeds) {
      // For empty slots, look for any smeltable item
      if (need.stack.isEmpty()) {
        int beforeCount = inputSlots.getStackInSlot(need.slot).getCount();
        sideManager.autoImportWithPredicate(inputSlots,
            stack -> canProcess(stack),
            Math.min(need.space, MAX_IMPORT_PER_OPERATION - totalImported));
        int afterCount = inputSlots.getStackInSlot(need.slot).getCount();
        totalImported += (afterCount - beforeCount);
      }
      // For non-empty slots, try to find matching items that are smeltable
      else {
        int beforeCount = inputSlots.getStackInSlot(need.slot).getCount();
        sideManager.autoImportWithPredicate(inputSlots,
            stack -> ItemStack.isSameItemSameComponents(stack, need.stack) &&
                canProcess(stack),
            Math.min(need.space, MAX_IMPORT_PER_OPERATION - totalImported));
        int afterCount = inputSlots.getStackInSlot(need.slot).getCount();
        totalImported += (afterCount - beforeCount);
      }
    }
  }

  public void tick(Level level, BlockPos pos, BlockState state, MechanicalFurnaceBlockEntity blockEntity) {
    if (level.isClientSide() || level == null || !(level instanceof ServerLevel))
      return;

    blockEntity.tc++;
    if (blockEntity.tc >= 200)
      blockEntity.tc = 0;

    // Get current redstone state
    boolean currentRedstoneState = level.hasNeighborSignal(pos);

    // Check redstone mode first
    boolean canRun = switch (redstoneManager.getRedstoneMode()) {
      case ALWAYS_ON -> true;
      case REDSTONE_ON -> currentRedstoneState;
      case REDSTONE_OFF -> !currentRedstoneState;
      case PULSE -> {
        if (!lastRedstoneState && currentRedstoneState) {
          // Rising edge - start new pulse processing
          pulseProcessing = true;
          completedPulseItem = false;
        }
        lastRedstoneState = currentRedstoneState;
        yield pulseProcessing;
      }
    };

    if (!canRun) {
      if (state.getValue(MechanicalFurnaceBlock.LIT)) {
        level.setBlock(pos, state.setValue(MechanicalFurnaceBlock.LIT, false), Block.UPDATE_ALL);
      }
      furnaceState = FurnaceState.DISABLED;
      return;
    }

    // Handle auto import/export every 10 ticks (0.5 seconds)
    if (blockEntity.tc % 10 == 0) {
      // Reset import count before auto import
      blockEntity.importCount = 0;

      if (sideManager.isAutoImportEnabled()) {
        autoImport();
      }
      if (sideManager.isAutoExportEnabled()) {
        sideManager.autoExport(outputSlots);
      }
    }

    // Count active recipes and check if we need fuel
    int activeRecipes = 0;
    int actuallyProcessing = 0;
    boolean needsFuel = false;

    // First pass: count active recipes and check fuel need
    for (int slot = 0; slot < inputSlots.getSlots(); slot++) {
      ItemStack inputStack = inputSlots.getStackInSlot(slot);
      if (!inputStack.isEmpty()) {
        ItemStack recipeOutput = getRecipeResult(inputStack);
        if (!recipeOutput.isEmpty()) {
          ItemStack outputStack = outputSlots.getStackInSlot(slot);
          if (outputStack.isEmpty() || (ItemStack.isSameItem(outputStack, recipeOutput) &&
              outputStack.getCount() < outputStack.getMaxStackSize())) {
            activeRecipes++;
            if (burnTime <= 0) {
              needsFuel = true;
            }
          }
        }
      }
    }

    // Update state based on fuel and recipes
    if (activeRecipes == 0) {
      furnaceState = FurnaceState.IDLE;
    } else if (needsFuel && fuelSlot.getStackInSlot(0).isEmpty()) {
      furnaceState = FurnaceState.ERROR; // No fuel available
    } else {
      furnaceState = FurnaceState.RUNNING;
    }

    // Handle fuel if needed
    if (needsFuel && activeRecipes > 0) {
      ItemStack fuelStack = fuelSlot.getStackInSlot(0);
      if (!fuelStack.isEmpty()) {
        int fuelValue = fuelStack.getBurnTime(RecipeType.SMELTING);
        if (fuelValue > 0) {
          fuelStack.shrink(1);
          burnTime = fuelValue;
          maxBurnTime = fuelValue;
          setChanged();
        }
      }
    }

    // Process all slots in parallel if we have fuel
    boolean didProcess = false;
    boolean completedThisTick = false; // New flag to track completions in current tick
    if (activeRecipes > 0 && burnTime > 0) {
      // Process all slots in parallel
      for (int slot = 0; slot < inputSlots.getSlots(); slot++) {
        ItemStack inputStack = inputSlots.getStackInSlot(slot);
        if (inputStack.isEmpty()) {
          slotProgress[slot] = 0;
          continue;
        }

        ItemStack recipeOutput = getRecipeResult(inputStack);

        if (recipeOutput.isEmpty()) {
          slotProgress[slot] = 0;
          continue;
        }

        ItemStack outputStack = outputSlots.getStackInSlot(slot);

        // Check if we can add the output
        if (!outputStack.isEmpty() && (!ItemStack.isSameItem(outputStack, recipeOutput) ||
            outputStack.getCount() >= outputStack.getMaxStackSize())) {
          slotProgress[slot] = 0;
          continue;
        }

        // For PULSE mode, process all valid recipes until one completes
        if (redstoneManager.getRedstoneMode() == RedstoneManager.RedstoneMode.PULSE) {
          if (!completedPulseItem || completedThisTick) { // Continue processing this tick if something completed
            slotProgress[slot] += processingSpeed;  // Increment by processing speed instead of 1
            actuallyProcessing++;
            didProcess = true;
          }
        } else {
          // Normal incremental processing for other modes
          slotProgress[slot] += processingSpeed;  // Increment by processing speed instead of 1
          actuallyProcessing++;
          didProcess = true;
        }

        // Complete the recipe if done
        if (slotProgress[slot] >= maxProgress) {
          inputStack.shrink(1);
          if (outputStack.isEmpty()) {
            outputSlots.setStackInSlot(slot, recipeOutput.copy());
          } else {
            outputStack.grow(1);
          }
          slotProgress[slot] = 0;

          if (redstoneManager.getRedstoneMode() == RedstoneManager.RedstoneMode.PULSE) {
            completedPulseItem = true;
            completedThisTick = true;
          }
        }
      }

      // Only stop pulse processing after the tick is complete
      if (completedThisTick) {
        pulseProcessing = false;
      }

      // Consume fuel based on how many items we're actually processing
      if (didProcess && actuallyProcessing > 0) {
        int fuelConsumption = actuallyProcessing;
        if (isFasterProcessing()) {
          fuelConsumption *= 2;  // Double fuel consumption for blast furnace/smoker
        }
        burnTime = Math.max(0, burnTime - fuelConsumption);
      }
      setChanged();
    } else if (activeRecipes == 0) {
      // Reset progress if no active recipes
      for (int slot = 0; slot < inputSlots.getSlots(); slot++) {
        slotProgress[slot] = 0;
      }
    }

    // Update block state
    boolean isRunning = burnTime >= 0 && didProcess;
    if (state.getValue(MechanicalFurnaceBlock.LIT) != isRunning) {
      level.setBlock(pos, state.setValue(MechanicalFurnaceBlock.LIT, isRunning), 3);
    }
  }

  public boolean hasFuel() {
    return burnTime > 0;
  }

  public boolean hasFuelStack() {
    return !fuelSlot.getStackInSlot(0).isEmpty();
  }

  public String getFuelBurnTimeString() {
    if (hasFuel()) {
      return (burnTime / 20) + "s";
    }
    ItemStack fuelStack = fuelSlot.getStackInSlot(0);
    if (!fuelStack.isEmpty()) {
      int potentialBurnTime = fuelStack.getBurnTime(RecipeType.SMELTING);
      return potentialBurnTime > 0 ? (potentialBurnTime / 20) + "s" : "N/A";
    }
    return "N/A";
  }

  @Override
  public @NotNull Component getDisplayName() {
    return ModBlocks.MECHANICAL_FURNACE_BLOCK.get().getName();
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
    return new MechanicalFurnaceMenu(id, inventory, this, this.data);
  }

  public int getSlotProgress(int slot) {
    return slot >= 0 && slot < 8 ? slotProgress[slot] : 0;
  }

  public RedstoneManager.RedstoneMode getRedstoneMode() {
    return redstoneManager.getRedstoneMode();
  }

  public FurnaceState getFurnaceState() {
    return furnaceState;
  }

  public CustomItemStackHandler getInputHandler() {
    return inputSlots;
  }

  public CustomItemStackHandler getOutputHandler() {
    return outputSlots;
  }

  public CustomItemStackHandler getFuelHandler() {
    return fuelSlot;
  }

  public RedstoneManager getRedstoneManager() {
    return redstoneManager;
  }

  public SideManager getSideManager() {
    return sideManager;
  }

  @Override
  public AddonManager getAddonManager() {
    return addonManager;
  }

  @Override
  public void setChanged() {
    updateProcessingSpeed();
    updateMaxProgress();
    super.setChanged();
  }

  private void updateProcessingSpeed() {
    // Reset to default speed
    this.processingSpeed = 1;

    // Get the addon slots handler
    CustomItemStackHandler addonSlots = this.addonManager.getAddonSlots();

    // Calculate combined speed multiplier from all addon slots
    int combinedSpeedMultiplier = 0;

    // Check all addon slots
    for (int i = 0; i < addonSlots.getSlots(); i++) {
      ItemStack addon = addonSlots.getStackInSlot(i);
      if (!addon.isEmpty() && Constants.FURNACE_SPEED_UPGRADES.containsKey(addon.getItem())) {
        // Get the speed multiplier for this item and add it to the combined multiplier
        int multiplier = Constants.FURNACE_SPEED_UPGRADES.get(addon.getItem());
        combinedSpeedMultiplier += multiplier;
      }
    }

    // Set the final processing speed
    if (combinedSpeedMultiplier > 0) {
      this.processingSpeed = combinedSpeedMultiplier;
    }
  }

  private Optional<RecipeHolder<SmeltingRecipe>> getSmeltingRecipe(ItemStack input) {
    if (level == null || input.isEmpty())
      return Optional.empty();
    return level.getRecipeManager()
        .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
  }

  private Optional<RecipeHolder<BlastingRecipe>> getBlastingRecipe(ItemStack input) {
    if (level == null || input.isEmpty())
      return Optional.empty();
    return level.getRecipeManager()
        .getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(input), level);
  }

  private Optional<RecipeHolder<SmokingRecipe>> getSmokingRecipe(ItemStack input) {
    if (level == null || input.isEmpty())
      return Optional.empty();
    return level.getRecipeManager()
        .getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(input), level);
  }

  // Instead of trying to combine them, keep them separate
  private boolean canProcess(ItemStack input) {
    if (hasSmokerFurnaceAddon()){
      return getSmokingRecipe(input).isPresent();
    }
    if (hasBlastFurnaceAddon()) {
      return getBlastingRecipe(input).isPresent();
    }
    return getSmeltingRecipe(input).isPresent();
  }

  private ItemStack getRecipeResult(ItemStack input) {
    if (hasSmokerFurnaceAddon()){
      return getSmokingRecipe(input)
          .map(recipe -> recipe.value().getResultItem(level.registryAccess()))
          .orElse(ItemStack.EMPTY);
    }
    if (hasBlastFurnaceAddon()) {
      return getBlastingRecipe(input)
          .map(recipe -> recipe.value().getResultItem(level.registryAccess()))
          .orElse(ItemStack.EMPTY);
    }
    return getSmeltingRecipe(input)
        .map(recipe -> recipe.value().getResultItem(level.registryAccess()))
        .orElse(ItemStack.EMPTY);
  }

  private boolean hasBlastFurnaceAddon() {
    CustomItemStackHandler addonSlots = this.addonManager.getAddonSlots();
    for (int i = 0; i < addonSlots.getSlots(); i++) {
      if (addonSlots.getStackInSlot(i).is(Items.BLAST_FURNACE)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasSmokerFurnaceAddon(){
    CustomItemStackHandler addonSlots = this.addonManager.getAddonSlots();
    for (int i = 0; i < addonSlots.getSlots(); i++) {
      if (addonSlots.getStackInSlot(i).is(Items.SMOKER)) {
        return true;
      }
    }
    return false;
  }

  private boolean isFasterProcessing(){
    return hasBlastFurnaceAddon() || hasSmokerFurnaceAddon();
  }

  private void updateMaxProgress() {
    this.maxProgress = isFasterProcessing() ? 200 / 2 : 200;
  }
}
