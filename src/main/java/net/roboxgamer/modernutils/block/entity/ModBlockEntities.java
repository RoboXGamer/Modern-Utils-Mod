package net.roboxgamer.modernutils.block.entity;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.block.entity.custom.BatteryBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.ExampleInventoryBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.MagicBlockBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalFurnaceBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.MiniChestBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
  public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
      DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModernUtilsMod.MODID);

  public static final Supplier<BlockEntityType<MagicBlockBlockEntity>> MAGIC_BLOCK_BE =
      BLOCK_ENTITIES.register("magic_block_be", () -> BlockEntityType.Builder.
          of(MagicBlockBlockEntity::new,
             ModBlocks.MAGIC_BLOCK.get())
          .build(null));

  public static final Supplier<BlockEntityType<MechanicalCrafterBlockEntity>> MECHANICAL_CRAFTER_BE =
      BLOCK_ENTITIES.register("mechanical_crafter_be", () -> BlockEntityType.Builder.
          of(MechanicalCrafterBlockEntity::new,
             ModBlocks.MECHANICAL_CRAFTER_BLOCK.get())
          .build(null));

  public static final Supplier<BlockEntityType<ExampleInventoryBlockEntity>> EXAMPLE_INVENTORY_BE =
      BLOCK_ENTITIES.register("example_inventory_be", () -> BlockEntityType.Builder.
          of(ExampleInventoryBlockEntity::new,
             ModBlocks.EXAMPLE_INVENTORY_BLOCK.get())
          .build(null));
  
  public static final Supplier<BlockEntityType<MiniChestBlockEntity>> MINI_CHEST_BLOCK_ENTITY =
      BLOCK_ENTITIES.register("mini_chest_block_entity", () -> BlockEntityType.Builder.
          of(MiniChestBlockEntity::new,
             ModBlocks.MINI_CHEST_BLOCK.get())
          .build(null));
  
  public static final Supplier<BlockEntityType<BatteryBlockEntity>> BATTERY_BLOCK_ENTITY =
      BLOCK_ENTITIES.register("battery_block_entity", () -> BlockEntityType.Builder.
          of(BatteryBlockEntity::new,
             ModBlocks.BATTERY_BLOCK.get())
          .build(null));

  public static final Supplier<BlockEntityType<MechanicalFurnaceBlockEntity>> MECHANICAL_FURNACE_BE =
      BLOCK_ENTITIES.register("mechanical_furnace_be", () -> BlockEntityType.Builder.
          of(MechanicalFurnaceBlockEntity::new,
             ModBlocks.MECHANICAL_FURNACE_BLOCK.get())
          .build(null));
  
  
  public static void register(IEventBus eventBus) {
    BLOCK_ENTITIES.register(eventBus);
  }
}
