package net.roboxgamer.modernutils.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.custom.FluidTankBlock;
import net.roboxgamer.modernutils.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlockEntity extends BlockEntity {
    private final FluidTank fluidTank;
    
    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, FluidTankBlock.capacity);
    }
    
    public FluidTankBlockEntity(BlockPos pos, BlockState state, int capacity) {
        super(ModBlockEntities.FLUID_TANK_BE.get(), pos, state);
        this.fluidTank = new FluidTank(capacity);
    }
    
    private void readFluidTankNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt){
        this.fluidTank.readFromNBT(lookupProvider, nbt);
    }
    
    private CompoundTag writeFluidTankNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt){
        return this.fluidTank.writeToNBT(lookupProvider,nbt);
    }
    
    CompoundTag getBEData(HolderLookup.Provider registries) {
        CompoundTag beData = new CompoundTag();
        // Serialize the inventory
        beData.put("fluidTank", writeFluidTankNBT(registries, new CompoundTag()));
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
        // Deserialize the inventory
        readFluidTankNBT(registries, tag.getCompound("fluidTank"));
    }
    
    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return getBEData(registries);
    }
    
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    // Saving to the block item
    
    
    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
    
    }
    
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
    
    }
    
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
    }
    
    // Public methods
    public FluidTank getFluidHandler() {
        return this.fluidTank;
    }
}
