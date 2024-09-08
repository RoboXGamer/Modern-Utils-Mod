package net.roboxgamer.tutorialmod.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;

public class ClientPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    var remainItemToggleValue = payload.remainItemToggleValue();
    System.out.println("Client received value: " + remainItemToggleValue);
  }
  
  public static void handleData(ItemStackPayload payload, final IPayloadContext context) {
    var itemStack = payload.itemStack();
    var blockPos = payload.blockPos();
    System.out.println("Client received itemStack: " + itemStack);
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.setRenderStack(itemStack);
    }
  }
}
