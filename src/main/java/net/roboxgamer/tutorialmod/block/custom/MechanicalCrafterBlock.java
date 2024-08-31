package net.roboxgamer.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalCrafterBlock extends Block implements EntityBlock {
  
  public MechanicalCrafterBlock(Properties properties) {
    super(properties);
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
    if (!level.isClientSide()) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof MechanicalCrafterBlockEntity blockEntity) {
        SimpleContainer inputInv = blockEntity.getInputContainer();
        SimpleContainer outputInv = blockEntity.getOutputContainer();
        Containers.dropContents(level, pos, inputInv);
        Containers.dropContents(level, pos, outputInv);
        
        //for (int i = 0; i < inputInv.getSlots(); i++) {
        //  ItemStack stack = inputInv.getStackInSlot(i);
        //  var entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        //  level.addFreshEntity(entity);
        //}
        //ItemStackHandler outputInv = blockEntity.getOutputSlotsItemHandler();
        //for (int i = 0; i < outputInv.getSlots(); i++) {
        //  ItemStack stack = outputInv.getStackInSlot(i);
        //  var entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        //  level.addFreshEntity(entity);
        //}
      }
    }
    
    super.onRemove(state, level, pos, newState, movedByPiston);
  }
  
 
  
  @Override
  protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull BlockHitResult hitResult) {
    var hand = player.getUsedItemHand();
    if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
    BlockEntity be = level.getBlockEntity(pos);
    if (!(be instanceof MechanicalCrafterBlockEntity blockEntity)) return InteractionResult.PASS;
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      serverPlayer.openMenu(blockEntity, pos);
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.FAIL;
  }
}
