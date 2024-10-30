package net.roboxgamer.modernutils.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.roboxgamer.modernutils.ModernUtilsMod;
import org.jetbrains.annotations.NotNull;

public record RedstoneModePayload(int mode, BlockPos blockPos) implements CustomPacketPayload {
  public static final Type<RedstoneModePayload> TYPE = new CustomPacketPayload.Type<>(
      ModernUtilsMod.location("redstone_mode")
  );
  public static final StreamCodec<ByteBuf, RedstoneModePayload> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      RedstoneModePayload::mode,
      BlockPos.STREAM_CODEC,
      RedstoneModePayload::blockPos,
      RedstoneModePayload::new
  );
  
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}