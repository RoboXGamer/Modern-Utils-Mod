package net.roboxgamer.tutorialmod.integrations.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.*;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.roboxgamer.tutorialmod.menu.CraftingGhostSlotItemHandler;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import net.roboxgamer.tutorialmod.menu.ModMenuTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MechanicalCrafterRecipeTransferHandler implements IRecipeTransferHandler<MechanicalCrafterMenu, RecipeHolder<CraftingRecipe>> {
  @Override
  public @NotNull Class<? extends MechanicalCrafterMenu> getContainerClass() {
    return MechanicalCrafterMenu.class;
  }
  
  @Override
  public @NotNull Optional<MenuType<MechanicalCrafterMenu>> getMenuType() {
    return Optional.of(ModMenuTypes.MECHANICAL_CRAFTER_MENU.get());
  }
  
  @Override
  public @NotNull RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
    return RecipeTypes.CRAFTING;
  }
  
  @Override
  public @Nullable IRecipeTransferError transferRecipe(@NotNull MechanicalCrafterMenu container,
                                                       @NotNull RecipeHolder<CraftingRecipe> recipe,
                                                       @NotNull IRecipeSlotsView recipeSlots,
                                                       @NotNull Player player,
                                                       boolean maxTransfer,
                                                       boolean doTransfer) {
    // Get the recipe's required ingredients
    List<Ingredient> ingredients = recipe.value().getIngredients();
    IRecipeTransferHandlerHelper handlerHelper = new RecipeTransferHandlerHelper(stackHelper, craftingCategory);
    // Ensure the recipe fits in the ghost crafting slots
    if (ingredients.size() > 9) {
      return IRecipeTransferError.createUserError("The recipe is too large for the available crafting slots.");
    }
    
    // If we're only validating (doTransfer == false), ensure that the player has the necessary items
    if (!doTransfer) {
      for (Ingredient ingredient : ingredients) {
        // Check if the player has the required item in their inventory
        if (!hasItemInInventory(player, ingredient)) {
          return IRecipeTransferError.createUserError("You do not have the required items.");
        }
      }
      return null;  // No error, recipe can be transferred
    }
    
    // If we are transferring (doTransfer == true), place items in ghost slots
    int slotIndex = 0;
    for (Ingredient ingredient : ingredients) {
      ItemStack matchingStack = findMatchingStack(player, ingredient);
      
      if (!matchingStack.isEmpty()) {
        // Get the ghost slot in the container
        Slot ghostSlot = container.slots.get(slotIndex);
        if (ghostSlot instanceof CraftingGhostSlotItemHandler) {
          ItemStack ghostItem = matchingStack.copy();
          ghostItem.setCount(1);  // Set to 1 for ghost slot behavior
          ghostSlot.set(ghostItem);  // Place the item in the ghost slot
        }
      }
      slotIndex++;
    }
    
    return null;  // Return null to indicate success
  }
  
  // Helper method to check if the player has the required item for the ingredient
  private boolean hasItemInInventory(Player player, Ingredient ingredient) {
    for (ItemStack stack : player.getInventory().items) {
      if (ingredient.test(stack)) {
        return true;
      }
    }
    return false;
  }
  
  // Helper method to find the matching stack from the player's inventory for an ingredient
  private ItemStack findMatchingStack(Player player, Ingredient ingredient) {
    for (ItemStack stack : player.getInventory().items) {
      if (ingredient.test(stack)) {
        return stack;
      }
    }
    return ItemStack.EMPTY;
  }
  
}