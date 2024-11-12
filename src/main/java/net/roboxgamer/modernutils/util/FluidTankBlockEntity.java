package net.roboxgamer.modernutils.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlockEntity extends BlockEntity {
  private final FluidTank fluidTank;
  public FluidTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int capacity) {
    super(type, pos, blockState);
    this.fluidTank = new FluidTank(capacity);
  }
  
  // Delegate methods to fluidTank
  public FluidStack getFluid() {
    return fluidTank.getFluid();
  }
  
  public int getCapacity() {
    return fluidTank.getCapacity();
  }
  
  public int getFluidAmount(){
    return this.fluidTank.getFluidAmount();
  }
  
  public int getSpaceLeftInStorage(){
    return this.fluidTank.getSpace();
  }
  
  public boolean isTankEmpty(){
    return this.fluidTank.isEmpty();
  }
  
  public FluidTank readFluidTankNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt){
    return this.fluidTank.readFromNBT(lookupProvider,nbt);
  }
  
  public CompoundTag writeFluidTankNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt){
    return this.fluidTank.writeToNBT(lookupProvider,nbt);
  }
  
  public boolean isFluidValid(int tank, FluidStack stack) {
    return fluidTank.isFluidValid(tank, stack);
  }
  
  public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
    return fluidTank.fill(resource, action);
  }
  
  public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
    return fluidTank.drain(resource, action);
  }
  
  public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
    return fluidTank.drain(maxDrain, action);
  }
  
  public @Nullable IFluidHandler getFluidHandler() {
    return this.fluidTank;
  }
}
