package net.roboxgamer.modernutils.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.custom.MechanicalCrafterBlock;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.network.SlotStatePayload;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class SideManager {
  BlockEntity blockEntity;
  public SideManager(BlockEntity be) {
    this.blockEntity = be;
  }
  
  private Boolean autoImportEnabled = true;
  private Boolean autoExportEnabled = false;
  
  private final Map<Direction, Boolean> importDirections = new HashMap<>(
      Map.of(
          Direction.NORTH, true,
          Direction.SOUTH, true,
          Direction.EAST, true,
          Direction.WEST, true,
          Direction.UP, true,
          Direction.DOWN, true
      )
  );
  
  private final Map<Direction, Boolean> exportDirections = new HashMap<>(
      Map.of(
          Direction.NORTH, false,
          Direction.SOUTH, false,
          Direction.EAST, false,
          Direction.WEST, false,
          Direction.UP, false,
          Direction.DOWN, false
      )
  );
  
  private final Map<Constants.Sides, Constants.SideState> sideBtnStates = new HashMap<>(
      Map.of(
          Constants.Sides.UP, Constants.SideState.INPUT,
          Constants.Sides.DOWN, Constants.SideState.INPUT,
          Constants.Sides.LEFT, Constants.SideState.INPUT,
          Constants.Sides.RIGHT, Constants.SideState.INPUT,
          Constants.Sides.BACK, Constants.SideState.INPUT,
          Constants.Sides.FRONT, Constants.SideState.INPUT
      )
  );
  
  public Direction getRelativeDirection(Constants.Sides side) {
    Direction facingDir = blockEntity.getBlockState().getValue(MechanicalCrafterBlock.FACING);
    return switch (side) {
      case UP -> Direction.UP;
      case DOWN -> Direction.DOWN;
      case LEFT -> facingDir.getClockWise();
      case RIGHT -> facingDir.getCounterClockWise();
      case BACK -> facingDir.getOpposite();
      case FRONT -> facingDir;
    };
  }
  
  public void handleSideBtnClick(@NotNull Constants.Sides side, boolean isShiftPressed, ClickAction clickAction) {
    Direction sideDir = getRelativeDirection(side);
    Constants.SideState sideState = getSideState(side);
    int currentState = sideState.ordinal();
    int totalStates = Constants.SideState.values().length;
    
    if (isShiftPressed) {
      // Handle shift + left-click logic here
      sideState = Constants.SideState.NONE;
      this.sideBtnStates.put(side, sideState);
    } else {
      int nextState;
      if (clickAction != null && clickAction.equals(ClickAction.SECONDARY)) {
        // For reverse cycling, handle negative numbers
        nextState = (currentState - 1 + totalStates) % totalStates;
      } else {
        // Forward cycling
        nextState = (currentState + 1) % totalStates;
      }
      this.sideBtnStates.put(side, Constants.SideState.values()[nextState]);
    }
    
    
    // Handle sideState
    sideState = getSideState(side);
    switch (sideState){
      case INPUT -> {
        this.importDirections.put(sideDir, true);
        this.exportDirections.put(sideDir, false);
      }
      case OUTPUT -> {
        this.importDirections.put(sideDir, false);
        this.exportDirections.put(sideDir, true);
      }
      case BOTH -> {
        this.importDirections.put(sideDir,true);
        this.exportDirections.put(sideDir,true);
      }
      case NONE -> {
        this.importDirections.put(sideDir,false);
        this.exportDirections.put(sideDir,false);
      }
    }
    
    ModernUtilsMod.LOGGER.debug("Side {} State Changed to {}", side, getSideState(side));
    blockEntity.setChanged();
  }
  
  public Constants.SideState getSideState(Constants.Sides side){
    return this.sideBtnStates.get(side);
  }
  
  public void setAutoImportEnabled(boolean b) {
    this.autoImportEnabled = b;
  }
  
  public boolean isAutoImportEnabled() {
    return this.autoImportEnabled;
  }
  
  public void setAutoExportEnabled(boolean b) {
    this.autoExportEnabled = b;
  }
  
  public boolean isAutoExportEnabled() {
    return this.autoExportEnabled;
  }
  
  public void setSideBtnState(Constants.@NotNull Sides side, Constants.SideState sideState) {
    this.sideBtnStates.put(side, sideState);
  }
  
  
  public void autoImportBtnHandler() {
    //  Logic to enable and disable auto import
    ModernUtilsMod.LOGGER.debug("Auto Import Button Pressed");
    this.setAutoImportEnabled(!this.isAutoImportEnabled());
    PacketDistributor.sendToServer(
        new SlotStatePayload(-2, this.isAutoImportEnabled(), blockEntity.getBlockPos())
    );
  }
  
  public void autoExportBtnHandler() {
    ModernUtilsMod.LOGGER.debug("Auto Export Button Pressed");
    this.setAutoExportEnabled(!this.isAutoExportEnabled());
    PacketDistributor.sendToServer(
        new SlotStatePayload(-1,this.isAutoExportEnabled(),blockEntity.getBlockPos())
    );
  }
  
  public void autoImport(List<IngredientNeed> neededItems, ItemStackHandler inputHandler) {
    getValidDirectionsStream(Direction.values(), this.importDirections).forEach(direction -> importFromAdjacentInventories(neededItems, direction,inputHandler));
  }
  
  private @NotNull Stream<Direction> getValidDirectionsStream(Direction[] directions, Map<Direction, Boolean> map) {
    //  Filter out directions that are false in the import directions map
    return Arrays.stream(directions).filter(map::get);
  }
  
  public boolean isImportDirection(Direction side) {
    return importDirections.getOrDefault(side, false);
  }
  
  public boolean isExportDirection(Direction side){
    return exportDirections.getOrDefault(side,false);
  }
  
  public record IngredientNeed(Ingredient ingredient, int slot, int count) {}
  
  private void importFromAdjacentInventories(List<IngredientNeed> neededItems, Direction direction,ItemStackHandler inputSlots) {
    if (neededItems.isEmpty()) return;
    BlockPos pos = blockEntity.getBlockPos().relative(direction);
    var level = blockEntity.getLevel();
    if (level == null || level.isClientSide() || !(level instanceof ServerLevel))
      return;
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity == null) return;
    IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
    if (cap == null) return;
    
    Iterator<IngredientNeed> iterator = neededItems.iterator();
    while (iterator.hasNext()) {
      IngredientNeed need = iterator.next();
      boolean found = false;
      
      for (int slot = 0; slot < cap.getSlots(); slot++) {
        ItemStack extractedStack = cap.extractItem(slot, need.count, true); // Simulate extraction
        if (!extractedStack.isEmpty() && need.ingredient.test(extractedStack)) {
          ItemStack actualExtracted = cap.extractItem(slot, need.count, false); // Actually extract
          if (putInInputSlots(inputSlots, actualExtracted)) {
            found = true;
            break;
          }
        }
      }
      
      if (found) {
        iterator.remove(); // Remove this need from the list
      }
    }
  }
  
  private boolean putInInputSlots(ItemStackHandler inputSlots, ItemStack stack) {
    // First, try to stack with existing items
    for (int i = 0; i < inputSlots.getSlots(); i++) {
      ItemStack existingStack = inputSlots.getStackInSlot(i);
      if (!existingStack.isEmpty() && ItemStack.isSameItemSameComponents(existingStack, stack)) {
        if (existingStack.getCount() < existingStack.getMaxStackSize()) {
          existingStack.grow(1);
          inputSlots.setStackInSlot(i, existingStack);
          return true;
        }
      }
    }
    
    // If stacking wasn't possible, find the first empty slot
    for (int i = 0; i < inputSlots.getSlots(); i++) {
      if (inputSlots.getStackInSlot(i).isEmpty()) {
        inputSlots.setStackInSlot(i, stack);
        return true;
      }
    }
    
    return false;
  }
  
  public void autoExport(CustomItemStackHandler outputHandler) {
    //  Get output slots
    if (outputHandler.isCompletelyEmpty())
      return;
    var level = blockEntity.getLevel();
    var exportValidDirections = getValidDirectionsStream(Direction.values(), this.exportDirections).toList();
    for (Direction direction : exportValidDirections) {
      BlockPos pos = blockEntity.getBlockPos().relative(direction);
      BlockState state = level.getBlockState(pos);
      if (state.hasBlockEntity() && state.getBlock() instanceof EntityBlock) {
        if (level.getBlockEntity(pos) instanceof MechanicalCrafterBlockEntity) return;
        // Get the item handler from the block entity
        IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
        if (cap == null)
          continue;
        
        // Iterate through the items to export to the block
        for (int i = 0; i < outputHandler.getSlots(); i++) {
          ItemStack itemToExport = outputHandler.getStackInSlot(i);
          if (itemToExport.isEmpty())
            continue;
          
          // Try to insert the item into the capability
          for (int j = 0; j < cap.getSlots(); j++) {
            ItemStack inserted = cap.insertItem(j, itemToExport, false);
            if (inserted.isEmpty()){
              outputHandler.setStackInSlot(i, ItemStack.EMPTY);
              break;
            }
            itemToExport.setCount(inserted.getCount()); // Update the count of the item remaining to export
          }
        }
      }
    }
  }
  
  private void saveSidesConfig(CompoundTag tag) {
    CompoundTag sidesConfig = new CompoundTag();
    sidesConfig.putInt("up", this.sideBtnStates.get(Constants.Sides.UP).ordinal());
    sidesConfig.putInt("down", this.sideBtnStates.get(Constants.Sides.DOWN).ordinal());
    sidesConfig.putInt("left", this.sideBtnStates.get(Constants.Sides.LEFT).ordinal());
    sidesConfig.putInt("right", this.sideBtnStates.get(Constants.Sides.RIGHT).ordinal());
    sidesConfig.putInt("back", this.sideBtnStates.get(Constants.Sides.BACK).ordinal());
    sidesConfig.putInt("front", this.sideBtnStates.get(Constants.Sides.FRONT).ordinal());
    tag.put("sidesConfig", sidesConfig);
  }
  
  private void loadSidesConfig(CompoundTag tag) {
    CompoundTag sidesConfig = tag.getCompound("sidesConfig");
    this.sideBtnStates.put(Constants.Sides.UP, Constants.SideState.values()[sidesConfig.getInt("up")]);
    this.sideBtnStates.put(Constants.Sides.DOWN, Constants.SideState.values()[sidesConfig.getInt("down")]);
    this.sideBtnStates.put(Constants.Sides.LEFT, Constants.SideState.values()[sidesConfig.getInt("left")]);
    this.sideBtnStates.put(Constants.Sides.RIGHT, Constants.SideState.values()[sidesConfig.getInt("right")]);
    this.sideBtnStates.put(Constants.Sides.BACK, Constants.SideState.values()[sidesConfig.getInt("back")]);
    this.sideBtnStates.put(Constants.Sides.FRONT, Constants.SideState.values()[sidesConfig.getInt("front")]);
    
    for (Constants.Sides side : Constants.Sides.values()) {
      var sideState = getSideState(side);
      Direction sideDir = getRelativeDirection(side);
      
      switch (sideState){
        case INPUT -> {
          this.importDirections.put(sideDir,true);
          this.exportDirections.put(sideDir,false);
        }
        case OUTPUT -> {
          this.importDirections.put(sideDir,false);
          this.exportDirections.put(sideDir,true);
        }
        case BOTH -> {
          this.importDirections.put(sideDir,true);
          this.exportDirections.put(sideDir,true);
        }
        case NONE -> {
          this.importDirections.put(sideDir,false);
          this.exportDirections.put(sideDir,false);
        }
      }
    }
  }
  
  public void saveToTag(CompoundTag tag){
    tag.putBoolean("autoImportEnabled", this.autoImportEnabled);
    tag.putBoolean("autoExportEnabled", this.autoExportEnabled);
    saveSidesConfig(tag);
  }
  
  public void loadFromTag(CompoundTag tag) {
    this.autoImportEnabled = tag.getBoolean("autoImportEnabled");
    this.autoExportEnabled = tag.getBoolean("autoExportEnabled");
    loadSidesConfig(tag);
  }
}
