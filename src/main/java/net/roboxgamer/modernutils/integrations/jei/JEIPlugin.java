package net.roboxgamer.modernutils.integrations.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.client.screen.MechanicalCrafterScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final ResourceLocation UID = ModernUtilsMod.location("jei_plugin");

  public JEIPlugin() {
  }

  @Override
  public @NotNull ResourceLocation getPluginUid() {
    return UID;
  }

  @Override
  public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
    registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_CRAFTER_BLOCK.get()), RecipeTypes.CRAFTING);
  }

  @Override
  public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(
        new MechanicalCrafterRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
  }

  @Override
  public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(MechanicalCrafterScreen.class, new MechanicalCrafterGuiHandler());
    registration.addGhostIngredientHandler(
        MechanicalCrafterScreen.class, MechanicalCrafterRecipeTransferHandler.GHOST_INGREDIENT_HANDLER);
  }

  private static class MechanicalCrafterGuiHandler implements IGuiContainerHandler<MechanicalCrafterScreen> {
    @Override
    public List<Rect2i> getGuiExtraAreas(MechanicalCrafterScreen containerScreen) {
      List<Rect2i> areas = new ArrayList<>();

      // Only add the addon tab area if it's open
      if (containerScreen.AddonTab != null && containerScreen.AddonTab.isOpen()) {
        // Get the tab dimensions and position
        int tabX = containerScreen.getGuiLeft() + containerScreen.getXSize(); // Position at the right edge of the GUI
        int tabY = containerScreen.getGuiTop(); // Position at the top of the GUI
        int tabWidth = 46; // Width from your screen class
        int tabHeight = 68; // Height from your screen class

        areas.add(new Rect2i(tabX, tabY, tabWidth, tabHeight));
      }

      // Add the addon button area (always visible in top right)
      areas.add(new Rect2i(
          containerScreen.getGuiLeft() + containerScreen.getXSize(),
          containerScreen.getGuiTop(),
          24, 24));

      // Add side config button area (always visible in bottom left)
      areas.add(new Rect2i(
          containerScreen.getGuiLeft() - 24,
          containerScreen.getGuiTop() + containerScreen.getYSize() - 24,
          24, 24));

      // Add the side config tab area if it's open
      if (containerScreen.SideConfigTab != null && containerScreen.SideConfigTab.isOpen()) {
        areas.add(new Rect2i(
            containerScreen.getGuiLeft() - 92, // Width from screen class
            containerScreen.getGuiTop() + containerScreen.getYSize() - 92, // Height from screen class
            92,
            92));
      }

      return areas;
    }
  }
}
