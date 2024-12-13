package net.roboxgamer.modernutils.util;

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
  };
  
  public enum RecipeTypes {
    BLACKLISTED,
    SPECIAL
  }
  
  public enum Sides{
    UP,
    DOWN,
    LEFT,
    RIGHT,
    BACK,
    FRONT
  }
  
  public enum SideState{
    NONE,
    INPUT,
    OUTPUT,
    BOTH,
  }
  
  public static int getColorForMode(Constants.SideState mode) {
    return switch (mode) {
      case INPUT -> 0xAAFF0000;  // Red with some transparency
      case OUTPUT -> 0xAA0000FF; // Blue with some transparency
      case BOTH -> 0xAA800080;   // Purple with some transparency
      default -> 0xAA555555;     // Grey with some transparency
    };
  }
  
  public interface IRedstoneConfigurable {
    RedstoneManager getRedstoneManager();
  }
  
  public interface ISidedMachine {
    SideManager getSideManager();
  }
}
