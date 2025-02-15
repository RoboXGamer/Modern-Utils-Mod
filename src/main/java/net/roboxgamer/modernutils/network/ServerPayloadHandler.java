package net.roboxgamer.modernutils.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.MagicBlockBlockEntity;
import net.roboxgamer.modernutils.util.Constants;

import static net.roboxgamer.modernutils.util.RedstoneManager.REDSTONE_MODE_MAP;

public class ServerPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    var remainItemToggleValue = payload.remainItemToggleValue();
    var blockPos = payload.blockPos();
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.setRemainItemToggleValue(remainItemToggleValue);
    }
  }
  
  public static void handleData(ItemStackPayload payload, final IPayloadContext context) {
    var itemStack = payload.itemStack();
    var blockPos = payload.blockPos();
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      ItemStack renderStack = mcbe.getRenderStack();
      if (renderStack.isEmpty()) {
        PacketDistributor.sendToAllPlayers(new ItemStackPayload(itemStack, blockPos));
      }
    }
  }
  
  public static void handleData(GhostSlotTransferPayload payload, final IPayloadContext context) {
    var slotIndex = payload.slotIndex();
    var itemStack = payload.itemStack();
    var blockPos = payload.blockPos();
    var blockEntity = context.player().level().getBlockEntity(blockPos);
    if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
      be.getCraftingSlotsItemHandler().setStackInSlot(slotIndex, itemStack);
    }
  }
  
  public static void handleData(RedstoneModePayload payload, final IPayloadContext context) {
    var mode = payload.mode();
    var blockPos = payload.blockPos();
    ModernUtilsMod.LOGGER.debug("Server received redstoneMode: {}", mode);
    var blockEntity = context.player().level().getBlockEntity(blockPos);
    if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
      be.getRedstoneManager().setRedstoneMode(REDSTONE_MODE_MAP.get(mode));
    }
  }
  
  public static void handleData(SlotStatePayload payload,final IPayloadContext context){
    var state = payload.slotState();
    var slotIndex = payload.slotIndex();
    var blockPos = payload.blockPos();
    //   SPECIAL CASE: Slot -1 is for auto export enabling/disabling
    //   SPECIAL CASE: Slot -2 is for auto import enabling/disabling
    switch (slotIndex) {
        case -1 -> {
          var blockEntity = context.player().level().getBlockEntity(blockPos);
          if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
            be.getSideManager().setAutoExportEnabled(state);
          }
        }
        case -2 -> {
          var blockEntity = context.player().level().getBlockEntity(blockPos);
          if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
            be.getSideManager().setAutoImportEnabled(state);
          }
        }
        default -> {
          ModernUtilsMod.LOGGER.debug("Server received slotState: {}", state);
          var blockEntity = context.player().level().getBlockEntity(blockPos);
          if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
            be.setSlotState(slotIndex, state ? 0 : 1);
          }
        }
      }
  }
  
  public static void handleData(SideStatePayload payload, final IPayloadContext context) {
  //  
  }

  public static void handleData(MagicBlockSettingsUpdatePayload payload, final IPayloadContext context) {
    var blockPos = payload.blockPos();
    var blockEntity = context.player().level().getBlockEntity(blockPos);
    if (!(blockEntity instanceof MagicBlockBlockEntity be)) return;

    payload.speed().ifPresent(be::setSpeed);
    payload.offsetX().ifPresent(be::setOffsetX);
    payload.offsetY().ifPresent(be::setOffsetY);
    payload.offsetZ().ifPresent(be::setOffsetZ);
    payload.renderOutline().ifPresent(be::setRenderOutline);
    
    be.setChanged();
  }
}
