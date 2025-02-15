package net.roboxgamer.modernutils.integrations.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.roboxgamer.modernutils.block.entity.custom.MagicBlockBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public enum MagicBlockDataProvider implements
    IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;
  
  @Override
  public void appendTooltip(
      ITooltip iTooltip,
      BlockAccessor blockAccessor,
      IPluginConfig iPluginConfig) {
    CompoundTag data = blockAccessor.getServerData();
    IElementHelper helper = IElementHelper.get();
    
    iTooltip.add(helper.text(Component.translatable("tooltip.modernutils.speed", data.getInt("speed"))));
    iTooltip.add(helper.text(Component.translatable("tooltip.modernutils.offset", 
        data.getInt("offsetX"), data.getInt("offsetY"), data.getInt("offsetZ"))));
  }
  
  @Override
  public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
    MagicBlockBlockEntity be = (MagicBlockBlockEntity) blockAccessor.getBlockEntity();
    compoundTag.putInt("speed", be.getSpeed());
    compoundTag.putInt("offsetX", be.getOffsetX());
    compoundTag.putInt("offsetY", be.getOffsetY());
    compoundTag.putInt("offsetZ", be.getOffsetZ());
  }
  
  @Override
  public ResourceLocation getUid() {
    return ModJadePlugin.MAGICDATA;
  }
}
