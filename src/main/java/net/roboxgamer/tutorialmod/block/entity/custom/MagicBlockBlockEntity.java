package net.roboxgamer.tutorialmod.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.roboxgamer.tutorialmod.block.entity.ModBlockEntities;

public class MagicBlockBlockEntity extends BlockEntity {
  private int tc = 0;
  
  private int speed = 2;
  
  public MagicBlockBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.MAGIC_BLOCK_BE.get(), pos, blockState);
  }
  
  @Override
  protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.loadAdditional(tag, registries);
    this.speed = tag.getInt("speed");
  }
  
  @Override
  protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    super.saveAdditional(tag, registries);
    tag.putInt("speed", this.speed);
  }
  
  public void tick() {
    this.tc++;
    
    Level level = this.getLevel();
    BlockPos pos = this.getBlockPos();
    if (level == null) return;
    
    BlockState state = level.getBlockState(pos.above());
    BlockEntity blockEntity = level.getBlockEntity(pos.above());
    
    for (int i = 0; i < getSpeed(); i++) {
      if (blockEntity != null) {
//        noinspection unchecked
        BlockEntityTicker<BlockEntity> ticker =
            state.getTicker(level,(BlockEntityType<BlockEntity>) blockEntity.getType());
        if (ticker != null) {
          ticker.tick(level, pos.above(), state, blockEntity);
        }
      } else if (state.isRandomlyTicking()) {
        if (level instanceof ServerLevel serverLevel) {
          int randomTickSpeed = serverLevel.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
          if (randomTickSpeed > 0 && serverLevel.random.nextInt(randomTickSpeed) == 0) {
            state.randomTick(serverLevel, pos.above(), serverLevel.random);
          }
        }
      }
    }
  }
  
  
  private boolean everySecond() {
    return this.tc % 20 == 0;
  }
  
  public int incrementSpeed() {
    int speed = this.speed;
    if (speed < 256) speed *= 2;
    this.speed = speed;
    setChanged();
    return this.speed;
  }
  
  public int getSpeed() {
    return this.speed;
  }
}

