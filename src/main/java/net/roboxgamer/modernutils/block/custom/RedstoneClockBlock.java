package net.roboxgamer.modernutils.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.RedstoneClockBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedstoneClockBlock extends Block implements EntityBlock {
    public static BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static IntegerProperty POWER = BlockStateProperties.POWER;
    public RedstoneClockBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(POWER,0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        builder.add(POWER);
    }
    
    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
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
        return new RedstoneClockBlockEntity(pos, state);
    }
    
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : ((level1, pos, state, blockEntity) -> ((RedstoneClockBlockEntity) blockEntity).tick());
    }
    
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        var hand = player.getUsedItemHand();
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RedstoneClockBlockEntity blockEntity)) return InteractionResult.PASS;
        // When we right-click on the block, we toggle the redstone mode to the next mode
        blockEntity.getRedstoneManager().setRedstoneMode(blockEntity.getRedstoneManager().getNextRedstoneMode());
        return InteractionResult.CONSUME;
    }
    
    @Override
    protected int getSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        //return level.getBlockEntity(pos, ModBlockEntities.REDSTONE_CLOCK_BE.get()).orElseThrow().getOutputSignal();
        return state.getValue(POWER);
    }
}
