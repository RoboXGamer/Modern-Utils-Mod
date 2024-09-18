package net.roboxgamer.tutorialmod.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CustomRecipeExtender<T extends CraftingRecipe> implements CraftingRecipe {
  private final T baseRecipe;
  private NonNullList<Ingredient> ingredients = NonNullList.create();
  
  public CustomRecipeExtender(T baseRecipe) {
    this.baseRecipe = baseRecipe;
  }
  
  public void setIngredients(NonNullList<Ingredient> ingredients) {
    this.ingredients = ingredients;
  }
  
  @Override
  public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
    return baseRecipe.matches(input, level);
  }
  
  @Override
  public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
    return baseRecipe.assemble(input, registries);
  }
  
  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return baseRecipe.canCraftInDimensions(width, height);
  }
  
  @Override
  public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
    return baseRecipe.getResultItem(registries);
  }
  
  public @NotNull NonNullList<Ingredient> getIngredients(){
    var t = baseRecipe.getIngredients();
    if (t.isEmpty()) return this.ingredients;
    return t;
  }
  
  @Override
  public @NotNull RecipeSerializer<?> getSerializer() {
    return baseRecipe.getSerializer();
  }
  
  @Override
  public @NotNull CraftingBookCategory category() {
    return baseRecipe.category();
  }
}
