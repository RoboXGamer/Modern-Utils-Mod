package net.roboxgamer.modernutils.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import net.roboxgamer.modernutils.menu.MagicBlockMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagicBlockBlockEntity extends BlockEntity implements MenuProvider {
  private int tc = 0;
  private int speed = 2;
  private int offsetX = 0;
  private int offsetY = 1; // Default to above block
  private int offsetZ = 0;
  private boolean renderOutline = false;
  
  public MagicBlockBlockEntity(BlockPos pos, BlockState blockState) {
    super(ModBlockEntities.MAGIC_BLOCK_BE.get(), pos, blockState);
  }

  public void tick() {
    this.tc++;

    Level level = this.getLevel();
    BlockPos pos = this.getBlockPos();
    if (level == null)
      return;
    if (level.isClientSide() && getRenderOutline()) {
      level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
    }
    
    BlockPos targetPos = pos.offset(getOffsetX(), getOffsetY(), getOffsetZ());
    // Only prevent ticking if the target is itself (offset 0,0,0)
    if (targetPos.equals(pos)) {
      return;
    }

    BlockState state = level.getBlockState(targetPos);
    BlockEntity blockEntity = level.getBlockEntity(targetPos);

    for (int i = 0; i < getSpeed(); i++) {
      if (blockEntity != null) {
        // noinspection unchecked
        BlockEntityTicker<BlockEntity> ticker = state.getTicker(level,
            (BlockEntityType<BlockEntity>) blockEntity.getType());
        if (ticker != null) {
          ticker.tick(level, targetPos, state, blockEntity);
        }
      } else if (state.isRandomlyTicking()) {
        if (level instanceof ServerLevel serverLevel) {
          int randomTickSpeed = serverLevel.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
          if (randomTickSpeed > 0 && serverLevel.random.nextInt(randomTickSpeed) == 0) {
            state.randomTick(serverLevel, targetPos, serverLevel.random);
          }
        }
      }
    }
  }
  
  public boolean getRenderOutline() {
    return this.renderOutline;
  }
  
  public int incrementSpeed() {
    int speed = getSpeed();
    if (speed < 256)
      speed *= 2;
    this.speed = speed;
    ModernUtilsMod.LOGGER.debug("Speed is now {}", speed);
    setChanged();
    return speed;
  }

  public int decrementSpeed() {
    int speed = getSpeed();
    if (speed > 2)
      speed /= 2;
    this.speed = speed;
    ModernUtilsMod.LOGGER.debug("Speed is now {}", speed);
    setChanged();
    return speed;
  }

  public int getSpeed() {
    return this.speed;
  }

  public int getOffsetX() {
    return this.offsetX;
  }

  public int getOffsetY() {
    return this.offsetY;
  }

  public int getOffsetZ() {
    return this.offsetZ;
  }

  public void setOffsetX(int value) {
    this.offsetX = Math.min(16, Math.max(-16, value));
    setChanged();
  }

  public void setOffsetY(int value) {
    this.offsetY = Math.min(16, Math.max(-16, value));
    setChanged();
  }

  public void setOffsetZ(int value) {
    this.offsetZ = Math.min(16, Math.max(-16, value));
    setChanged();
  }
  
  public void setSpeed(int value) {
    this.speed = Math.min(256, Math.max(2, value));
    setChanged();
  }

  public BlockPos getTargetBlockPos() {
    return this.getBlockPos().offset(getOffsetX(), getOffsetY(), getOffsetZ());
  }

  public void setRenderOutline(boolean value) {
    this.renderOutline = value;
    setChanged();
  }
  
  CompoundTag getModData(HolderLookup.Provider registries) {
    CompoundTag modData = new CompoundTag();
    
    modData.putInt("speed", getSpeed());
    modData.putInt("offsetX", getOffsetX());
    modData.putInt("offsetY", getOffsetY());
    modData.putInt("offsetZ", getOffsetZ());
    modData.putBoolean("renderOutline", getRenderOutline());
    
    return modData;
  }
  @Override
  protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.saveAdditional(tag, registries);
    CompoundTag modData = getModData(registries);
    tag.put(ModernUtilsMod.MODID, modData);
  }
  
  @Override
  protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    super.loadAdditional(tag, registries);
    
    // Check if we are on the client side
    if (level != null && level.isClientSide()) {
      // Deserialize data from the tag for client-side
      deserializeFromTag(tag, registries);
    } else {
      CompoundTag modData = tag.getCompound(ModernUtilsMod.MODID);
      deserializeFromTag(modData, registries);
    }
  }
  private void deserializeFromTag(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
    setSpeed(tag.getInt("speed"));
    setOffsetX(tag.getInt("offsetX"));
    setOffsetY(tag.getInt("offsetY"));
    setOffsetZ(tag.getInt("offsetZ"));
    setRenderOutline(tag.getBoolean("renderOutline"));
  }
  
  @Override
  public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
    return getModData(registries);
  }
  
  @Override
  public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
  
  @Override
  public @NotNull Component getDisplayName() {
    return Component.translatable("block.modernutils.magic_block");
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
    return new MagicBlockMenu(containerId, inventory, this,null);
  }
}