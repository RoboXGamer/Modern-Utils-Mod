package net.roboxgamer.tutorialmod.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
  public static void handleData(RemainItemTogglePayload payload, final IPayloadContext context) {
    var remainItemToggleValue = payload.remainItemToggleValue();
    System.out.println("Client received value: " + remainItemToggleValue);
  }
}
