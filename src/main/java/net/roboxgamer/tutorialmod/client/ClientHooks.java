package net.roboxgamer.tutorialmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.roboxgamer.tutorialmod.client.screen.MechanicalCrafterScreen;

public class ClientHooks {
  public static void openMechanicalCrafterScreen(BlockPos pos){
    //Minecraft.getInstance().setScreen(new MechanicalCrafterScreen(pos));
  }
}
