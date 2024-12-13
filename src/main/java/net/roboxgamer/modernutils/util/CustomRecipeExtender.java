package net.roboxgamer.modernutils.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class CustomRecipeExtender<T extends CraftingRecipe> {
  public final T baseRecipe;
  private NonNullList<Ingredient> ingredients = NonNullList.create();
  
  public CustomRecipeExtender(T baseRecipe) {
    this.baseRecipe = baseRecipe;
  }
  
  public void setIngredients(NonNullList<Ingredient> ingredients) {
    this.ingredients = ingredients;
  }
  
  public @NotNull NonNullList<Ingredient> getIngredients(){
    var t = baseRecipe.getIngredients();
    if (t.isEmpty()) return this.ingredients;
    return t;
  }
}
