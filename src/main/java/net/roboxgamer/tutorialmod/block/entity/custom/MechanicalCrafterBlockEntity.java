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
import net.neoforged.neoforge.items.ItemStackHandler;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MechanicalCrafterBlockEntity extends BlockEntity implements MenuProvider {
  public Component TITLE = Component.translatable("block.tutorialmod.mechanical_crafter_block");
  
  private int tc = 0;
  private CraftingRecipe recipe;
  
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
  
  CustomItemStackHandler inputSlots = new CustomItemStackHandler(9);
  CustomItemStackHandler outputSlots = new CustomItemStackHandler(9);
  CustomItemStackHandler craftingSlots = new CustomItemStackHandler(10){
    @Override
    protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      Level level = MechanicalCrafterBlockEntity.this.getLevel();
      if (level == null) return;
      if (level.isClientSide()) return;
      if (!(level instanceof ServerLevel slevel)) return;
      BlockEntity blockEntity = slevel.getBlockEntity(MechanicalCrafterBlockEntity.this.getBlockPos());
      if (!(blockEntity instanceof MechanicalCrafterBlockEntity be)) return;
      be.hasValidRecipe(slevel);
      be.recipe = be.getRecipe(slevel);
    }
  };
  
  public static final int INPUT_SLOTS_COUNT = 9;
  public static final int OUTPUT_SLOTS_COUNT = 9;
  public static final int CRAFT_RESULT_SLOT = 0;
  public static final int[] CRAFT_RECIPE_SLOTS = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
  
  Lazy<ItemStackHandler> inputSlotsLazy = Lazy.of(() -> new ItemStackHandler(inputSlots.getSlots()));
  Lazy<ItemStackHandler> outputSlotsLazy = Lazy.of(() -> new ItemStackHandler(outputSlots.getSlots()));
  Lazy<ItemStackHandler> craftingSlotsLazy = Lazy.of(() -> new ItemStackHandler(craftingSlots.getSlots()));
  
  
  public MechanicalCrafterBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.MECHANICAL_CRAFTER_BE.get(), pos, blockState);
  }
  
  public CustomItemStackHandler getInputSlotsItemHandler() {
    return this.inputSlots;
  }
  
  public ItemStackHandler getOutputSlotsItemHandler() {
    return this.outputSlots;
  }
  
  public CustomItemStackHandler getCraftingSlotsItemHandler() {
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
//    TODO: Ticking logic
    this.tc++;
    if (this.tc == 20 * 60) this.tc = 0;
    
    if (this.tc == 1) this.recipe = getRecipe((ServerLevel) this.level);
    
    Level level = this.getLevel();
    if (level == null) return;
    if (level.isClientSide()) return;
    if (!(level instanceof ServerLevel slevel)) return;
    BlockEntity blockEntity = slevel.getBlockEntity(this.getBlockPos());
    if (!(blockEntity instanceof MechanicalCrafterBlockEntity be)) return;
    
    // *** Logic for crafting ***
    
    if (canCraft()) {
      craft(slevel);
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
    CraftingInput input = CraftingInput.of(3,3,this.craftingSlots.getStacks());
    RecipeType<CraftingRecipe> recipeType = RecipeType.CRAFTING;
    Optional<RecipeHolder<CraftingRecipe>> optionalRecipe = recipes.getRecipeFor(recipeType, input, level);
    var foundRecipe = optionalRecipe.map(RecipeHolder::value).orElse(null);
    if (foundRecipe == null){
      //this.craftingSlots.setStackInSlot(0, ItemStack.EMPTY);
      return null;
    }
    ItemStack result = optionalRecipe
        .map(RecipeHolder::value)
        .map(e -> e.assemble(input, level.registryAccess()))
        .orElse(ItemStack.EMPTY);
    if (result.isEmpty()) return null;
    this.craftingSlots.setStackInSlot(0, result);
    return foundRecipe;
  }
  private boolean hasValidRecipe(ServerLevel level) {
    if (this.craftingSlots.isCompletelyEmpty()) {
      return false;
    }
    
    RecipeManager recipes = level.getRecipeManager();
    CraftingInput input = CraftingInput.of(3,3,this.craftingSlots.getStacks());
    
    RecipeType<CraftingRecipe> recipeType = RecipeType.CRAFTING;
    Optional<RecipeHolder<CraftingRecipe>> optionalRecipe = recipes.getRecipeFor(recipeType, input, level);
    if (optionalRecipe.isPresent()) {
      CraftingRecipe foundRecipe = optionalRecipe.get().value();
      ItemStack result = foundRecipe.getResultItem(level.registryAccess());
      
      if (!result.isEmpty()) {
        this.recipe = foundRecipe;
        TutorialMod.LOGGER.info("Crafting Result: {}", result);
        return true;
      }
    }
    
    this.recipe = null;
    return false;
  }
  
  private boolean canCraft() {
    // Check if we have necessary inputs and output space
    // Check if all input slots are not empty
    boolean isInputEmpty = this.inputSlots.isCompletelyEmpty();
    if (isInputEmpty) return false;
    // Check if output slots are not full
    boolean isOutputFull = this.outputSlots.isFull();
    if (isOutputFull) return false;
    if (this.recipe == null) return false;
    // Check if the ingredients needed for the recipe are in the input slots
    NonNullList<Ingredient> ingredients = this.recipe.getIngredients();
    for (Ingredient ingredient : ingredients) {
      for (int i = 0; i < this.inputSlots.getSlots(); i++) {
        if (this.inputSlots.getStackInSlot(i).isEmpty()) continue;
        if (this.inputSlots.getStackInSlot(i).getItem() == ingredient.getItems()[0].getItem()) {
          if (this.inputSlots.getStackInSlot(i).getCount() >= ingredient.getItems()[0].getCount()) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private void craft(ServerLevel level) {
  //  Take the items out of the input
    boolean takenOutInput = false;
    for (int i = 0; i < this.inputSlots.getSlots(); i++) {
      if (this.inputSlots.getStackInSlot(i).isEmpty()) continue;
      for (Ingredient ingredient : this.recipe.getIngredients()) {
        ItemStack inputItemStack = this.inputSlots.getStackInSlot(i);
        ItemStack ingredientItemStack = ingredient.getItems()[0];
        int inputCount = inputItemStack.getCount();
        int ingredientCount = ingredientItemStack.getCount();
        if (inputItemStack.getItem() == ingredientItemStack.getItem()) {
          if (inputCount >= ingredientCount) {
            this.inputSlots.getStackInSlot(i).shrink(ingredientCount);
            takenOutInput = true;
            break;
          }
        }
      }
      if (takenOutInput) break;
    }
    ItemStack result = this.recipe.getResultItem(level.registryAccess());
    //  Put the result in the output
    for (int i = 0; i < this.outputSlots.getSlots(); i++) {
      if (this.outputSlots.getStackInSlot(i).isEmpty()) {
        this.outputSlots.setStackInSlot(i, result);
        break;
      }
    }
    setChanged();
    resetCrafting();
  }
  
  
  
  private void resetCrafting() {
    // Clear current recipe and reset progress
    this.recipe = null;
  }
    
  
  
  
  private boolean everySecond() {
    return this.tc % 20 == 0;
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
