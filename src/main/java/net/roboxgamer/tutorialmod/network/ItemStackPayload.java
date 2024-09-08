package net.roboxgamer.tutorialmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.roboxgamer.tutorialmod.TutorialMod;
import org.jetbrains.annotations.NotNull;

public record ItemStackPayload (ItemStack itemStack, BlockPos blockPos) implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<ItemStackPayload> TYPE = new CustomPacketPayload.Type<>(
      TutorialMod.location("item_stack"));
  
  
  public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackPayload> STREAM_CODEC = StreamCodec.composite(
      ItemStack.STREAM_CODEC,
      ItemStackPayload::itemStack,
      BlockPos.STREAM_CODEC,
      ItemStackPayload::blockPos,
      ItemStackPayload::new
  );
  
  @Override
  public @NotNull Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}