package net.roboxgamer.modernutils.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;

public class ClientPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    var remainItemToggleValue = payload.remainItemToggleValue();
    //ModernUtils.LOGGER.debug("Client received remainItemToggleValue: {}", remainItemToggleValue);
  }
  
  public static void handleData(ItemStackPayload payload, final IPayloadContext context) {
    ItemStack itemStack = payload.itemStack();
    //ModernUtils.LOGGER.debug("Client received itemStack: {}", itemStack);
    BlockPos blockPos = payload.blockPos();
    //ModernUtils.LOGGER.debug("Client received blockPos: {}", blockPos);
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.setRenderStack(itemStack);
    }
  }
  
  public static void handleData(GhostSlotTransferPayload payload, final IPayloadContext context) {
    ItemStack itemStack = payload.itemStack();
    //ModernUtils.LOGGER.debug("Client received itemStack: {}", itemStack);
    BlockPos blockPos = payload.blockPos();
    //ModernUtils.LOGGER.debug("Client received blockPos: {}", blockPos);
  }
  
  public static void handleData(RedstoneModePayload payload, final IPayloadContext context) {
    int mode = payload.mode();
    //ModernUtils.LOGGER.debug("Client received redstoneMode: {}", mode);
  }
  
  public static void handleData(SlotStatePayload payload, final IPayloadContext context) {
    boolean state = payload.slotState();
    //ModernUtils.LOGGER.debug("Client received redstoneMode: {}", mode);
  }
  
  public static void handleData(SideStatePayload payload, final IPayloadContext context) {
    //ModernUtilsMod.LOGGER.debug("Client received sideStatePayload: {}", payload);
    var blockEntity = context.player().level().getBlockEntity(payload.blockPos());
    if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
      be.setSideBtnState(payload.side(),payload.sideState());
    }
  }
}
