package net.roboxgamer.modernutils.util;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.roboxgamer.modernutils.ModernUtilsMod;

public class ModTags {
  public static class Blocks {

    private static TagKey<Block> createTag(String name) {
      return BlockTags.create(ModernUtilsMod.location(name));
    }
  }

  public static class Items {

    private static TagKey<Item> createTag(String name) {
      return ItemTags.create(ModernUtilsMod.location(name));
    }
  }
}