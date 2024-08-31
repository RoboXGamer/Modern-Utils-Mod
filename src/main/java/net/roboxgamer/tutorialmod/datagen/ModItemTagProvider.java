package net.roboxgamer.tutorialmod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
  public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
                            CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
    super(pOutput, pLookupProvider, pBlockTags, TutorialMod.MODID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.Provider pProvider) {
//    ModTags.Items.
  }
}
