package net.roboxgamer.tutorialmod.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.tutorialmod.TutorialMod;

import java.util.function.Supplier;

public class ModMenuTypes {
  public static final DeferredRegister<MenuType<?>> MENUS =
      DeferredRegister.create(BuiltInRegistries.MENU, TutorialMod.MODID);
  
  public static final Supplier<MenuType<MechanicalCrafterMenu>> MECHANICAL_CRAFTER_MENU = MENUS.register("mechanical_crafter_menu", () -> IMenuTypeExtension.create(MechanicalCrafterMenu::new));
  
  
  
  public static void register(IEventBus eventBus) {
    MENUS.register(eventBus);
  }
}
