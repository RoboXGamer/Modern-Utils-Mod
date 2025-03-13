package net.roboxgamer.modernutils.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.modernutils.ModernUtilsMod;

import java.util.function.Supplier;

public class ModMenuTypes {
  public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU,
      ModernUtilsMod.MODID);

  public static final Supplier<MenuType<MechanicalCrafterMenu>> MECHANICAL_CRAFTER_MENU = MENUS.register(
      "mechanical_crafter_menu", () -> IMenuTypeExtension.create(MechanicalCrafterMenu::new));

  public static final Supplier<MenuType<MiniChestMenu>> MINI_CHEST_MENU = MENUS.register(
      "mini_chest_menu", () -> IMenuTypeExtension.create(MiniChestMenu::new));

  public static final Supplier<MenuType<MagicBlockMenu>> MAGIC_BLOCK_MENU = MENUS.register(
      "magic_block_menu", () -> IMenuTypeExtension.create(MagicBlockMenu::new));

  public static final Supplier<MenuType<MechanicalFurnaceMenu>> MECHANICAL_FURNACE_MENU = MENUS.register(
      "mechanical_furnace_menu", () -> IMenuTypeExtension.create(MechanicalFurnaceMenu::new));

  public static void register(IEventBus eventBus) {
    MENUS.register(eventBus);
  }
}
