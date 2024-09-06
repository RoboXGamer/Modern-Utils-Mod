package net.roboxgamer.tutorialmod.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MechanicalCrafterBlockEntity extends BlockEntity implements MenuProvider {
  private static final int RESULT_SLOT = 0;
  public Component TITLE = Component.translatable("block.tutorialmod.mechanical_crafter_block");
  
  private int tc = 0;
  private CraftingRecipe recipe;
  private ItemStack result;
  private NonNullList<ItemStack> remainingItems;
  
  public static final int INPUT_SLOTS_COUNT = 9;
  public static final int OUTPUT_SLOTS_COUNT = 9;
  public static final int CRAFT_RESULT_SLOT = 0;
  public static final int[] CRAFT_RECIPE_SLOTS = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
  
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
    
    public NonNullList<ItemStack> getStacksCopy() {
      var t = NonNullList.withSize(this.stacks.size(), ItemStack.EMPTY);
      for (int i = 0; i < this.stacks.size(); i++) {
        t.set(i, this.stacks.get(i).copy());
      }
      return t;
    }
    
    public boolean isCompletelyEmpty() {
      //  if all the slots are empty, return true
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
  }
  
  public class CraftingSlotHandler extends CustomItemStackHandler {
    public CraftingSlotHandler(int size) {
      super(size);
    }
    
      @Override
      protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      if (slot == 0) return;
      Level level = MechanicalCrafterBlockEntity.this.getLevel();
      if (level == null || level.isClientSide() || !(level instanceof ServerLevel slevel)) return;
      BlockEntity blockEntity = slevel.getBlockEntity(MechanicalCrafterBlockEntity.this.getBlockPos());
      if (!(blockEntity instanceof MechanicalCrafterBlockEntity be)) return;
      be.recipe = be.getRecipe(slevel);
      be.result = be.getResult(slevel);
      if (be.craftingSlots.getStackInSlot(RESULT_SLOT).isEmpty() || !be.craftingSlots.getStackInSlot(RESULT_SLOT).is(be.result.getItem())) {
        be.craftingSlots.setStackInSlot(0, be.result);
      }
    }
  }
  
  CustomItemStackHandler inputSlots = new CustomItemStackHandler(9);
  CustomItemStackHandler outputSlots = new CustomItemStackHandler(9);
  CraftingSlotHandler craftingSlots = new CraftingSlotHandler(10);
  
  //Combine handler of input and output slots
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
        // If the slot is in the inputSlots, prevent extraction and return an empty ItemStack
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
        // If the slot is in the outputSlots, prevent insertion and return the stack unchanged
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
      
      // Check if the slot is within the input or output slots and return their respective limits
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
      
      // Check if the slot is within the input slots and allow only valid items for input slots
      if (slot < inputSlotCount) {
        return inputSlots.isItemValid(slot, stack); // Input slot
      } else {
        return false; // No item is valid for output slots
      }
    }
  };
  
  private ItemStack getResult(ServerLevel slevel) {
    if (this.recipe == null) return ItemStack.EMPTY;
    return this.recipe.getResultItem(slevel.registryAccess()).copy();
  }
  
  Lazy<CustomItemStackHandler> combinedInvHandlerLazy = Lazy.of(() -> new CustomItemStackHandler(combinedInvHandler.getSlots()));
  
  public MechanicalCrafterBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.MECHANICAL_CRAFTER_BE.get(), pos, blockState);
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
  
  public List<ItemStack> getInputStacks() {
    return this.inputSlots.getStacks();
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
  
  @Override
  public @NotNull Component getDisplayName() {
    return TITLE;
  }
  
  
  public void tick() {
//  Ticking logic
    this.tc++;
    if (this.tc == 20 * 60) this.tc = 0;
    
    Level level = this.getLevel();
    if (level == null) return;
    if (level.isClientSide()) return;
    if (!(level instanceof ServerLevel slevel)) return;
    
    if (this.tc == 1) {
      this.recipe = getRecipe((ServerLevel) this.level);
      if (this.recipe != null) {
        this.result = this.recipe.getResultItem(level.registryAccess()).copy();
        this.craftingSlots.setStackInSlot(0, this.result);
      }
    }

    BlockEntity blockEntity = slevel.getBlockEntity(this.getBlockPos());
    if (!(blockEntity instanceof MechanicalCrafterBlockEntity)) return;
    
    // *** Logic for crafting ***
    
   if (everySecond()) {
     if (canCraft()) {
       //TutorialMod.LOGGER.info("Can Craft!");
       craft();
     }
   }
    
    
    // REFERENCE CODE
    //RecipeManager recipes = slevel.getRecipeManager();
    //CraftingInput input = CraftingInput.of(1, 1, List.of(Items.DIAMOND_BLOCK.getDefaultInstance()));
    //RecipeType<CraftingRecipe> recipeType = RecipeType.CRAFTING;
    //Optional<RecipeHolder<CraftingRecipe>> optional = recipes.getRecipeFor(
    //    // The recipe type to get the recipe for. In our case, we use the crafting type.
    //    recipeType,
    //    // Our recipe input.
    //    input,
    //    // Our level context.
    //    slevel
    //);
    //optional.map(RecipeHolder::value).ifPresent(recipe -> {
    //  // Do whatever you want here. Note that the recipe is now a Recipe<CraftingInput> instead of a Recipe<?>.
    //  ItemStack result = recipe.getResultItem(level.registryAccess());
    //  TutorialMod.LOGGER.info("Result: " + result);
    //});
          /*
          ItemStackHandler inputSlots = be.getInputSlotsItemHandler();
          ItemStackHandler outputSlots = be.getOutputSlotsItemHandler();
          ItemStack input = inputSlots.getStackInSlot(0);
          //  Assuming the crafting recipe of oak_logs to oak_planks
          boolean toCraft = false;
          var slot = -1;
          ItemStack result = new ItemStack(Blocks.OAK_PLANKS, 4);
          for (int i = 0; i < inputSlots.getSlots(); i++) {
            ItemStack stack = inputSlots.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() != Blocks.OAK_LOG.asItem()) continue;
            //  Assuming the crafting recipe of oak_logs to oak_planks
            toCraft = true;
            slot = i;
            break;
          }
          if (!toCraft) return;
          for (int i = 0; i < outputSlots.getSlots(); i++) {
            ItemStack stack = outputSlots.getStackInSlot(i);
            if (stack.isEmpty()) {
              outputSlots.setStackInSlot(i, result);
              toCraft = false;
              break;
            }
            if (stack.getItem() == result.getItem()) {
              if (stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(4);
                outputSlots.setStackInSlot(i, stack);
                toCraft = false;
                break;
              }
            }
          }
          if (toCraft) return;
          //  Consume input
          var iStack = inputSlots.getStackInSlot(slot);
          iStack.shrink(1);
          inputSlots.setStackInSlot(slot, iStack);
        }
      }
      */
    
    
  }
  
  private CraftingRecipe getRecipe(ServerLevel level) {
    if (this.craftingSlots.isCompletelyEmpty()) return null;
    RecipeManager recipes = level.getRecipeManager();
    RecipeType<CraftingRecipe> recipeType = RecipeType.CRAFTING;
    var l = this.craftingSlots.getStacks();
    NonNullList<ItemStack> l2 = NonNullList.withSize(l.size(), ItemStack.EMPTY);
    for (int i = 1; i < l.size(); i++) {
      l2.set(i - 1, l.get(i));
    }
    TutorialMod.LOGGER.info("l2: {}", l2);
    CraftingInput input = CraftingInput.of(3, 3, l2);
    List<RecipeHolder<CraftingRecipe>> list = recipes.getRecipesFor(recipeType, input, level);
    if (list.isEmpty()) return null;
    RecipeHolder<CraftingRecipe> foundRecipe = list.getFirst();
    TutorialMod.LOGGER.info("foundRecipe: {}", foundRecipe);
    ItemStack result = foundRecipe.value().getResultItem(level.registryAccess()).copy();
    TutorialMod.LOGGER.info("result: {}", result);
    if (result.isEmpty()) {
      return null;
    }
    this.remainingItems = foundRecipe.value().getRemainingItems(input);
    return foundRecipe.value();
  }
  
  private boolean inputCheck(List<ItemStack> input, List<Ingredient> ingredients) {
    // Iterate over each ingredient in the recipe
    for (Ingredient ingredient : ingredients) {
      boolean matched = false;
      if (ingredient.getItems().length == 0) continue;
      
      // Iterate over each item in the input slots
      for (ItemStack inputItem : input) {
        if (inputItem.isEmpty()) continue;
        if (ingredient.test(inputItem)) {
          // Find the specific ingredient option that matches the input item
          for (ItemStack possibleMatch : ingredient.getItems()) {
            if (possibleMatch.is(inputItem.getItem())) {
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
        if (matched) break;
      }
      
      // If no input item could satisfy this ingredient, return false
      if (!matched) {
        //TutorialMod.LOGGER.debug("Crafting failed: Cannot craft");
        return false;
      }
    }
    // If all ingredients were matched with input items, return true
    return true;
  }
  
  private boolean canCraft() {
    // Check if we have necessary inputs and output space
    if (this.inputSlots.isCompletelyEmpty()) return false;
    if (this.outputSlots.isFull()) return false;
    // Check if the recipe and result are valid
    if (this.recipe == null || this.result == null) return false;
    if (this.result.isEmpty()) return false;
    
    // Get the list of ingredients from the recipe
    NonNullList<Ingredient> ingredients = this.recipe.getIngredients();
    
    // Make a copy of the input items to avoid modifying the actual input slots directly
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
      
      if (outputSlot.isEmpty()) {
        // If the output slot is empty, it can hold the full remaining count
        remainingCount = 0;
        break;
      } else if (outputSlot.is(result.getItem())) {
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
    // If all ingredients were matched and output slots can accommodate the result, return true
  }
  
  private void craft() {
    // Get the list of ingredients from the recipe
    NonNullList<Ingredient> ingredients = this.recipe.getIngredients();
    
    // Now take the items out of the input
    inputCheck(getInputStacks(), ingredients);
    
    // Put the result in the output
    ItemStack result = this.result.copy();
    int remainingCount = result.getCount();
    
    for (int i = 0; i < this.outputSlots.getSlots(); i++) {
      ItemStack outputSlot = this.outputSlots.getStackInSlot(i);
      
      if (outputSlot.isEmpty()) {
        this.outputSlots.setStackInSlot(i, result);
        remainingCount = 0;
        break;
      } else if (outputSlot.is(result.getItem())) {
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
    for (ItemStack remainingItem : this.remainingItems) {
      //TutorialMod.LOGGER.info("remainingItem: {}", remainingItem);
      remainingCount += remainingItem.getCount();
      if (remainingItem.isEmpty()) continue;
      //  Place in the input slot that is not full
      for (int j = 0; j < this.inputSlots.getSlots(); j++) {
        ItemStack inputSlot = this.inputSlots.getStackInSlot(j);
        if (inputSlot.is(remainingItem.getItem())) {
          int spaceAvailable = inputSlot.getMaxStackSize() - inputSlot.getCount();
          int amountToAdd = Math.min(remainingCount, spaceAvailable);
          inputSlot.grow(amountToAdd);
          remainingCount -= amountToAdd;
          
          if (remainingCount <= 0) {
            break;
          }
        } else if (inputSlot.isEmpty()) {
          this.inputSlots.setStackInSlot(j, remainingItem.copy());
          remainingCount = 0;
          break;
        }
      }
      
      setChanged();
    }
  }
  
  
  public void recheckRecipe(ServerLevel level) {
    this.recipe = getRecipe(level);
    if (this.recipe == null) {
      this.result = ItemStack.EMPTY;
      return;
    }
    this.result = this.recipe.getResultItem(level.registryAccess()).copy();
    this.craftingSlots.setStackInSlot(0, this.result);
    setChanged();
  }
  
  
  private boolean everySecond() {
    return this.tc % 20 == 0;
  }
  
  public @Nullable IItemHandler getCombinedInvWrapper() {
    return this.combinedInvHandler;
  }
  
  CompoundTag getTutorialModData(HolderLookup.Provider registries) {
    CompoundTag tutorialModData = new CompoundTag();
    tutorialModData.put("inputInv",
                        this.inputSlots.serializeNBT(registries));
    tutorialModData.put("outputInv",
                        this.outputSlots.serializeNBT(registries));
    tutorialModData.put("craftingInv",
                        this.craftingSlots.serializeNBT(registries));
    return tutorialModData;
  }
  
  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    tag.put(TutorialMod.MODID, getTutorialModData(registries));
  }
  
  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);
    CompoundTag tutorialmodData = tag.getCompound(TutorialMod.MODID);
    this.inputSlots.deserializeNBT
        (registries, tutorialmodData.getCompound("inputInv"));
    this.outputSlots.deserializeNBT
        (registries, tutorialmodData.getCompound("outputInv"));
    this.craftingSlots.deserializeNBT
        (registries, tutorialmodData.getCompound("craftingInv"));
  }
  
  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
    return getTutorialModData(registries);
  }
  
  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
  
  @Override
  public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
    return new MechanicalCrafterMenu(containerId, playerInventory, this);
  }
}
