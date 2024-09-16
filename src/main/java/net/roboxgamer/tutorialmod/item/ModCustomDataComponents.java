package net.roboxgamer.tutorialmod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.tutorialmod.TutorialMod;

public class ModCustomDataComponents {
  public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TutorialMod.MODID);
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> BLOCK_POS_DATA_COMPONENT = DATA_COMPONENTS.registerComponentType(
      "block_pos",
      builder -> builder
          // The codec to read/write the data to disk
          .persistent(BlockPos.CODEC) // Here put the codec of the type you want
          // The codec to read/write the data across the network
          .networkSynchronized(BlockPos.STREAM_CODEC) // Here put the stream codec of the type you want
  );
  
  public static void register(IEventBus eventbus) {
    DATA_COMPONENTS.register(eventbus);
  }
}
