package net.roboxgamer.modernutils.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.roboxgamer.modernutils.block.entity.custom.MiniChestBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniChestBlock extends Block implements EntityBlock {
  public static DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
  
  public MiniChestBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
  }
  
  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }
  
  @Override
  public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
    return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }
  
  private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
  
  @Override
  protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
    return SHAPE;
  }
  
  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
    return new MiniChestBlockEntity(pos, state);
  }
  
  @Override
  protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
    if (!state.is(newState.getBlock())) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof MiniChestBlockEntity blockEntity) {
        SimpleContainer inv = blockEntity.getInvContainer();
        Containers.dropContents(level, pos, inv);
      }
    }
    super.onRemove(state, level, pos, newState, movedByPiston);
  }
  
  @Override
  protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
    if (level.isClientSide) {
      return InteractionResult.SUCCESS;
    }
    var hand = player.getUsedItemHand();
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    BlockEntity be = level.getBlockEntity(pos);
    if (!(be instanceof MiniChestBlockEntity blockEntity)) return InteractionResult.PASS;
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      serverPlayer.openMenu(blockEntity, pos);
    }
    return InteractionResult.CONSUME;
  }
}
