package net.roboxgamer.modernutils.util;

import java.util.Set;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

public class Constants {
  public static final class Colors {
    public static final int BACKGROUND_SECONDARY = 0xFFDDE5EB;     // Light grayish-blue (main container)
    public static final int BACKGROUND_MAIN = 0xFFECEFF1; // Lighter grayish-blue (inner area)
    public static final int SLOT_BACKGROUND = 0xFFCED8E0;     // Slightly darker grayish-blue (slot background)
    
    public static final int PROGRESS_BACKGROUND = 0xFFCED8E0; // Slightly darker grayish-blue for progress background
    public static final int PROGRESS_FILL = 0xFF66BB6A;       // Muted Green for progress bar
    
    public static final int FUEL_BACKGROUND = 0xFFCED8E0;     // Slightly darker grayish-blue for fuel background
    public static final int FUEL_FILL = 0xFFFF7043;          // Muted Orange for fuel gauge
    
    public static final int BORDER_LIGHT = 0xFFB0BEC5;        // Lighter grayish-blue border
    public static final int BORDER_DARK = 0xFFA1ACB3;         // Darker grayish-blue border
  }

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
  
  public enum Sides {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    BACK,
    FRONT
  }
  
  public enum SideState {
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

  public static Set<Item> ALLOWED_FURNACE_ADDONS = Set.of(
      Items.COAL,
      Items.CHARCOAL,
      Items.DIAMOND,
      Items.IRON_INGOT,
      Items.GOLD_INGOT,
      Items.NETHERITE_INGOT,
      Items.BLAST_FURNACE,
      Items.SMOKER
  );

  // Define speed multipliers for furnace upgrades
  public static final Map<Item, Integer> FURNACE_SPEED_UPGRADES = Map.of(
      Items.COAL, 2,                // 2x speed
      Items.CHARCOAL, 2,           // 2x speed
      Items.IRON_INGOT, 3,         // 3x speed
      Items.GOLD_INGOT, 4,         // 4x speed
      Items.DIAMOND, 6,            // 6x speed
      Items.NETHERITE_INGOT, 8     // 8x speed
  );
  
  // Define all valid speed upgrade blocks with their corresponding speed multipliers for the mechanical crafter
  public static final Map<Item, Integer> CRAFTER_SPEED_UPGRADES = Map.of(
      Items.COAL_BLOCK, 2,             // 2x speed
      Items.IRON_BLOCK, 4,             // 4x speed 
      Items.GOLD_BLOCK, 8,             // 8x speed
      Items.REDSTONE_BLOCK, 12,        // 12x speed
      Items.DIAMOND_BLOCK, 20,         // 20x speed
      Items.NETHERITE_BLOCK, 50,       // 50x speed
      Items.AMETHYST_BLOCK, 100        // Instant crafting (100x speed - completes in 1 tick)
  );
  // For validation, just need the keys/items
  public static final Set<Item> ALLOWED_CRAFTER_ADDONS = CRAFTER_SPEED_UPGRADES.keySet();

  public interface IRedstoneConfigurable {
    RedstoneManager getRedstoneManager();
  }
  
  public interface ISidedMachine {
    SideManager getSideManager();
  }
  
  public interface IAddonSupport {
    AddonManager getAddonManager();
  }
}
