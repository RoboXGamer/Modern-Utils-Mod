package net.roboxgamer.modernutils.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.modernutils.ModernUtilsMod;

public class ModItems {
  public static final DeferredRegister.Items ITEMS =
      DeferredRegister.createItems(ModernUtilsMod.MODID);
  
  public static final DeferredRegister.Items WIP_ITEMS =
      DeferredRegister.createItems(ModernUtilsMod.MODID);

  public static final DeferredItem<Item> EXAMPLE_ITEM = WIP_ITEMS.register("example_item",()-> new Item(new Item.Properties()));

  public static void register(IEventBus eventBus) {
    ITEMS.register(eventBus);
    WIP_ITEMS.register(eventBus);
  }
}
