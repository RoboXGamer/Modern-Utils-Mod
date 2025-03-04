package net.roboxgamer.modernutils;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ModernUtilsMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED_MAGIC_BLOCK = BUILDER
            .comment("Whether to enable the magic block")
            .define("enableMagicBlock", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static boolean enabledMagicBlock;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        enabledMagicBlock = ENABLED_MAGIC_BLOCK.get();
    }
}
