package net.roboxgamer.modernutils.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.entity.custom.MiniChestBlockEntity;
import net.roboxgamer.modernutils.menu.MiniChestMenu;

import org.jetbrains.annotations.NotNull;

public class MiniChestScreen extends AbstractContainerScreen<MiniChestMenu> {
  private static final String location =
      ModernUtilsMod.MODID + ".mini_chest_screen";
  private static final Component TITLE =
      Component.translatable("gui." + location);
  
  private static final ResourceLocation TEXTURE =
      ModernUtilsMod.location("textures/gui/mini_chest_screen.png");
  
  private final BlockPos position;
  private final int imageWidth, imageHeight;
  
  private MiniChestBlockEntity blockEntity;
  //private int leftPos, topPos;
  
  public MiniChestScreen(MiniChestMenu menu, Inventory playerInv, Component title) {
    super(menu,playerInv,title);
    this.position = menu.getBlockEntity().getBlockPos();
    this.imageWidth = 176;
    this.imageHeight = 166;
    this.inventoryLabelY = this.imageHeight - 92;
  }
  
  @Override
  protected void init() {
    this.leftPos = this.width / 2 - this.imageWidth / 2;
    this.topPos = this.height / 2 - this.imageHeight / 2;
    
    if (this.minecraft == null) return;
    Level level = this.minecraft.level;
    if (level == null) return;
    
    BlockEntity be = level.getBlockEntity(this.position);
    if (be instanceof MiniChestBlockEntity mcbe) {
      this.blockEntity = mcbe;
    } else {
      ModernUtilsMod.LOGGER.error("Mechanical Crafter Screen: BlockEntity is not a MechanicalCrafterBlockEntity!");
    }
  }
  
  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
  }
  
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }
}
