package net.roboxgamer.modernutils.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.roboxgamer.modernutils.ModernUtilsMod;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record MagicBlockSettingsUpdatePayload(BlockPos blockPos, Optional<Integer> speed,
        Optional<Integer> offsetX, Optional<Integer> offsetY, Optional<Integer> offsetZ,
        Optional<Boolean> renderOutline) implements CustomPacketPayload {
    
    public static final Type<MagicBlockSettingsUpdatePayload> TYPE = new CustomPacketPayload.Type<>(
            ModernUtilsMod.location("magic_block_settings"));

    public MagicBlockSettingsUpdatePayload {
        speed = Optional.ofNullable(speed).orElse(Optional.empty());
        offsetX = Optional.ofNullable(offsetX).orElse(Optional.empty());
        offsetY = Optional.ofNullable(offsetY).orElse(Optional.empty());
        offsetZ = Optional.ofNullable(offsetZ).orElse(Optional.empty());
        renderOutline = Optional.ofNullable(renderOutline).orElse(Optional.empty());
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, MagicBlockSettingsUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        MagicBlockSettingsUpdatePayload::blockPos,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
        MagicBlockSettingsUpdatePayload::speed,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
        MagicBlockSettingsUpdatePayload::offsetX,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
        MagicBlockSettingsUpdatePayload::offsetY,
        ByteBufCodecs.optional(ByteBufCodecs.VAR_INT),
        MagicBlockSettingsUpdatePayload::offsetZ,
        ByteBufCodecs.optional(ByteBufCodecs.BOOL),
        MagicBlockSettingsUpdatePayload::renderOutline,
        MagicBlockSettingsUpdatePayload::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}