package net.roboxgamer.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.block.entity.custom.MagicBlockBlockEntity;
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
  protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
    if (!level.isClientSide() && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
      boolean isEmptyHand = player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();
      if (!isEmptyHand) return InteractionResult.PASS;
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof MagicBlockBlockEntity blockEntity) {
        boolean isCrouching = player.isCrouching();
        if (isCrouching) {
          int speed = blockEntity.getSpeed();
          player.sendSystemMessage(Component.literal("Speed: " + speed));
          return InteractionResult.CONSUME;
        }
        int speed = blockEntity.incrementSpeed();
        player.sendSystemMessage(Component.literal("Speed: " + speed));
        return InteractionResult.CONSUME;
      }
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
    return level.isClientSide ? null : ((level1, pos, state, blockEntity) -> ((MagicBlockBlockEntity) blockEntity).tick());
  }
}
