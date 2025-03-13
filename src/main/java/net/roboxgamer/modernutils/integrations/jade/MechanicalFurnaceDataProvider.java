package net.roboxgamer.modernutils.integrations.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalFurnaceBlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum MechanicalFurnaceDataProvider implements
    IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
  INSTANCE;

  @Override
  public void appendTooltip(
      ITooltip tooltip,
      BlockAccessor accessor,
      IPluginConfig config) {
    CompoundTag data = accessor.getServerData();
    
    // Progress information
    boolean hasFuel = data.getBoolean("hasFuel");
    boolean hasFuelStack = data.getBoolean("hasFuelStack");
    String fuelBurnTime = data.getString("fuelBurnTime");
    
    // Add fuel status
    tooltip.add(Component.translatable("tooltip.modernutils.mechanical_furnace.fuel_status", 
        hasFuel ? "§aActive" : "§cInactive"));
    
    // Add burn time if fuel is present
    if (hasFuelStack || hasFuel) {
        tooltip.add(Component.translatable("tooltip.modernutils.mechanical_furnace.burn_time", 
            fuelBurnTime));
    }

    // Add redstone mode
    String redstoneMode = data.getString("redstoneMode");
    tooltip.add(Component.translatable("tooltip.modernutils.mechanical_furnace.redstone_mode", 
        redstoneMode));

    // Add furnace state
    String furnaceState = data.getString("furnaceState");
    String stateColor = switch (furnaceState) {
        case "RUNNING" -> "§a";
        case "IDLE" -> "§e";
        case "DISABLED" -> "§c";
        case "ERROR" -> "§4";
        default -> "§7";
    };
    tooltip.add(Component.translatable("tooltip.modernutils.mechanical_furnace.state", 
        stateColor + furnaceState));
  }

  @Override
  public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
    if (accessor.getBlockEntity() instanceof MechanicalFurnaceBlockEntity be) {
        // Fuel data
        boolean hasFuel = be.hasFuel();
        tag.putBoolean("hasFuel", hasFuel);
        boolean hasFuelStack = be.hasFuelStack();
        tag.putBoolean("hasFuelStack", hasFuelStack);
        
        // Get burn time of current fuel if present
        if (hasFuelStack || hasFuel) {
            tag.putString("fuelBurnTime", be.getFuelBurnTimeString());
        }

        // Add redstone mode
        tag.putString("redstoneMode", be.getRedstoneMode().toString());

        // Add furnace state
        tag.putString("furnaceState", be.getFurnaceState().toString());
    }
  }

  @Override
  public ResourceLocation getUid() {
    return ModJadePlugin.FURNACEDATA;
  }
}
