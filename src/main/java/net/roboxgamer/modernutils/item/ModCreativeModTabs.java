package net.roboxgamer.modernutils.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.modernutils.Config;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeModTabs {
  public static final DeferredRegister<CreativeModeTab> CREATIVE_MOD_TAB
      = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModernUtilsMod.MODID);

  public static final Supplier<CreativeModeTab> MODERN_UTILS_TAB =
      CREATIVE_MOD_TAB.register("modernutils_tab",
                                () -> CreativeModeTab.builder()
                                    .icon(() -> new ItemStack(ModItems.EXAMPLE_ITEM.get()))
                                    .title(
                                        Component.translatable("creativetab.modernutils.tab"))
                                    .displayItems((parameters, output) -> {
                                      ModItems.ITEMS.getEntries().forEach(item -> {
                                        var i = item.get();
                                        var isMagicBlock = i.asItem().equals(
                                            ModBlocks.MAGIC_BLOCK.asItem());
                                        if (isMagicBlock && Config.enabledMagicBlock) {
                                          output.accept(i);
                                        }
                                        else if (!isMagicBlock) {
                                          output.accept(i);
                                        }
                                      });
                                    })
                                    .build());

  public static void register(IEventBus eventBus) {
    CREATIVE_MOD_TAB.register(eventBus);
  }
}
