package net.roboxgamer.modernutils.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.MagicBlockBlockEntity;
import net.roboxgamer.modernutils.util.Constants;

public class ClientPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    // Empty handler - no client-side action needed
  }
  
  public static void handleData(ItemStackPayload payload, final IPayloadContext context) {
    var be = context.player().level().getBlockEntity(payload.blockPos());
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.setRenderStack(payload.itemStack());
    }
  }
  
  public static void handleData(GhostSlotTransferPayload payload, final IPayloadContext context) {
    // Empty handler - no client-side action needed
  }
  
  public static void handleData(RedstoneModePayload payload, final IPayloadContext context) {
    // Empty handler - no client-side action needed
  }
  
  public static void handleData(SlotStatePayload payload, final IPayloadContext context) {
    // Empty handler - no client-side action needed
  }
  
  public static void handleData(SideStatePayload payload, final IPayloadContext context) {
    var blockEntity = context.player().level().getBlockEntity(payload.blockPos());
    if (blockEntity instanceof Constants.ISidedMachine sidedMachine) {
      sidedMachine.getSideManager().setSideBtnState(payload.side(), payload.sideState());
    }
  }

  public static void handleData(MagicBlockSettingsUpdatePayload payload, final IPayloadContext context) {
    var blockEntity = context.player().level().getBlockEntity(payload.blockPos());
    if (!(blockEntity instanceof MagicBlockBlockEntity be)) return;

    payload.speed().ifPresent(be::setSpeed);
    payload.offsetX().ifPresent(be::setOffsetX);
    payload.offsetY().ifPresent(be::setOffsetY);
    payload.offsetZ().ifPresent(be::setOffsetZ);
    payload.renderOutline().ifPresent(be::setRenderOutline);
  }
}
