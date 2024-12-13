package net.roboxgamer.modernutils.integrations.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum MechanicalCrafterDataProvider implements
    IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;

  @Override
  public void appendTooltip(
      ITooltip tooltip,
      BlockAccessor accessor,
      IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    
    // Add redstone mode
    String redstoneMode = data.getString("redstoneMode");
    tooltip.add(Component.literal("Redstone Mode: " + redstoneMode));

    // Add crafter state
    boolean isProcessing = data.getBoolean("isProcessing");
    boolean canProcess = data.getBoolean("canProcess");
    
    String stateColor;
    String state;
    
    if (!canProcess) {
        stateColor = "§c"; // Red
        state = "DISABLED";
    } else if (isProcessing) {
        stateColor = "§a"; // Green
        state = "RUNNING";
    } else {
        stateColor = "§e"; // Yellow
        state = "IDLE";
    }
    
    tooltip.add(Component.literal("State: " + stateColor + state));
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof MechanicalCrafterBlockEntity be) {
        // Add redstone mode
        tag.putString("redstoneMode", be.getRedstoneManager().getRedstoneMode().toString());
        
        // Add processing state
        boolean canProcess = switch (be.getRedstoneManager().getRedstoneMode()) {
            case ALWAYS_ON -> true;
            case REDSTONE_ON -> accessor.getLevel().hasNeighborSignal(accessor.getPosition());
            case REDSTONE_OFF -> !accessor.getLevel().hasNeighborSignal(accessor.getPosition());
            case PULSE -> false; // Pulse mode is considered not processing by default
        };
        
        tag.putBoolean("canProcess", canProcess);
        
        // Check if actually processing (has valid recipe and items)
        boolean isProcessing = canProcess && !be.getInputSlotsItemHandler().isCompletelyEmpty() 
            && !be.getOutputSlotsItemHandler().isFull();
        tag.putBoolean("isProcessing", isProcessing);
    }
  }

  @Override
  public ResourceLocation getUid() {
    return ModJadePlugin.CRAFTERDATA;
  }
} 