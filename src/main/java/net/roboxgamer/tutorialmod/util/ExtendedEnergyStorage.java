package net.roboxgamer.tutorialmod.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.energy.EnergyStorage;

public class ExtendedEnergyStorage extends EnergyStorage {
  private final BlockEntity be;
  public ExtendedEnergyStorage(int capacity, BlockEntity blockEntity) {
    super(capacity);
    this.be = blockEntity;
  }
  
  public ExtendedEnergyStorage(int capacity, int maxTransfer, BlockEntity be) {
    super(capacity, maxTransfer);
    this.be = be;
  }
  
  public ExtendedEnergyStorage(int capacity, int maxReceive, int maxExtract, BlockEntity be) {
    super(capacity, maxReceive, maxExtract);
    this.be = be;
  }
  
  public ExtendedEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy, BlockEntity be) {
    super(capacity, maxReceive, maxExtract, energy);
    this.be = be;
  }
  
  /**
   * Sets the current energy amount to the specified value, clamping it to the range [0, capacity].
   *
   * @param energy the new energy amount
   */
  public void setEnergy(int energy) {
    if(energy < 0)
      energy = 0;
    if(energy > this.capacity)
      energy = this.capacity;
    this.energy = energy;
    onEnergyChanged();
  }
  
  /**
   * Adds the specified amount of energy to the current energy amount, clamping the result to the range [0, capacity].
   *
   * @param energy the amount of energy to add
   */
  public void addEnergy(int energy) {
    setEnergy(this.energy + energy);
  }
  
  /**
   * Removes the specified amount of energy from the current energy amount, clamping the result to the range [0, capacity].
   *
   * @param energy the amount of energy to remove
   */
  public void removeEnergy(int energy) {
    setEnergy(this.energy - energy);
  }
  
  /**
   * Returns the maximum amount of energy that can be received in a single operation.
   *
   * @return the maximum receive amount
   */
  public int getMaxReceive() {
    return this.maxReceive;
  }
  
  /**
   * Returns the maximum amount of energy that can be extracted in a single operation.
   *
   * @return the maximum extract amount
   */
  public int getMaxExtract() {
    return this.maxExtract;
  }
  
  @Override
  public int receiveEnergy(int toReceive, boolean simulate) {
    var toReturn = super.receiveEnergy(toReceive, simulate);
    if (!simulate) {
      onEnergyChanged();
    }
    return toReturn;
  }
  
  @Override
  public int extractEnergy(int toExtract, boolean simulate) {
    var toReturn = super.extractEnergy(toExtract, simulate);
    if (!simulate) {
      onEnergyChanged();
    }
    return toReturn;
  }
  
  public void onEnergyChanged() {
    this.be.setChanged();
  }
}
