package net.roboxgamer.tutorialmod.util;

import net.minecraft.world.item.crafting.*;

public class Constants {
  public static final Class<?>[] MECHANICAL_CRAFTER_SPECIAL_RECIPES = new Class<?>[]{
      TippedArrowRecipe.class,
      FireworkRocketRecipe.class,
      FireworkStarRecipe.class,
      FireworkStarFadeRecipe.class,
  };
  public static final Class<?>[] MECHANICAL_CRAFTER_BLACKLISTED_RECIPES = new Class<?>[]{
      RepairItemRecipe.class,
      MapCloningRecipe.class,
      ArmorDyeRecipe.class,
      BannerDuplicateRecipe.class,
      BookCloningRecipe.class,
      DecoratedPotRecipe.class,
      ShieldDecorationRecipe.class,
      ShulkerBoxColoring.class, // TODO: Needs more research
      SuspiciousStewRecipe.class // TODO: Needs more research
  };
  
  public enum RecipeTypes {
    BLACKLISTED,
    SPECIAL
  }
}
