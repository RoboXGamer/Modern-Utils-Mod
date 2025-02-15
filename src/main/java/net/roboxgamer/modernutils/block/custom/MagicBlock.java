package net.roboxgamer.modernutils.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import net.roboxgamer.modernutils.block.entity.custom.MagicBlockBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagicBlock extends Block implements EntityBlock {
  public MagicBlock(Properties properties) {
    super(properties);
  }

  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
    return ModBlockEntities.MAGIC_BLOCK_BE.get().create(pos, state);
  }

  @Override
  protected @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
    return RenderShape.MODEL;
  }

  @Override
  protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level,
      @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
    if (level.isClientSide) {
      return InteractionResult.SUCCESS;
    }
    var hand = player.getUsedItemHand();
    if (hand != InteractionHand.MAIN_HAND)
      return InteractionResult.PASS;
    BlockEntity be = level.getBlockEntity(pos);
    if (!(be instanceof MagicBlockBlockEntity blockEntity))
      return InteractionResult.PASS;
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      serverPlayer.openMenu(blockEntity, pos);
    }
    return InteractionResult.CONSUME;
  }

  @Override
  public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState blockState,
      @NotNull BlockEntityType<T> blockEntityType) {
    return level.isClientSide ? null
        : ((level1, pos, state, blockEntity) -> ((MagicBlockBlockEntity) blockEntity).tick());
  }
}
