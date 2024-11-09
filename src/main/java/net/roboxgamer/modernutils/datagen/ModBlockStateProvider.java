package net.roboxgamer.modernutils.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
  public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
    super(output, ModernUtilsMod.MODID, exFileHelper);
  }
  @Override
  protected void registerStatesAndModels() {
    blockWithItem(ModBlocks.EXAMPLE_BLOCK);
    blockWithItem(ModBlocks.MAGIC_BLOCK);
    blockWithItem(ModBlocks.EXAMPLE_INVENTORY_BLOCK);
    blockWithItem(ModBlocks.BATTERY_BLOCK);
    
    Block block = ModBlocks.MINI_CHEST_BLOCK.get();
    ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/mini_chest_block"));
    horizontalBlock(block, model);
    simpleBlockItem(block,model);
    
    blockWithItemWithModel(
        ModBlocks.MECHANICAL_CRAFTER_BLOCK,
        new ModelFile.UncheckedModelFile(modLoc("block/mechanical_crafter_block"))
    );
  }
  private <T extends Block> void blockWithItem(DeferredBlock<T> block) {
    simpleBlockWithItem(block.get(), cubeAll(block.get()));
  }
  
  private <T extends Block> void blockWithItemWithModel(DeferredBlock<T> block, ModelFile model) {
    simpleBlockWithItem(block.get(), model);
  }
  
  
  // This is an automatic method to register all simple blocks with item that are in ModBlocks.BLOCKS
  //@Override
  //protected void registerStatesAndModels() {
  //  ModBlocks.BLOCKS.getEntries().forEach(block -> simpleBlockWithItem(block.get(), cubeAll(block.get())));
  //}
  
}
