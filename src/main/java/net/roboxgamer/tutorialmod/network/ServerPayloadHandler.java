package net.roboxgamer.tutorialmod.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;

import static net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity.REDSTONE_MODE_MAP;

public class ServerPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    var remainItemToggleValue = payload.remainItemToggleValue();
    var blockPos = payload.blockPos();
    //TutorialMod.LOGGER.debug("Server received value: {}", remainItemToggleValue);
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.setRemainItemToggleValue(remainItemToggleValue);
    }
  }
  
  public static void handleData(ItemStackPayload payload, final IPayloadContext context) {
    var itemStack = payload.itemStack();
    var blockPos = payload.blockPos();
    //TutorialMod.LOGGER.debug("Server received itemStack: {}", itemStack);
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.getCraftingSlotsItemHandler().setStackInSlot(0, itemStack);
    }
  }
  
  public static void handleData(GhostSlotTransferPayload payload, final IPayloadContext context) {
    var slotIndex = payload.slotIndex();
    var itemStack = payload.itemStack();
    var blockPos = payload.blockPos();
    //TutorialMod.LOGGER.debug("Server received itemStack: {}", itemStack);
    var blockEntity = context.player().level().getBlockEntity(blockPos);
    if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
      be.getCraftingSlotsItemHandler().setStackInSlot(slotIndex, itemStack);
    }
  }
  
  public static void handleData(RedstoneModePayload payload, final IPayloadContext context) {
    var mode = payload.mode();
    var blockPos = payload.blockPos();
    TutorialMod.LOGGER.debug("Server received redstoneMode: {}", mode);
    var blockEntity = context.player().level().getBlockEntity(blockPos);
    if (blockEntity instanceof MechanicalCrafterBlockEntity be) {
      be.setRedstoneMode(REDSTONE_MODE_MAP.get(mode));
    }
  }
}
