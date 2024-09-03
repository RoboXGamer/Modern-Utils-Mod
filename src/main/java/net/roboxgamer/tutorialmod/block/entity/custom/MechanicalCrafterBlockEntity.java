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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MechanicalCrafterBlockEntity extends BlockEntity implements MenuProvider {
  public Component TITLE = Component.translatable("block.tutorialmod.mechanical_crafter_block");
  
  private int tc = 0;
  private CraftingRecipe recipe;
  private ItemStack result;
  
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
  
  CustomItemStackHandler inputSlots = new CustomItemStackHandler(9);
  CustomItemStackHandler outputSlots = new CustomItemStackHandler(9);
  CustomItemStackHandler craftingSlots = new CustomItemStackHandler(10){
    @Override
    protected void onContentsChanged(int slot) {
      super.onContentsChanged(slot);
      if (slot == 0) return;
      Level level = MechanicalCrafterBlockEntity.this.getLevel();
      if (level == null) return;
      if (level.isClientSide()) return;
      if (!(level instanceof ServerLevel slevel)) return;
      BlockEntity blockEntity = slevel.getBlockEntity(MechanicalCrafterBlockEntity.this.getBlockPos());
      if (!(blockEntity instanceof MechanicalCrafterBlockEntity be)) return;
      be.recipe = be.getRecipe(slevel);
      be.result = be.getResult(slevel);
      be.craftingSlots.setStackInSlot(0, be.result);
    }
  };
  
  private ItemStack getResult(ServerLevel slevel) {
    if (this.recipe == null) return ItemStack.EMPTY;
    return this.recipe.getResultItem(slevel.registryAccess()).copy();
  }
  
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
    if (!(blockEntity instanceof MechanicalCrafterBlockEntity be)) return;
    
    // *** Logic for crafting ***
    
    if (canCraft()) {
      TutorialMod.LOGGER.info("Can Craft!");
      //craft(slevel);
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
      l2.set(i-1, l.get(i));
    }
    TutorialMod.LOGGER.info("l2: {}", l2);
    CraftingInput input = CraftingInput.of(3,3,l2);
    List<RecipeHolder<CraftingRecipe>> list = recipes.getRecipesFor(recipeType, input, level);
    if (list.isEmpty()) return null;
    RecipeHolder<CraftingRecipe> foundRecipe = list.getFirst();
    TutorialMod.LOGGER.info("foundRecipe: {}", foundRecipe);
    ItemStack result = foundRecipe.value().getResultItem(level.registryAccess()).copy();
    TutorialMod.LOGGER.info("result: {}", result);
    if (result.isEmpty()){
      return null;
    }
    return foundRecipe.value();
  }
  
  private boolean canCraft() {
    // Check if we have necessary inputs and output space
    if (this.inputSlots.isCompletelyEmpty()) return false;
    // Check if output slots are not full
    if (this.outputSlots.isFull()) return false;
    if (this.recipe == null || this.result == null){
      return false;
    }
    else if (this.result.isEmpty()){
      return false;
    }
    NonNullList<Ingredient> ingredients = this.recipe.getIngredients();
    // make a copy of the ingredients list to avoid concurrent modification
    List<ItemStack[]> ig = new ArrayList<>();
    ingredients.forEach(
        e -> ig.add(e.getItems().clone())
    );
    var input = this.inputSlots.getStacksCopy();
    var l = ig.stream().flatMap(Arrays::stream).toList();
    l.forEach(
        item -> {
          input.forEach(
              inputItem -> {
                if (inputItem.isEmpty()) return;
                if (inputItem.getItem() == item.getItem()) {
                  if (inputItem.getCount() >= item.getCount()) {
                    inputItem.shrink(item.getCount());
                    item.setCount(0);
                  }else{
                    item.shrink(inputItem.getCount());
                    inputItem.setCount(0);
                  }
                }
              }
          );
        }
    );
    // Check now if all the ingredients were satisfied
    var inputUsed = l.stream().allMatch(ItemStack::isEmpty);
    TutorialMod.LOGGER.debug("inputUsed: {}", inputUsed);
    
    //  Check if the ingredients needed for the recipe are in the input slots
    //TutorialMod.LOGGER.debug("Ingredients: {}", ingredients);
    return false;
  }
  public void recheckRecipe(ServerLevel level) {
    this.recipe = getRecipe(level);
    if (this.recipe == null){
      this.result = ItemStack.EMPTY;
      return;
    }
    this.result = this.recipe.getResultItem(level.registryAccess()).copy();
    this.craftingSlots.setStackInSlot(0, this.result);
    setChanged();
  }
  
  private void craft(ServerLevel level) {
  //  Take the items out of the input
    List<Ingredient> ingredients = this.recipe.getIngredients();
    ingredients.forEach(
        ingredient -> Arrays.stream(ingredient.getItems()).forEach(
            itemStack -> {
            //  Here this is the itemStack of the ingredient to remove
              this.inputSlots.getStacks().forEach(
                  inputItemStack -> {
                    //  Here this is the itemStack of the input slot
                    if (inputItemStack.isEmpty()) return;
                    if (inputItemStack.getItem() == itemStack.getItem()) {
                      if (inputItemStack.getCount() >= itemStack.getCount()) {
                        inputItemStack.shrink(itemStack.getCount());
                      }else{
                        itemStack.shrink(inputItemStack.getCount());
                        inputItemStack.setCount(0);
                      }
                    }
                  }
              );
            }
        )
    );
    //  Put the result in the output
    for (int i = 0; i < this.outputSlots.getSlots(); i++) {
      if (this.outputSlots.getStackInSlot(i).isEmpty()) {
        this.outputSlots.setStackInSlot(i, this.result.copy());
        break;
      }
      else if (this.outputSlots.getStackInSlot(i).getItem() == this.result.getItem()) {
        if (this.outputSlots.getStackInSlot(i).getCount() < this.outputSlots.getStackInSlot(i).getMaxStackSize()) {
          this.outputSlots.getStackInSlot(i).grow(this.result.getCount());
          break;
        }
      }
    }
    setChanged();
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
