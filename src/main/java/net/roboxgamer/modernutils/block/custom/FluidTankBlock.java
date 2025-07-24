package net.roboxgamer.modernutils.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.roboxgamer.modernutils.block.entity.custom.FluidTankBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class FluidTankBlock extends Block implements EntityBlock {
    public FluidTankBlock(Properties properties) {
        super(properties);
    }
    public static int capacity = 10000;
    
    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FluidTankBlockEntity(pos, state, capacity);
    }
    
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState blockState, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult blockHitResult) {
            IFluidHandlerItem fluidHandlerItem = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidHandlerItem != null) {
                if (stack.getItem() instanceof BucketItem) {
                    if (!(level.getBlockEntity(pos) instanceof FluidTankBlockEntity tankBE)) {
                                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                            }
                            var tank = tankBE.getFluidHandler();

                            Item item = stack.getItem();

                            // Case 1: Player is holding a filled bucket
                            if (item instanceof BucketItem bucketItem && !(item instanceof MobBucketItem)) {
                                Fluid fluidInBucket = bucketItem.content;

                                // If the bucket is filled with a fluid, try to insert it into the tank
                                if (fluidInBucket != Fluids.EMPTY) {
                                    FluidStack fluidStack = new FluidStack(fluidInBucket, 1000); // Buckets contain 1000 mB
                                    int filledAmount = tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);

                                    if (filledAmount > 0) {
                                        // Replace filled bucket with an empty bucket
                                        if (!player.getAbilities().instabuild) {
                                            stack.shrink(1);
                                            ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                                            if (!player.addItem(emptyBucket)) {
                                                player.drop(emptyBucket, false);
                                            }
                                        }
                                        var sound = bucketItem.content.getFluidType().getSound(player, level, pos, SoundActions.BUCKET_EMPTY);
                                        var soundSource = SoundSource.BLOCKS;
                                        if (sound != null) {
                                            level.playSound(player, pos, sound, soundSource, 1.0F, 1.0F);
                                        }
                                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
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
                                        // Replace empty bucket with a filled bucket
                                        if (!player.getAbilities().instabuild) {
                                            stack.shrink(1);
                                            ItemStack filledBucket = new ItemStack(fluidToExtract.getBucket());
                                            ItemHandlerHelper.giveItemToPlayer(player, filledBucket);
                                        }
                                        var sound = fluidToExtract.getFluidType().getSound(player, level, pos, SoundActions.BUCKET_FILL);
                                        var soundSource = SoundSource.BLOCKS;
                                        if (sound != null) {
                                            level.playSound(player, pos, sound, soundSource, 1.0F, 1.0F);
                                        }
                                        return ItemInteractionResult.sidedSuccess(level.isClientSide());
                                    }
                                }
                            }
                }
    
                IFluidHandler cap = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, blockHitResult.getDirection());
                if (cap == null) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
    
                FluidStack itemFluid = fluidHandlerItem.getFluidInTank(0);
                FluidStack tankFluid = cap.getFluidInTank(0);
    
                if (tankFluid.getAmount() > itemFluid.getAmount()) {
                    FluidStack drainTest = cap.drain(fluidHandlerItem.getTankCapacity(0), IFluidHandler.FluidAction.SIMULATE);
                    if (drainTest.getAmount() > 0) {
                        int fillAmount = fluidHandlerItem.fill(drainTest, IFluidHandler.FluidAction.SIMULATE);
                        if (fillAmount > 0) {
                            FluidStack drained = cap.drain(fillAmount, IFluidHandler.FluidAction.EXECUTE);
                            fluidHandlerItem.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                            return ItemInteractionResult.sidedSuccess(level.isClientSide());
                        }
                    }
                } else {
                    int fillAmount = cap.fill(itemFluid, IFluidHandler.FluidAction.SIMULATE);
                    if (fillAmount > 0) {
                        FluidStack drained = fluidHandlerItem.drain(fillAmount, IFluidHandler.FluidAction.EXECUTE);
                        if (!drained.isEmpty()) {
                            cap.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                            return ItemInteractionResult.sidedSuccess(level.isClientSide());
                        }
                    }
                }
            }
        return ItemInteractionResult.SUCCESS;
    }
    
    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack itemStack = new ItemStack(state.getBlock());
        if (be instanceof FluidTankBlockEntity fluidTankBE && fluidTankBE.getFluidHandler().getFluidAmount() != 0) {
            be.saveToItem(itemStack, params.getLevel().registryAccess());
        }
        return Collections.singletonList(itemStack);
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data != null) {
                var tag = data.copyTag().getCompound("modernutils");
                var fluid = tag.getCompound("fluidTank").getCompound("Fluid");
                FluidStack fluidStack = FluidStack.parseOptional(context.level().registryAccess(),fluid);
                tooltipComponents.add(Component.literal("Fluid: " + fluidStack.getFluid()));
                tooltipComponents.add(Component.literal("Amount: " + fluidStack.getAmount()));
            }
        }
    }
}
