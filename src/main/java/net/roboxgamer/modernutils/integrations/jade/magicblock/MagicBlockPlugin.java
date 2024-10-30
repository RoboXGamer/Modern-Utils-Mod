package net.roboxgamer.modernutils.integrations.jade.magicblock;

import net.minecraft.resources.ResourceLocation;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.custom.MagicBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MagicBlockPlugin implements IWailaPlugin {
  public static final ResourceLocation DATA =
      ModernUtilsMod.location("data");

  @Override
  public void register(IWailaCommonRegistration registration) {
    registration.registerBlockDataProvider(MagicBlockDataProvider.INSTANCE, MagicBlock.class);
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerBlockComponent(MagicBlockDataProvider.INSTANCE, MagicBlock.class);
  }
}
