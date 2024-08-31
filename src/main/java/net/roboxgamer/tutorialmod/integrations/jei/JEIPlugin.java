package net.roboxgamer.tutorialmod.integrations.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.roboxgamer.tutorialmod.client.screen.MechanicalCrafterScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("tutorialmod", "jei_plugin");
  @Override
  public @NotNull ResourceLocation getPluginUid() {
    return UID;
  }
}
