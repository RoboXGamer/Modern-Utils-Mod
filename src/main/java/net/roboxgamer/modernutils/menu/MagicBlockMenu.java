package net.roboxgamer.modernutils.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.block.entity.custom.MagicBlockBlockEntity;
import org.jetbrains.annotations.NotNull;

public class MagicBlockMenu extends AbstractContainerMenu {
    public final MagicBlockBlockEntity blockEntity;

    // Client Constructor
    public MagicBlockMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()),
                null);
    }

    // Server Constructor
    public MagicBlockMenu(int containerId, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MAGIC_BLOCK_MENU.get(), containerId);
        checkContainerSize(inventory, 0);
        blockEntity = ((MagicBlockBlockEntity) entity);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.MAGIC_BLOCK.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}