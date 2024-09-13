package net.roboxgamer.tutorialmod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;

public class ClientPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    var remainItemToggleValue = payload.remainItemToggleValue();
    //TutorialMod.LOGGER.debug("Client received remainItemToggleValue: {}", remainItemToggleValue);
  }
  
  public static void handleData(ItemStackPayload payload, final IPayloadContext context) {
    ItemStack itemStack = payload.itemStack();
    //TutorialMod.LOGGER.debug("Client received itemStack: {}", itemStack);
    BlockPos blockPos = payload.blockPos();
    //TutorialMod.LOGGER.debug("Client received blockPos: {}", blockPos);
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.setRenderStack(itemStack);
    }
  }
  
  public static void handleData(GhostSlotTransferPayload payload, final IPayloadContext context) {
    ItemStack itemStack = payload.itemStack();
    //TutorialMod.LOGGER.debug("Client received itemStack: {}", itemStack);
    BlockPos blockPos = payload.blockPos();
    //TutorialMod.LOGGER.debug("Client received blockPos: {}", blockPos);
  }
}