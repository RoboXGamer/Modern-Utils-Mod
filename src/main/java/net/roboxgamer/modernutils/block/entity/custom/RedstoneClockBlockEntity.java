package net.roboxgamer.modernutils.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.custom.RedstoneClockBlock;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.RedstoneManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedstoneClockBlockEntity extends BlockEntity implements Constants.IRedstoneConfigurable {
    private final RedstoneManager redstoneManager;
    private int outputSignal = 0;
    
    private int tc = 0;
    
    public RedstoneClockBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.REDSTONE_CLOCK_BE.get(), pos, blockState);
        this.redstoneManager = new RedstoneManager(this);
    }
    
    private boolean everySecond(double seconds) {
        return this.tc % (20 * seconds) == 0;
    }
    
    
    public void tick() {
        this.tc++;
        if (everySecond(60))
            this.tc = 0; // Every 1 minute
        if (this.level == null || this.level.isClientSide() || !(this.level instanceof ServerLevel slevel))
            return;
        
        // Server-side logic
        
        // Redstone control logic
        boolean powered = level.hasNeighborSignal(this.getBlockPos());
        
        ModernUtilsMod.LOGGER.info("Redstone Mode: {}", this.redstoneManager.getRedstoneMode());
        switch (this.redstoneManager.getRedstoneMode()) {
            case ALWAYS_ON:
                break; // No additional check, always on
            
            case REDSTONE_ON:
                if (!powered)
                    return; // Only if receiving redstone power
                break;
            
            case REDSTONE_OFF:
                if (powered)
                    return; // Stop if receiving redstone power
                break;
            case PULSE:
                // TODO: Implement pulse mode
                return;
        }
        
    //    Give out redstone power 15 every second in always on mode
        if (this.redstoneManager.getRedstoneMode() == RedstoneManager.RedstoneMode.ALWAYS_ON && everySecond(10)) {
            this.outputSignal = this.outputSignal == 0 ? 15 : 0;
            setChanged();
            BlockState state = this.getBlockState().setValue(RedstoneClockBlock.POWER, this.outputSignal);
            this.level.setBlockAndUpdate(this.getBlockPos(),state);
        }
        ModernUtilsMod.LOGGER.info("Output Signal: {}", this.outputSignal);
    }
    
    @Override
    public RedstoneManager getRedstoneManager() {
        return this.redstoneManager;
    }
    
    public int getOutputSignal() {
        return this.outputSignal;
    }
    
//    Saving and loading
CompoundTag getBEData(HolderLookup.Provider registries) {
    CompoundTag beData = new CompoundTag();
    this.redstoneManager.saveToTag(beData);
    beData.putInt("outputSignal", this.outputSignal);
    return beData;
}
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag modData = getBEData(registries);
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
        this.redstoneManager.loadFromTag(tag);
        this.outputSignal = tag.getInt("outputSignal");
    }
    
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return getBEData(registries);
    }
    
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
