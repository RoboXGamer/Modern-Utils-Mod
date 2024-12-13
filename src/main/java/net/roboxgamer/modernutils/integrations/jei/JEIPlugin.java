package net.roboxgamer.modernutils.integrations.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.client.screen.MechanicalCrafterScreen;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final ResourceLocation UID = ModernUtilsMod.location("jei_plugin");
  
  public JEIPlugin() {}
  
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
        new MechanicalCrafterRecipeTransferHandler(registration.getTransferHelper())
        ,RecipeTypes.CRAFTING);
  }
  
  @Override
  public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
    registration.addGhostIngredientHandler(
        MechanicalCrafterScreen.class,MechanicalCrafterRecipeTransferHandler.GHOST_INGREDIENT_HANDLER
    );
  }
}
