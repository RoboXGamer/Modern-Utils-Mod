package net.roboxgamer.modernutils.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.ClickAction;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.util.Constants;
import org.jetbrains.annotations.NotNull;

public record SideStatePayload(Constants.@NotNull Sides side, ClickAction clickAction, BlockPos blockPos) implements CustomPacketPayload {
  public static final Type<SideStatePayload> TYPE = new CustomPacketPayload.Type<>(
      ModernUtilsMod.location("side_state_payload")
  );
  public static final StreamCodec<FriendlyByteBuf, SideStatePayload> STREAM_CODEC = StreamCodec.composite(
      NeoForgeStreamCodecs.enumCodec(Constants.Sides.class),
      SideStatePayload::side,
      NeoForgeStreamCodecs.enumCodec(ClickAction.class),
      SideStatePayload::clickAction,
      BlockPos.STREAM_CODEC,
      SideStatePayload::blockPos,
      SideStatePayload::new
  );
  
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
