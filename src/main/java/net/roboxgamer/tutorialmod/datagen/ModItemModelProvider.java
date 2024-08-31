package net.roboxgamer.tutorialmod.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
  public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, TutorialMod.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    basicItem(ModItems.EXAMPLE_ITEM.get());
  }
}