package net.roboxgamer.modernutils.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.ModBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
  public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
    super(output, lookupProvider, ModernUtilsMod.MODID, existingFileHelper);
  }

  @Override
  protected void addTags(HolderLookup.@NotNull Provider pProvider) {
    this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .add(ModBlocks.EXAMPLE_BLOCK.get())
        .add(ModBlocks.MAGIC_BLOCK.get())
        .add(ModBlocks.BATTERY_BLOCK.get())
        .add(ModBlocks.MECHANICAL_FURNACE_BLOCK.get());

    this.tag(BlockTags.MINEABLE_WITH_AXE)
        .add(ModBlocks.MECHANICAL_CRAFTER_BLOCK.get())
        .add(ModBlocks.EXAMPLE_INVENTORY_BLOCK.get())
        .add(ModBlocks.MINI_CHEST_BLOCK.get());
  }
}
