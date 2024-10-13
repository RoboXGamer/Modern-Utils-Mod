package net.roboxgamer.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.tutorialmod.block.entity.custom.renderer.MiniChestBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniChestBlock extends Block implements EntityBlock {
  public MiniChestBlock(Properties properties) {
    super(properties);
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
