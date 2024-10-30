package net.roboxgamer.modernutils.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.roboxgamer.modernutils.ModernUtilsMod;
import org.jetbrains.annotations.NotNull;

public record GhostSlotTransferPayload(int slotIndex, ItemStack itemStack, BlockPos blockPos) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<GhostSlotTransferPayload> TYPE = new CustomPacketPayload.Type<>(
      ModernUtilsMod.location("ghost_slot_transfer"));
  
  public static final StreamCodec<RegistryFriendlyByteBuf, GhostSlotTransferPayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      GhostSlotTransferPayload::slotIndex,
      ItemStack.OPTIONAL_STREAM_CODEC,
      GhostSlotTransferPayload::itemStack,
      BlockPos.STREAM_CODEC,
      GhostSlotTransferPayload::blockPos,
      GhostSlotTransferPayload::new
  );
  
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
