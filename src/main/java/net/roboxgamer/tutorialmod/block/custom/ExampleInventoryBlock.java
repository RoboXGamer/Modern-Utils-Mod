package net.roboxgamer.tutorialmod.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.roboxgamer.tutorialmod.block.entity.custom.ExampleInventoryBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExampleInventoryBlock extends Block implements EntityBlock {
  public ExampleInventoryBlock(Properties properties) {
    super(properties);
  }
  
  @Override
  public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new ExampleInventoryBlockEntity(pos, state);
  }
  
  @Override
  protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
    var hand = player.getUsedItemHand();
    if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof ExampleInventoryBlockEntity blockEntity) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStackHandler inventory = blockEntity.getInventory();
        if (stack.isEmpty()) {
        // Get the item from the block entity and give it to the player
          if (blockEntity.getItem().isEmpty()) {
            player.sendSystemMessage(Component.literal("No item in the block entity"));
            return InteractionResult.SUCCESS;
          }
          
          ItemStack extracted = inventory.extractItem(0,player.isCrouching() ? inventory.getSlotLimit(0) : 1 , false);
          player.setItemInHand(hand,extracted);
        } else {
        //  Set the item in the block entity to the item in the player's hand (assuming the blockentity can store the item)
          ItemStack toInsert = stack.copy();
          toInsert.setCount(1);
          
          ItemStack leftOver = inventory.insertItem(0,toInsert,false);
          
          ItemStack remainder = stack.copy();
          remainder.setCount(remainder.getCount() - 1);
          remainder.grow(leftOver.getCount());
          player.setItemInHand(hand,remainder);
        }
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.sidedSuccess(level.isClientSide());
  }
  
  @Override
  protected void onRemove(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
    if (!level.isClientSide()) {
      BlockEntity be = level.getBlockEntity(pos);
      if (be instanceof ExampleInventoryBlockEntity blockEntity) {
        ItemStackHandler inventory = blockEntity.getInventory();
        for (int i = 0; i < inventory.getSlots(); i++) {
          ItemStack stack = inventory.getStackInSlot(i);
          var entity = new ItemEntity(level,pos.getX() + 0.5,pos.getY() + 0.5,pos.getZ() + 0.5, stack);
          level.addFreshEntity(entity);
        }
      }
    }
    
    super.onRemove(state, level, pos, newState, movedByPiston);
  }
}
