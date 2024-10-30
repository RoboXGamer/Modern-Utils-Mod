package net.roboxgamer.modernutils.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.modernutils.ModernUtilsMod;

import java.util.function.Supplier;

public class ModCreativeModTabs {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TAB
      = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModernUtilsMod.MODID);

  public static final Supplier<CreativeModeTab> MODERN_UTILS_TAB =
      CREATIVE_MOD_TAB.register("tutorial_mod_tab",
                                () -> CreativeModeTab.builder()
                                    .icon(() -> new ItemStack(ModItems.EXAMPLE_ITEM.get()))
                                    .title(
                                        Component.translatable("creativetab.modernutils.tab"))
                                    .displayItems((parameters, output) -> {
                                      ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
                                    })
                                    .build());

  public static void register(IEventBus eventBus) {
    CREATIVE_MOD_TAB.register(eventBus);
  }
}
