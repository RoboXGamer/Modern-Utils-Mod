package net.roboxgamer.tutorialmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.roboxgamer.tutorialmod.TutorialMod;
import org.jetbrains.annotations.NotNull;

public record SlotStatePayload(int slotIndex, boolean slotState, BlockPos blockPos) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<SlotStatePayload> TYPE = new CustomPacketPayload.Type<>(
      TutorialMod.location("slot_state"));
  
  public static final StreamCodec<RegistryFriendlyByteBuf, SlotStatePayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      SlotStatePayload::slotIndex,
      ByteBufCodecs.BOOL,
      SlotStatePayload::slotState,
      BlockPos.STREAM_CODEC,
      SlotStatePayload::blockPos,
      SlotStatePayload::new
  );
  
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
