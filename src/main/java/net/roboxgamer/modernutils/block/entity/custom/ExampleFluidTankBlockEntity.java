package net.roboxgamer.modernutils.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import net.roboxgamer.modernutils.util.FluidTankBlockEntity;

public class ExampleFluidTankBlockEntity extends FluidTankBlockEntity {
  public ExampleFluidTankBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.EXAMPLE_FLUID_TANK_BLOCK_ENTITY.get(), pos, blockState,Integer.MAX_VALUE);
  }
}
