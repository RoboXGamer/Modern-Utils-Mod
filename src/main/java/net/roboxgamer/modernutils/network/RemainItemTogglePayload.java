package net.roboxgamer.modernutils.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.roboxgamer.modernutils.ModernUtilsMod;
import org.jetbrains.annotations.NotNull;

public record RemainItemTogglePayload(int remainItemToggleValue, BlockPos blockPos) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<RemainItemTogglePayload> TYPE = new CustomPacketPayload.Type<>(
      ModernUtilsMod.location("remain_item_toggle"));
  
  
  public static final StreamCodec<ByteBuf, RemainItemTogglePayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      RemainItemTogglePayload::remainItemToggleValue,
      BlockPos.STREAM_CODEC,
      RemainItemTogglePayload::blockPos,
      RemainItemTogglePayload::new
  );
  
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}