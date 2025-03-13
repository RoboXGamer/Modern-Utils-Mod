package net.roboxgamer.modernutils.integrations.jade;

import net.minecraft.resources.ResourceLocation;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.custom.MagicBlock;
import net.roboxgamer.modernutils.block.custom.MechanicalCrafterBlock;
import net.roboxgamer.modernutils.block.custom.MechanicalFurnaceBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin(ModernUtilsMod.MODID)
public class ModJadePlugin implements IWailaPlugin {
  public static final ResourceLocation MAGICDATA =
      ModernUtilsMod.location("magicdata");
  public static final ResourceLocation FURNACEDATA =
     ModernUtilsMod.location("furnacedata");
  public static final ResourceLocation CRAFTERDATA =
      ModernUtilsMod.location("crafterdata");
  
  @Override
  public void register(IWailaCommonRegistration registration) {
    registration.registerBlockDataProvider(MagicBlockDataProvider.INSTANCE, MagicBlock.class);
    registration.registerBlockDataProvider(MechanicalFurnaceDataProvider.INSTANCE, MechanicalFurnaceBlock.class);
    registration.registerBlockDataProvider(MechanicalCrafterDataProvider.INSTANCE, MechanicalCrafterBlock.class);
  }
  
  @Override
  public void registerClient(IWailaClientRegistration registration) {
    // Register block components
    registration.registerBlockComponent(MagicBlockDataProvider.INSTANCE, MagicBlock.class);
    registration.registerBlockComponent(MechanicalFurnaceDataProvider.INSTANCE, MechanicalFurnaceBlock.class);
    registration.registerBlockComponent(MechanicalCrafterDataProvider.INSTANCE, MechanicalCrafterBlock.class);
  }
}
