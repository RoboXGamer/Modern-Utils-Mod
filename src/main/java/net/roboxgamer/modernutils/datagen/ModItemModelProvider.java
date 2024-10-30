package net.roboxgamer.modernutils.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
  public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, ModernUtilsMod.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    basicItem(ModItems.EXAMPLE_ITEM.get());
  }
}