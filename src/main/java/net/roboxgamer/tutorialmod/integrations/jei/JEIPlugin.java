package net.roboxgamer.tutorialmod.integrations.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.ModBlocks;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import net.roboxgamer.tutorialmod.menu.ModMenuTypes;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final ResourceLocation UID = TutorialMod.location("jei_plugin");
  @Override
  public @NotNull ResourceLocation getPluginUid() {
    return UID;
  }
  
  @Override
  public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
    IModPlugin.super.registerRecipeCatalysts(registration);
    registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_CRAFTER_BLOCK.get()), new RecipeType[]{RecipeTypes.CRAFTING});
  }
  
  @Override
  public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
    IModPlugin.super.registerRecipeTransferHandlers(registration);
    registration.addRecipeTransferHandler(MechanicalCrafterMenu.class, ModMenuTypes.MECHANICAL_CRAFTER_MENU.get(),RecipeTypes.CRAFTING, 1, 9, 28, 36);
  }
}
