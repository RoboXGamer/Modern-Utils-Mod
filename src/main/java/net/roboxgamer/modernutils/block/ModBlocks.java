package net.roboxgamer.modernutils.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.custom.*;
import net.roboxgamer.modernutils.item.ModItems;

import java.util.function.Supplier;


public class ModBlocks {
  public static final DeferredRegister.Blocks BLOCKS =
      DeferredRegister.createBlocks(ModernUtilsMod.MODID);
  
  public static final DeferredRegister.Blocks WIP_BLOCKS =
      DeferredRegister.createBlocks(ModernUtilsMod.MODID);

  public static final DeferredBlock<Block> EXAMPLE_BLOCK = registerWIPBlock("example_block",() ->
      new Block(BlockBehaviour.Properties.of().sound(SoundType.AMETHYST).strength(1.0F, 1.0F)));

  public static final DeferredBlock<MagicBlock> MAGIC_BLOCK = registerBlock("magic_block", ()->
      new MagicBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

  public static final DeferredBlock<MechanicalCrafterBlock> MECHANICAL_CRAFTER_BLOCK = registerBlock("mechanical_crafter_block",()->
      new MechanicalCrafterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

  public static final DeferredBlock<ExampleInventoryBlock> EXAMPLE_INVENTORY_BLOCK = registerWIPBlock("example_inventory_block", ()->
      new ExampleInventoryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
  
  public static final DeferredBlock<MiniChestBlock> MINI_CHEST_BLOCK = registerBlock("mini_chest_block", ()->
      new MiniChestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
  
  public static final DeferredBlock<BatteryBlock> BATTERY_BLOCK = registerWIPBlock("battery_block", ()->
      new BatteryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
  
  private static <T extends Block> DeferredBlock<T> registerWIPBlock(String name, Supplier<T> block) {
    DeferredBlock<T> toReturn = WIP_BLOCKS.register(name, block);
    registerWIPBlockItem(name, toReturn);
    return toReturn;
  }

  private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
    DeferredBlock<T> toReturn = BLOCKS.register(name, block);
    registerBlockItem(name, toReturn);
    return toReturn;
  }
  
  public static <T extends Block> void registerWIPBlockItem(String name, DeferredBlock<T> block) {
    ModItems.WIP_ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
  }

  public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
    ModItems.ITEMS.register(name, ()-> new BlockItem(block.get(), new Item.Properties()));
  }

  public static void register(IEventBus eventBus) {
    BLOCKS.register(eventBus);
    WIP_BLOCKS.register(eventBus);
  }
}
