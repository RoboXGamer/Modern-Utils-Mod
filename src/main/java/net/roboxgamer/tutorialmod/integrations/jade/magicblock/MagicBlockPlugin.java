package net.roboxgamer.tutorialmod.integrations.jade.magicblock;

import net.minecraft.resources.ResourceLocation;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.custom.MagicBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MagicBlockPlugin implements IWailaPlugin {
  public static final ResourceLocation DATA =
      ResourceLocation.fromNamespaceAndPath(TutorialMod.MODID,"data");

  @Override
  public void register(IWailaCommonRegistration registration) {
    registration.registerBlockDataProvider(MagicBlockDataProvider.INSTANCE, MagicBlock.class);
  }

  @Override
  public void registerClient(IWailaClientRegistration registration) {
    registration.registerBlockComponent(MagicBlockDataProvider.INSTANCE, MagicBlock.class);
  }
}
