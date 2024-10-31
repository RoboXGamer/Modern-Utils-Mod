package net.roboxgamer.modernutils.block.entity.custom;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.custom.MechanicalCrafterBlock;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import net.roboxgamer.modernutils.menu.MechanicalCrafterMenu;
import net.roboxgamer.modernutils.network.ItemStackPayload;
import net.roboxgamer.modernutils.network.SideStatePayload;
import net.roboxgamer.modernutils.network.SlotStatePayload;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.CustomRecipeExtender;
import net.roboxgamer.modernutils.util.RedstoneManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.roboxgamer.modernutils.util.Constants.MECHANICAL_CRAFTER_BLACKLISTED_RECIPES;
import static net.roboxgamer.modernutils.util.Constants.MECHANICAL_CRAFTER_SPECIAL_RECIPES;
import static net.roboxgamer.modernutils.util.RedstoneManager.REDSTONE_MODE_MAP;

public class MechanicalCrafterBlockEntity extends BlockEntity implements MenuProvider {
  public Component TITLE = Component.translatable("block.modernutils.mechanical_crafter_block");
  
  public static final int INPUT_SLOTS_COUNT = 9;
  public static final int OUTPUT_SLOTS_COUNT = 9;
  public static final int CRAFT_RESULT_SLOT = 0;
  public static final int[] CRAFT_RECIPE_SLOTS = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
  private static final int RESULT_SLOT = 0;
  
  private int tc = 0;
  private CustomRecipeExtender<?> recipe;
  private ItemStack result;
  private int remainItemToggleValue = 1;
  private List<ItemStack> craftingInputList;
  
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
    Direction facingDir = getBlockState().getValue(MechanicalCrafterBlock.FACING);
    Direction sideDir = switch (side) {
      case UP -> Direction.UP;
      case DOWN -> Direction.DOWN;
      case LEFT -> facingDir.getClockWise();
      case RIGHT -> facingDir.getCounterClockWise();
      case BACK -> facingDir.getOpposite();
      case FRONT -> facingDir;
      default -> Direction.NORTH;
    };
    return sideDir;
  }
  
  public void handleSideBtnClick(@NotNull Constants.Sides side, Button button) {
    Direction sideDir = getRelativeDirection(side);
    
  //  Cycle through the side states
    Constants.SideState sideState = getSideState(side);
    int currentState = sideState.ordinal();
    int nextState = (currentState + 1) % Constants.SideState.values().length;
    this.sideBtnStates.put(side, Constants.SideState.values()[nextState]);
    
    
    // Handle sideState
    sideState = getSideState(side);
    switch (sideState){
      case INPUT -> this.importDirections.put(sideDir,true);
      case OUTPUT -> this.exportDirections.put(sideDir,true);
      case BOTH -> {
        this.importDirections.put(sideDir,true);
        this.exportDirections.put(sideDir,true);
      }
      case NONE -> {
        this.importDirections.put(sideDir,false);
        this.exportDirections.put(sideDir,false);
      }
    }
    if (button != null) {
      button.setMessage(Component.literal(String.format("%s Side,State: %s",side,sideState)));
    }
    
    ModernUtilsMod.LOGGER.debug("Side {} State Changed to {}", side, getSideState(side));
    setChanged();
    if (level != null && level.isClientSide()) {
      PacketDistributor.sendToServer(
          new SideStatePayload(side, sideState, this.getBlockPos())
      );
    }
  }
  
  public Constants.SideState getSideState(Constants.Sides side){
    return this.sideBtnStates.get(side);
  }
  
  private final RedstoneManager redstoneManager = new RedstoneManager(this);
  private ContainerData containerData;
  
  public ContainerData getContainerData() {
    return this.containerData;
  }
  
  public void setSlotState(int slotIndex, int v) {
    this.containerData.set(slotIndex, v);
    this.setChanged();
  }
  
  public void autoImportBtnHandler() {
  //  Logic to enable and disable auto import
    ModernUtilsMod.LOGGER.debug("Auto Import Button Pressed");
    this.autoImportEnabled = !this.autoImportEnabled;
    PacketDistributor.sendToServer(
        new SlotStatePayload(-2,this.autoImportEnabled,this.getBlockPos())
    );
  }
  
  public void autoExportBtnHandler() {
    ModernUtilsMod.LOGGER.debug("Auto Export Button Pressed");
    this.autoExportEnabled = !this.autoExportEnabled;
    PacketDistributor.sendToServer(
        new SlotStatePayload(-1,this.autoExportEnabled,this.getBlockPos())
    );
  }
  
  public void setAutoExport(boolean state) {
    this.autoExportEnabled = state;
  }
  
  public void setAutoImport(boolean state) {
    this.autoImportEnabled = state;
  }
  
  public boolean isAutoImportEnabled() {
    return this.autoImportEnabled;
  }
  
  public boolean isAutoExportEnabled() {
    return this.autoExportEnabled;
  }
  
  public class CustomItemStackHandler extends ItemStackHandler {
    public CustomItemStackHandler(int size) {
      super(size);
    }

    @Override
    protected void onContentsChanged(int slot) {
      MechanicalCrafterBlockEntity.this.setChanged();
    }

    public NonNullList<ItemStack> getStacks() {
      return this.stacks;
    }

    public NonNullList<ItemStack> getStacksCopy(int startIndex) {
      var t = NonNullList.withSize(this.stacks.size(), ItemStack.EMPTY);
      for (int i = startIndex; i < this.stacks.size(); i++) {
        t.set(i - startIndex, this.stacks.get(i).copy());
      }
      return t;
    }

    public NonNullList<ItemStack> getStacksCopy() {
      return this.getStacksCopy(0);
    }

    public boolean isCompletelyEmpty() {
      // if all the slots are empty, return true
      boolean isEmpty = true;
      for (ItemStack stack : this.stacks) {
        if (!stack.isEmpty()) {
          isEmpty = false;
          break;
        }
      }
      return isEmpty;
    }

    public boolean isFull() {
      boolean isFull = true;
      for (ItemStack stack : this.stacks) {
        if (stack.getCount() < stack.getMaxStackSize()) {
          isFull = false;
          break;
        }
      }
      return isFull;
    }
    
    public boolean allDisabled() {
      boolean allDisabled = true;
      for (int i = 0; i < this.stacks.size(); i++) {
        if (!isSlotDisabled(i)) {
          allDisabled = false;
          break;
        }
      }
      return allDisabled;
    }
  }

  public class CraftingSlotHandler extends CustomItemStackHandler {
    public CraftingSlotHandler(int size) {
      super(size);
    }

    @Override
    protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      if (slot == RESULT_SLOT)
        return;
      Level level = MechanicalCrafterBlockEntity.this.getLevel();
      if (level == null || level.isClientSide() || !(level instanceof ServerLevel slevel))
        return;
      BlockEntity blockEntity = slevel.getBlockEntity(MechanicalCrafterBlockEntity.this.getBlockPos());
      if (!(blockEntity instanceof MechanicalCrafterBlockEntity be))
        return;
      be.recipe = be.getRecipe(slevel);
      if (be.recipe == null) {
        be.result = null;
        PacketDistributor.sendToAllPlayers(new ItemStackPayload(ItemStack.EMPTY, be.getBlockPos()));
        be.craftingSlots.setStackInSlot(MechanicalCrafterBlockEntity.RESULT_SLOT, ItemStack.EMPTY);
      }
      if (be.result != null) {
        PacketDistributor.sendToAllPlayers(new ItemStackPayload(be.result, be.getBlockPos()));
        be.craftingSlots.setStackInSlot(MechanicalCrafterBlockEntity.RESULT_SLOT, be.result);
      }
    }
    
    NonNullList<Ingredient> getIngredientsList() {
      return NonNullList.copyOf(
          this.getStacksCopy(1).stream()
              .map(itemStack -> itemStack.copyWithCount(1))
              .map(Ingredient::of)
              .toList());
    }
  }

  CustomItemStackHandler inputSlots = new CustomItemStackHandler(9);
  CustomItemStackHandler outputSlots = new CustomItemStackHandler(9);
  CraftingSlotHandler craftingSlots = new CraftingSlotHandler(10);

  // Combine handler of input and output slots
  CombinedInvWrapper combinedInvHandler = new CombinedInvWrapper(inputSlots, outputSlots) {

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
      // Get the number of input slots
      int inputSlotCount = inputSlots.getSlots();

      // Ensure the slot is within the total valid slot range
      if (slot < 0 || slot >= getSlots()) {
        return ItemStack.EMPTY; // Slot out of bounds, return empty
      }

      // Check if the slot is within the outputSlots range
      if (slot >= inputSlotCount) {
        // Calculate the corresponding slot in the outputSlots handler
        int outputSlot = slot - inputSlotCount;

        // Extract the item from the outputSlots handler
        return outputSlots.extractItem(outputSlot, amount, simulate);
      } else {
        // If the slot is in the inputSlots, prevent extraction and return an empty
        // ItemStack
        return ItemStack.EMPTY;
      }
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      // Get the number of input slots
      int inputSlotCount = inputSlots.getSlots();

      // Ensure the slot is within the total valid slot range
      if (slot < 0 || slot >= getSlots()) {
        return stack; // Slot out of bounds, return stack unchanged
      }

      // Check if the slot is within the inputSlots range
      if (slot < inputSlotCount) {
        // Insert the item into the inputSlots handler
        return inputSlots.insertItem(slot, stack, simulate);
      } else {
        // If the slot is in the outputSlots, prevent insertion and return the stack
        // unchanged
        return stack;
      }
    }

    @Override
    public int getSlots() {
      // The total number of slots is the sum of input and output slots
      return inputSlots.getSlots() + outputSlots.getSlots();
    }

    @Override
    public int getSlotLimit(int slot) {
      // Get the number of input slots
      int inputSlotCount = inputSlots.getSlots();

      // Ensure the slot is within the total valid slot range
      if (slot < 0 || slot >= getSlots()) {
        return 0; // Invalid slot, return limit 0
      }

      // Check if the slot is within the input or output slots and return their
      // respective limits
      if (slot < inputSlotCount) {
        return inputSlots.getSlotLimit(slot); // Input slot
      } else {
        int outputSlot = slot - inputSlotCount;
        return outputSlots.getSlotLimit(outputSlot); // Output slot
      }
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      // Get the number of input slots
      int inputSlotCount = inputSlots.getSlots();

      // Ensure the slot is within the total valid slot range
      if (slot < 0 || slot >= getSlots()) {
        return false; // Invalid slot, no item is valid
      }

      // Check if the slot is within the input slots and allow only valid items for
      // input slots
      if (slot < inputSlotCount) {
        return inputSlots.isItemValid(slot, stack); // Input slot
      } else {
        return false; // No item is valid for output slots
      }
    }
  };

  public MechanicalCrafterBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.MECHANICAL_CRAFTER_BE.get(), pos, blockState);
    this.containerData  = new ContainerData() {
      private final int[] slotStates = new int[9];
      
      @Override
      public int get(int index) {
        return this.slotStates[index];
      }
      
      @Override
      public void set(int index, int value) {
        this.slotStates[index] = value;
      }
      
      @Override
      public int getCount() {
        return 9;
      }
    };
  }

  @Override
  public @NotNull Component getDisplayName() {
    return TITLE;
  }

  public void tick() {
    // Ticking logic
    this.tc++;
    if (everySecond(60))
      this.tc = 0; // Every 1 minute
    // ModernUtils.LOGGER.debug("tc: {}", this.tc);
    if (this.level == null || this.level.isClientSide() || !(this.level instanceof ServerLevel slevel))
      return;

    if (this.tc == 20) {
      this.recipe = getRecipe(slevel);
      if (this.recipe == null) {
        this.result = null;
      }
      if (this.result != null) {
        PacketDistributor.sendToAllPlayers(new ItemStackPayload(this.result, this.getBlockPos()));
        this.craftingSlots.setStackInSlot(RESULT_SLOT, this.result);
      }
    }
    
    // Redstone control logic
    boolean powered = level.hasNeighborSignal(this.getBlockPos());
    
    switch (this.redstoneManager.getRedstoneMode()) {
      case ALWAYS_ON:
        break; // No additional check, always allows crafting
      
      case REDSTONE_ON:
        if (!powered) return; // Only craft if receiving redstone power
        break;
      
      case REDSTONE_OFF:
        if (powered) return; // Stop crafting if receiving redstone power
        break;
    }
    // *** Logic for crafting ***

    if (everySecond(0.5)) { // Every 10 ticks
      if (canCraft()) {
        // ModernUtils.LOGGER.info("Can Craft!");
        craft();
      } else {
        if (autoImportEnabled) {
          autoImport();
          if (canCraft()){
            craft();
          }
        }
      }
    }
    if (autoExportEnabled) {
      autoExport(slevel);
    }
  }
  
  private void autoImport() {
    if (this.recipe == null) return;
    
    List<IngredientNeed> neededItems = calculateNeededItems();
    if (neededItems.isEmpty()) return;
    
    getValidDirectionsStream(Direction.values(), this.importDirections).forEach(direction -> importFromAdjacentInventories(neededItems, direction));
  }
  
  private @NotNull Stream<Direction> getValidDirectionsStream(Direction[] directions, Map<Direction, Boolean> map) {
    //  Filter out directions that are false in the import directions map
    return Arrays.stream(directions).filter(map::get);
  }
  
  private record IngredientNeed(Ingredient ingredient, int slot, int count) {
  }
  
  private List<IngredientNeed> calculateNeededItems() {
    List<IngredientNeed> neededItems = new ArrayList<>();
    List<Ingredient> recipeIngredients = this.recipe.getIngredients();
    NonNullList<ItemStack> inputStacks = this.inputSlots.getStacksCopy();
    for (int i = 0; i < recipeIngredients.size(); i++) {
      Ingredient ingredient = recipeIngredients.get(i);
      if (ingredient == Ingredient.EMPTY) continue; // Skip empty ingredients
      
      ItemStack[] matchingStacks = ingredient.getItems();
      if (matchingStacks.length == 0) continue; // Skip if no matching items
      ItemStack requiredStack = matchingStacks[0].copy(); // Use the first matching item as a representative
      int requiredCount = requiredStack.getCount();
      int foundCount = 0;
      
      // Check all input slots for this ingredient
      for (ItemStack slotStack : inputStacks) {
        if (!slotStack.isEmpty() && ingredient.test(slotStack) && slotStack.getCount() >= requiredCount) {
          foundCount += requiredCount;
          slotStack.shrink(requiredCount);
          break;
        }
      }
      
      if (foundCount < requiredCount) {
        neededItems.add(new IngredientNeed(ingredient, i, requiredCount - foundCount));
      }
    }
    
    return neededItems;
  }
  
  private void importFromAdjacentInventories(List<IngredientNeed> neededItems, Direction direction) {
      if (neededItems.isEmpty()) return;
      BlockPos pos = this.getBlockPos().relative(direction);
      if (this.level == null || this.level.isClientSide() || !(this.level instanceof ServerLevel))
        return;
      BlockEntity blockEntity = this.level.getBlockEntity(pos);
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
            if (putInInputSlots(this.inputSlots, actualExtracted)) {
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
  
  private void autoExport(ServerLevel level) {
  //  Get output slots
    CustomItemStackHandler outputSlots = this.outputSlots;
    if (outputSlots.isCompletelyEmpty())
      return;
    var exportValidDirections = getValidDirectionsStream(Direction.values(), this.exportDirections).toList();
    for (Direction direction : exportValidDirections) {
      BlockPos pos = this.getBlockPos().relative(direction);
      BlockState state = level.getBlockState(pos);
      if (state.hasBlockEntity() && state.getBlock() instanceof EntityBlock) {
        if (level.getBlockEntity(pos) instanceof MechanicalCrafterBlockEntity) return;
        // Get the item handler from the block entity
        IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
        if (cap == null)
          continue;
        
        // Iterate through the items to export to the block
        for (int i = 0; i < outputSlots.getSlots(); i++) {
          ItemStack itemToExport = outputSlots.getStackInSlot(i);
          if (itemToExport.isEmpty())
            continue;
          
          // Try to insert the item into the capability
          for (int j = 0; j < cap.getSlots(); j++) {
            ItemStack inserted = cap.insertItem(j, itemToExport, false);
            if (inserted.isEmpty()){
              outputSlots.setStackInSlot(i, ItemStack.EMPTY);
              break;
            }
            itemToExport.setCount(inserted.getCount()); // Update the count of the item remaining to export
          }
        }
      }
    }
  }

  private CustomRecipeExtender<?> getRecipe(ServerLevel level) {
    if (this.craftingSlots.isCompletelyEmpty()) return null;
    RecipeManager recipes = level.getRecipeManager();
    var craftingSlotsStacksCopy = this.craftingSlots.getStacksCopy(1);
    CraftingInput input = CraftingInput.of(3, 3, craftingSlotsStacksCopy);
    this.craftingInputList = input.items();
    List<RecipeHolder<CraftingRecipe>> list = recipes.getRecipesFor(RecipeType.CRAFTING, input, level);
    if (list.isEmpty()) {
      this.result = null;
      this.craftingSlots.setStackInSlot(0, ItemStack.EMPTY);
      return null;
    }
    RecipeHolder<CraftingRecipe> foundRecipe = list.getFirst(); // TODO: Add support for multiple recipes conflicting with the same input
    // ModernUtils.LOGGER.debug("foundRecipe: {}", foundRecipe);
    
    ItemStack result = foundRecipe.value().assemble(input, level.registryAccess()).copy();
    if (result.isEmpty()) return null;
    this.result = result;
    // ModernUtils.LOGGER.debug("Result: {}", this.result);
    
    PacketDistributor.sendToAllPlayers(new ItemStackPayload(this.result, this.getBlockPos()));
    if (this.craftingSlots.getStackInSlot(RESULT_SLOT) != result)
      this.craftingSlots.setStackInSlot(RESULT_SLOT, this.result);
    
    
    
    CustomRecipeExtender<?> recipeToReturn = new CustomRecipeExtender<>(foundRecipe.value());
    
    // Blacklisted types of recipes
    if (isRecipe(foundRecipe.value(), Constants.RecipeTypes.BLACKLISTED)) {
      return null;
    }
    // Special types of recipes
    if (isRecipe(foundRecipe.value(), Constants.RecipeTypes.SPECIAL)){
      NonNullList<Ingredient> ingredients = this.craftingSlots.getIngredientsList();
      recipeToReturn.setIngredients(ingredients);
    }
    
    return recipeToReturn;
  }
  
  private boolean isRecipe(CraftingRecipe value, Constants.RecipeTypes type) {
    Class<?>[] recipes = switch (type) {
      case BLACKLISTED -> MECHANICAL_CRAFTER_BLACKLISTED_RECIPES;
      case SPECIAL -> MECHANICAL_CRAFTER_SPECIAL_RECIPES;
    };
    
    for (var entry : recipes) {
      if (entry.isInstance(value)) return true;
    }
    return false;
  }
  
  private CraftingInput getCraftingInputFromActualInput(List<ItemStack> items) {
    // Get a copy of the input slots' stacks
    var inputSlotStacks = this.inputSlots.getStacksCopy();

    // Make a copy of the items list passed as a parameter
    ArrayList<ItemStack> itemsToMatch = items.stream().map(ItemStack::copy)
        .collect(Collectors.toCollection(ArrayList::new));

    // Prepare a list for the crafting input with 9 slots (3x3)
    var matchedItems = NonNullList.withSize(9, ItemStack.EMPTY);

    // Iterate through the input slots and attempt to match the items
    for (int slotIndex = 0; slotIndex < inputSlotStacks.size(); slotIndex++) {
      ItemStack inputSlotItem = inputSlotStacks.get(slotIndex);

      // Iterate through the items to match
      for (int matchIndex = 0; matchIndex < itemsToMatch.size(); matchIndex++) {
        ItemStack matchingItem = itemsToMatch.get(matchIndex);

        // If items match by type, copy the count from the matching item
        if (ItemStack.isSameItem(matchingItem, inputSlotItem)) {
          matchedItems.set(slotIndex, inputSlotItem.copyWithCount(matchingItem.getCount()));

          // Remove the matched item from the list to avoid further matches
          itemsToMatch.remove(matchIndex);
          break; // Break the inner loop and move to the next input slot
        }
      }
    }

    // Construct the CraftingInput with the matched items
    return CraftingInput.of(3, 3, matchedItems);
  }

  private boolean inputCheck(List<ItemStack> input, List<Ingredient> ingredients) {
    // Iterate over each ingredient in the recipe
    for (Ingredient ingredient : ingredients) {
      boolean matched = false;
      // ModernUtils.LOGGER.info("Ingredient: {}", ingredient);
      if (ingredient.getItems().length == 0)
        continue;

      // Iterate over each item in the input slots
      for (ItemStack inputItem : input) {
        if (inputItem.isEmpty())
          continue;
        if (ingredient.test(inputItem)) {
          // Find the specific ingredient option that matches the input item
          for (ItemStack possibleMatch : ingredient.getItems()) {
            if (ItemStack.isSameItem(possibleMatch, inputItem)) {
              // Check if we have enough of the input item to satisfy the ingredient
              int requiredCount = possibleMatch.getCount();

              if (inputItem.getCount() >= requiredCount) {
                // If enough, decrease the input item count by the required amount
                inputItem.shrink(requiredCount);
                matched = true;
                break;
              } else {
                // If not enough, use all the input item and continue
                requiredCount -= inputItem.getCount();
                inputItem.setCount(0);
              }
            }
          }
        }

        // If we found a match and satisfied the ingredient, break out of the loop
        if (matched)
          break;
      }

      // If no input item could satisfy this ingredient, return false
      if (!matched) {
        // ModernUtils.LOGGER.debug("Crafting failed: Cannot craft");
        return false;
      }
    }
    // If all ingredients were matched with input items, return true
    return true;
  }

  private boolean canCraft() {
    // Check if we have necessary inputs and output space
    if (this.inputSlots.isCompletelyEmpty())
      return false;
    if (this.outputSlots.isFull())
      return false;
    if (this.outputSlots.allDisabled())
      return false;
    // Check if the recipe and result are valid
    if (this.recipe == null || this.result == null || this.craftingInputList == null)
      return false;
    if (this.result.isEmpty())
      return false;
    // Get the list of ingredients from the recipe
    NonNullList<Ingredient> ingredients = this.recipe.getIngredients();

    // Make a copy of the input items to avoid modifying the actual input slots
    // directly
    List<ItemStack> input = this.inputSlots.getStacksCopy();

    var validInput = inputCheck(input, ingredients);
    if (!validInput) {
      return false;
    }

    // Now check if there is enough space in the output slots for the result
    ItemStack result = this.result.copy();
    int remainingCount = result.getCount();

    for (int i = 0; i < this.outputSlots.getSlots(); i++) {
      ItemStack outputSlot = this.outputSlots.getStackInSlot(i);

      if (outputSlot.isEmpty() && !isSlotDisabled(i)) {
        // If the output slot is empty, it can hold the full remaining count
        remainingCount = 0;
        break;
      } else if (ItemStack.isSameItemSameComponents(outputSlot, result)) {
        // If the output slot contains the same item, calculate the available space
        int spaceAvailable = outputSlot.getMaxStackSize() - outputSlot.getCount();
        remainingCount -= Math.min(remainingCount, spaceAvailable);

        if (remainingCount <= 0) {
          break;
        }
      }
    }

    // If there's still remaining result that can't fit, crafting cannot proceed
    return remainingCount <= 0;
    // If all ingredients were matched and output slots can accommodate the result,
    // return true
  }

  private void craft() {
    // Get the list of ingredients from the recipe
    NonNullList<Ingredient> ingredients = this.recipe.getIngredients();
    // Get the crafting input from the actual input
    CraftingInput input = getCraftingInputFromActualInput(this.craftingInputList);
    // ModernUtils.LOGGER.debug("input: {}", input.items());

    // Get the remaining items
    NonNullList<ItemStack> remainingItems = this.recipe.baseRecipe.getRemainingItems(input);

    // Now take the items out of the input
    inputCheck(this.inputSlots.getStacks(), ingredients);

    // Put the result in the output
    ItemStack result = this.result.copy();
    int remainingCount = result.getCount();

    for (int i = 0; i < this.outputSlots.getSlots(); i++) {
      if (this.isSlotDisabled(i)) continue;
      ItemStack outputSlot = this.outputSlots.getStackInSlot(i);

      if (outputSlot.isEmpty()) {
        this.outputSlots.setStackInSlot(i, result);
        remainingCount = 0;
        break;
      } else if (ItemStack.isSameItemSameComponents(outputSlot, result)) {
        int spaceAvailable = outputSlot.getMaxStackSize() - outputSlot.getCount();
        int amountToAdd = Math.min(remainingCount, spaceAvailable);
        outputSlot.grow(amountToAdd);
        remainingCount -= amountToAdd;

        if (remainingCount <= 0) {
          break;
        }
      }
    }

    // Handle remaining items
    remainingCount = 0;
    var toPlaceIn = this.remainItemToggleValue == 0 ? this.inputSlots : this.outputSlots;
    // ModernUtils.LOGGER.debug("toPlaceIn: {}",this.remainItemToggleValue == 1 ? "Input" : "Output" );
    for (ItemStack remainingItem : remainingItems) {
      // ModernUtils.LOGGER.debug("remainingItem: {}", remainingItem);
      remainingCount += remainingItem.getCount();
      if (remainingItem.isEmpty())
        continue;
      for (int j = 0; j < toPlaceIn.getSlots(); j++) {
        ItemStack slot = toPlaceIn.getStackInSlot(j);
        if (ItemStack.isSameItemSameComponents(slot, remainingItem)) {
          int spaceAvailable = slot.getMaxStackSize() - slot.getCount();
          int amountToAdd = Math.min(remainingCount, spaceAvailable);
          slot.grow(amountToAdd);
          remainingCount -= amountToAdd;

          if (remainingCount <= 0) {
            break;
          }
        } else if (slot.isEmpty()) {
          toPlaceIn.setStackInSlot(j, remainingItem.copy());
          remainingCount = 0;
          break;
        }
      }

      setChanged();
    }
  }
  
  private boolean everySecond(double seconds){
    return this.tc % (20 * seconds) == 0;
  }
  
  public RedstoneManager getRedstoneManager() {
    return redstoneManager;
  }
  
  public String getRemainItemToggleDisplayValue() {
    return this.remainItemToggleValue == 0 ? "Input" : "Output";
  }
  
  public void setRemainItemToggleValue(int value) {
    this.remainItemToggleValue = value;
  }
  
  public int toggleRemainItemValue() {
    if (this.remainItemToggleValue == 0) {
      this.remainItemToggleValue = 1;
    } else {
      this.remainItemToggleValue = 0;
    }
    return this.remainItemToggleValue;
  }
  
  public ItemStack getRenderStack() {
    if (this.result == null)
      return ItemStack.EMPTY;
    if (ItemStack.isSameItemSameComponents(this.result, Items.END_CRYSTAL.getDefaultInstance()))
      return ItemStack.EMPTY;
    return this.result;
  }
  
  public void setRenderStack(ItemStack itemStack) {
    this.result = itemStack;
    this.craftingSlots.setStackInSlot(RESULT_SLOT, this.result);
  }

  public @Nullable IItemHandler getCombinedInvWrapper() {
    return this.combinedInvHandler;
  }
  
  public IItemHandler getCapabilityHandler(Direction side) {
    if (side == null) {
      return this.combinedInvHandler;
    }
    boolean canImport = importDirections.getOrDefault(side, false);
    boolean canExport = exportDirections.getOrDefault(side, false);
    
    return new IItemHandler() {
      @Override
      public int getSlots() {
        return combinedInvHandler.getSlots();
      }
      
      @Override
      public @NotNull ItemStack getStackInSlot(int slot) {
        return combinedInvHandler.getStackInSlot(slot);
      }
      
      @Override
      public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!canImport || slot >= inputSlots.getSlots()) {
          return stack;
        }
        return combinedInvHandler.insertItem(slot, stack, simulate);
      }
      
      @Override
      public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!canExport || slot < inputSlots.getSlots()) {
          return ItemStack.EMPTY;
        }
        return combinedInvHandler.extractItem(slot, amount, simulate);
      }
      
      @Override
      public int getSlotLimit(int slot) {
        return combinedInvHandler.getSlotLimit(slot);
      }
      
      @Override
      public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (!canImport || slot >= inputSlots.getSlots()) {
          return false;
        }
        return combinedInvHandler.isItemValid(slot, stack);
      }
    };
  }
  
  public CustomItemStackHandler getInputSlotsItemHandler() {
    return this.inputSlots;
  }
  
  public ItemStackHandler getOutputSlotsItemHandler() {
    return this.outputSlots;
  }
  
  public CraftingSlotHandler getCraftingSlotsItemHandler() {
    return this.craftingSlots;
  }
  
  public SimpleContainer getInputContainer() {
    var t = new SimpleContainer(inputSlots.getSlots());
    for (int i = 0; i < inputSlots.getSlots(); i++) {
      t.setItem(i, inputSlots.getStackInSlot(i));
    }
    return t;
  }
  
  public SimpleContainer getOutputContainer() {
    var t = new SimpleContainer(outputSlots.getSlots());
    for (int i = 0; i < outputSlots.getSlots(); i++) {
      t.setItem(i, outputSlots.getStackInSlot(i));
    }
    return t;
  }

  // Saving and loading
  CompoundTag getModData(HolderLookup.Provider registries) {
    CompoundTag modData = new CompoundTag();
    
    // Serialize input, output, and crafting slots
    modData.put("inputInv", this.inputSlots.serializeNBT(registries));
    modData.put("outputInv", this.outputSlots.serializeNBT(registries));
    modData.put("craftingInv", this.craftingSlots.serializeNBT(registries));
    
    // Store additional state variables
    modData.putInt("remainItemToggleValue", this.remainItemToggleValue);
    modData.putInt("redstoneMode", this.redstoneManager.getRedstoneMode().ordinal());
    modData.putBoolean("autoImportEnabled", this.autoImportEnabled);
    modData.putBoolean("autoExportEnabled", this.autoExportEnabled);
    
    // Save the result if it exists
    if (this.result != null && !this.result.isEmpty()) {
      modData.put("result", this.result.save(registries));
    }
    
    this.addDisabledSlots(modData);
    this.saveSidesConfig(modData);
    
    // Attempt to save the recipe, if available
    saveRecipeToNBT(modData, registries);
    
    return modData;
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
        case INPUT -> this.importDirections.put(sideDir,true);
        case OUTPUT -> this.exportDirections.put(sideDir,true);
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
  
  private void addDisabledSlots(CompoundTag tag) {
    IntList intlist = new IntArrayList();
    
    for (int i = 0; i < 9; i++) {
      if (this.isSlotDisabled(i)) {
        intlist.add(i);
      }
    }
    
    tag.putIntArray("disabled_slots", intlist);
  }
  
  public boolean isSlotDisabled(int slot) {
    return slot > -1 && slot < 9 && this.containerData.get(slot) == 1;
  }
  
  private void saveRecipeToNBT(CompoundTag modData, HolderLookup.Provider registries) {
    try {
      if (this.recipe instanceof CustomRecipeExtender<?> t) {
        modData.put("recipe", Recipe.CODEC.encodeStart(NbtOps.INSTANCE, t.baseRecipe).getOrThrow());
      }
    } catch (Exception e) {
      ModernUtilsMod.LOGGER.error("Error saving recipe to NBT: {}", e.getMessage());
    }
  }
  
  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    CompoundTag modData = getModData(registries);
    tag.put(ModernUtilsMod.MODID, modData);
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
  }
  
  private void deserializeFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    // Deserialize input, output, and crafting slots
    this.inputSlots.deserializeNBT(registries, tag.getCompound("inputInv"));
    this.outputSlots.deserializeNBT(registries, tag.getCompound("outputInv"));
    this.craftingSlots.deserializeNBT(registries, tag.getCompound("craftingInv"));
    
    // Load additional state variables
    this.remainItemToggleValue = tag.getInt("remainItemToggleValue");
    this.redstoneManager.setRedstoneMode(
        REDSTONE_MODE_MAP.get(tag.getInt("redstoneMode"))
    );
    this.autoImportEnabled = tag.getBoolean("autoImportEnabled");
    this.autoExportEnabled = tag.getBoolean("autoExportEnabled");
    this.result = ItemStack.parseOptional(registries, tag.getCompound("result"));
    
    // Load the recipe if it exists
    if (tag.contains("recipe")) {
      loadRecipeFromNBT(tag.getCompound("recipe"));
    }
    
    int[] aint = tag.getIntArray("disabled_slots");
    
    for (int i = 0; i < 9; i++) {
      this.containerData.set(i, 0);
    }
    
    for (int j : aint) {
      if (this.slotCanBeDisabled(j)) {
        this.containerData.set(j, 1);
      }
    }
    
    this.loadSidesConfig(tag);
  }
  
  private boolean slotCanBeDisabled(int slot) {
    return slot > -1 && slot < 9 && this.outputSlots.getStackInSlot(slot).isEmpty();
  }
  
  private void loadRecipeFromNBT(CompoundTag recipeTag) {
    var recipe = Recipe.CODEC.parse(NbtOps.INSTANCE, recipeTag).getOrThrow();
    if (recipe instanceof CraftingRecipe craftingRecipe) {
      this.recipe = new CustomRecipeExtender<>(craftingRecipe);
    }
  }

  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
    return getModData(registries);
  }

  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
  
  // Menu
  @Override
  public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory,
      @NotNull Player player) {
    return new MechanicalCrafterMenu(containerId, playerInventory, this,this.containerData);
  }
}
