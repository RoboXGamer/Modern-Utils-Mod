package net.roboxgamer.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalCrafterBlock extends Block implements EntityBlock {
  public static BooleanProperty POWERED = BlockStateProperties.POWERED;
  public static DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  
  public MechanicalCrafterBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(FACING, Direction.NORTH));
  }
  
  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(POWERED);
    builder.add(FACING);
  }
  
  @Override
  public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
    Level level = context.getLevel();
    BlockPos pos = context.getClickedPos();
    return this.defaultBlockState()
        .setValue(FACING, context.getHorizontalDirection().getOpposite())
        .setValue(POWERED, level.hasNeighborSignal(pos));
  }
  
  @Override
  protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
    if (level instanceof ServerLevel serverLevel) {
      boolean currentPowered = state.getValue(POWERED);
      boolean isGettingPowered = level.hasNeighborSignal(pos);
      if (isGettingPowered != currentPowered) {
        serverLevel.setBlock(pos, state.setValue(POWERED, isGettingPowered), Block.UPDATE_ALL);
      }
      if (level.getBlockEntity(pos) instanceof MechanicalCrafterBlockEntity blockEntity) {
        blockEntity.setChanged();
      }
    }
    super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
  }
  
  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
    return ModBlockEntities.MECHANICAL_CRAFTER_BE.get().create(pos, state);
  }
  
  @Override
  protected @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
    return RenderShape.MODEL;
  }
  
  @Override
  public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
    return level.isClientSide ? null : ((level1, pos, state, blockEntity) -> ((MechanicalCrafterBlockEntity) blockEntity).tick());
  }
  
  @Override
  protected void onRemove(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
    if (!state.is(newState.getBlock())) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof MechanicalCrafterBlockEntity blockEntity) {
        SimpleContainer inputInv = blockEntity.getInputContainer();
        SimpleContainer outputInv = blockEntity.getOutputContainer();
        Containers.dropContents(level, pos, inputInv);
        Containers.dropContents(level, pos, outputInv);
      }
      super.onRemove(state, level, pos, newState, movedByPiston);
      level.updateNeighbourForOutputSignal(pos, this);
    } else {
      super.onRemove(state, level, pos, newState, movedByPiston);
    }
  }
  
  @Override
  protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
    if (level.isClientSide) {
      return InteractionResult.SUCCESS;
    }
    var hand = player.getUsedItemHand();
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    BlockEntity be = level.getBlockEntity(pos);
    if (!(be instanceof MechanicalCrafterBlockEntity blockEntity)) return InteractionResult.PASS;
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      serverPlayer.openMenu(blockEntity, pos);
    }
    return InteractionResult.CONSUME;
  }
}
