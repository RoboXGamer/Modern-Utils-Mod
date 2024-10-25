package net.roboxgamer.tutorialmod.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.util.ExtendedEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BatteryBlockEntity extends BlockEntity {
  private int tc = 0;
  
  private final ExtendedEnergyStorage energyStorage = new ExtendedEnergyStorage(10000, 10000, 0, this);
  
  public BatteryBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.BATTERY_BLOCK_ENTITY.get(), pos, blockState);
  }
  
  CompoundTag getTutorialModData(HolderLookup.Provider registries) {
    CompoundTag tutorialModData = new CompoundTag();
    
    tutorialModData.put("Energy", this.energyStorage.serializeNBT(registries));
    return tutorialModData;
  }
  
  private void deserializeFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    this.energyStorage.deserializeNBT(registries, Objects.requireNonNull(tag.get("Energy")));
  }
  
  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);
    // Check if we are on the client side
    if (level != null && level.isClientSide()) {
      // Deserialize data from the tag for client-side
      deserializeFromTag(tag, registries);
    } else {
      CompoundTag tutorialModData = tag.getCompound(TutorialMod.MODID);
      deserializeFromTag(tutorialModData, registries);
    }
  }
  
  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    CompoundTag tutorialModData = getTutorialModData(registries);
    tag.put(TutorialMod.MODID, tutorialModData);
  }
  
  public void tick() {
    this.tc++;
    if (everySecond()) tc = 0;
    
    Level level = this.getLevel();
    BlockPos pos = this.getBlockPos();
    if (level == null || level.isClientSide() || !(level instanceof ServerLevel slevel) || !(level.getBlockEntity(
        pos) instanceof BatteryBlockEntity be)) return;
    
    if (everySecond(1)) {
      //  TESTING
      //be.energyStorage.removeEnergy(100);
    }
  }

private boolean everySecond() {
  return this.tc % (20) == 0;
}

private boolean everySecond(double s) {
  return this.tc % (20 * s) == 0;
}

public EnergyStorage getEnergyStorage() {
  return this.energyStorage;
}
}

