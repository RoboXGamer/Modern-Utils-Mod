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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalCrafterBlock extends Block implements EntityBlock {
  public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
  
  public MechanicalCrafterBlock(Properties properties) {
    super(properties);
    registerDefaultState(stateDefinition.any()
                             .setValue(POWERED, false)
    );
  }
  
  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(POWERED);
  }
  
  public boolean isPowered(BlockState state) {
    return state.getValue(POWERED);
  }
  
  public BlockState setPowered(BlockState state, boolean powered) {
    return state.setValue(POWERED, powered);
  }
  
  @Override
  protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
    super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    boolean powered = level.hasNeighborSignal(pos);
    BlockState newState = setPowered(state, powered);
    // Notify the client and update the state without replacing the block entity
    level.setBlock(pos, newState, Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS); // Only update clients and neighbors
    
    // Force the block entity to retain its contents
    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity != null) {
      blockEntity.setChanged(); // Mark the block entity as changed, ensuring it is not invalidated
    }
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
  protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
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
