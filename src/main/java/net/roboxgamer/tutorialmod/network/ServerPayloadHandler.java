package net.roboxgamer.tutorialmod.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;

public class ServerPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    var remainItemToggleValue = payload.remainItemToggleValue();
    var blockPos = payload.blockPos();
    System.out.println("Server received value: " + remainItemToggleValue);
    var be = context.player().level().getBlockEntity(blockPos);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      mcbe.setRemainItemToggleValue(remainItemToggleValue);
    }
  }
}
