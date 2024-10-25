package net.roboxgamer.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.roboxgamer.tutorialmod.block.entity.custom.BatteryBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BatteryBlock extends Block implements EntityBlock {
  
  public BatteryBlock(Properties properties) {
    super(properties);
  }
  
  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new BatteryBlockEntity(blockPos, blockState);
  }
  
  @Override
  public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, BlockEntityType<T> blockEntityType) {
    return level.isClientSide ? null : ((level1, pos, state1, blockEntity) -> ((BatteryBlockEntity) blockEntity).tick());
  }
}
