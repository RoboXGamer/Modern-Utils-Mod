package net.roboxgamer.modernutils.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.data.KillData;
import java.util.function.Supplier;

public class ModCustomDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModernUtilsMod.MODID);

    

    public static final Supplier<DataComponentType<BlockPos>> LAST_POS = DATA_COMPONENT_TYPES.registerComponentType("last_pos",
            builder -> builder
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(StreamCodec.composite(
                            ByteBufCodecs.INT, BlockPos::getX,
                            ByteBufCodecs.INT, BlockPos::getY,
                            ByteBufCodecs.INT, BlockPos::getZ,
                            (x, y, z) -> new BlockPos(x, y, z))));

    public static final Supplier<DataComponentType<KillData>> KILL_DATA = DATA_COMPONENT_TYPES.registerComponentType("kill_data",
            builder -> builder
                    .persistent(KillData.CODEC)
                    .networkSynchronized(KillData.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
