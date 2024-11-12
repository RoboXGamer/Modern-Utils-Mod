package net.roboxgamer.modernutils.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.roboxgamer.modernutils.block.entity.custom.ExampleFluidTankBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExampleFluidTankBlock extends Block implements EntityBlock {
  public ExampleFluidTankBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }
  
  @Override
  public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
    return new ExampleFluidTankBlockEntity(blockPos, blockState);
  }
  
  @Override
  protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
    if (!(level.getBlockEntity(pos) instanceof ExampleFluidTankBlockEntity tank)) {
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    
    Item item = stack.getItem();
    
    // Case 1: Player is holding a filled bucket
    if (item instanceof BucketItem bucketItem) {
      Fluid fluidInBucket = bucketItem.content;
      
      // If the bucket is filled with a fluid, try to insert it into the tank
      if (fluidInBucket != Fluids.EMPTY) {
        FluidStack fluidStack = new FluidStack(fluidInBucket, 1000); // Buckets contain 1000 mB
        int filledAmount = tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
        
        if (filledAmount > 0) {
          if (!level.isClientSide) {
            // Replace filled bucket with an empty bucket
            if (!player.getAbilities().instabuild) {
              stack.shrink(1);
              ItemStack emptyBucket = new ItemStack(Items.BUCKET);
              if (!player.addItem(emptyBucket)) {
                player.drop(emptyBucket, false);
              }
            }
          }
          var sound = bucketItem.content.getFluidType().getSound(player, level, pos, SoundActions.BUCKET_EMPTY);
          var soundSource = SoundSource.BLOCKS;
          if (sound != null) {
            level.playSound(player, pos, sound, soundSource, 1.0F, 1.0F);
          }
          return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
      }
    }
    
    // Case 2: Player is holding an empty bucket and trying to extract fluid from the tank
    if (stack.is(Items.BUCKET)) {
      FluidStack tankFluid = tank.getFluid();
      if (!tankFluid.isEmpty() && tankFluid.getAmount() >= 1000) { // Check if there's enough fluid in the tank
        Fluid fluidToExtract = tankFluid.getFluid();
        
        // Try to extract 1000 mB (1 bucket worth) from the tank
        FluidStack extractedFluid = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        if (!extractedFluid.isEmpty() && extractedFluid.getAmount() == 1000) {
          if (!level.isClientSide) {
            // Replace empty bucket with a filled bucket
            if (!player.getAbilities().instabuild) {
              stack.shrink(1);
              ItemStack filledBucket = new ItemStack(fluidToExtract.getBucket());
              if (!player.addItem(filledBucket)) {
                player.drop(filledBucket, false);
              }
            }
          }
          var sound = fluidToExtract.getFluidType().getSound(player, level, pos, SoundActions.BUCKET_FILL);
          var soundSource = SoundSource.BLOCKS;
          if (sound != null) {
            level.playSound(player, pos, sound, soundSource, 1.0F, 1.0F);
          }
          return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
      }
    }
    
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }
  
}
