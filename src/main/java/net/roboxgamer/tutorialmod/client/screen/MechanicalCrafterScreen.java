package net.roboxgamer.tutorialmod.client.screen;

import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MechanicalCrafterScreen extends AbstractContainerScreen<MechanicalCrafterMenu> {
  private static final String location =
      TutorialMod.MODID + ".mechanical_crafter_screen";
  private static final Component TITLE =
      Component.translatable("gui." + location);
  private static final Component BUTTON_TEXT =
      Component.translatable("gui." + location + ".button_text");
  
  private static final ResourceLocation TEXTURE =
      ResourceLocation.fromNamespaceAndPath(TutorialMod.MODID, "textures/gui/mechanical_crafter_screen.png");
  
  //
  private final BlockPos position;
  private final int imageWidth, imageHeight;
  
  private MechanicalCrafterBlockEntity blockEntity;
  //private int leftPos, topPos;
  
  //Widgets
  private Button button;
  
  
  public MechanicalCrafterScreen(MechanicalCrafterMenu menu, Inventory playerInv, Component title) {
    super(menu,playerInv,title);
    this.position = menu.getBlockEntity().getBlockPos();
    this.imageWidth = 176;
    this.imageHeight = 235;
    this.inventoryLabelY = this.imageHeight - 92;
  }
  
  @Override
  public boolean isPauseScreen() {
    return false;
  }
  
  @Override
  protected void init() {
    super.init();
    
    // Already set in super
    //this.leftPos = this.width / 2 - this.imageWidth / 2;
    //this.topPos = this.height / 2 - this.imageHeight / 2;
    
    if (this.minecraft == null) return;
    Level level = this.minecraft.level;
    if (level == null) return;
    
    BlockEntity be = level.getBlockEntity(this.position);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      this.blockEntity = mcbe;
      
    } else {
      TutorialMod.LOGGER.error("Mechanical Crafter Screen: BlockEntity is not a MechanicalCrafterBlockEntity!");
      return;
    }
    
    //  Widgets to add!
    
    this.button = addRenderableWidget(
        Button.builder(
                BUTTON_TEXT,
                this::handleButtonClick
            )
            .bounds(this.leftPos + this.imageWidth - 56, this.topPos + 34, 50, 20)
            .tooltip(Tooltip.create(BUTTON_TEXT))
            .build()
    );
  }
  
  private void handleButtonClick(Button button) {
    TutorialMod.LOGGER.info("Button Clicked!");
    //  Button logic here
  }
  
  private void renderScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    guiGraphics.drawString(this.font, TITLE, this.leftPos + 8, this.topPos + 6, 0x404040, false);
  }
  
  private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    for (Renderable renderable : this.renderables) {
      renderable.render(guiGraphics, mouseX, mouseY, partialTick);
    }
  }
  
  private void renderMyLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.drawString(this.font, Component.literal("Input"), this.leftPos + 8, this.topPos + 80, 0x404040, false);
    guiGraphics.drawString(this.font, Component.literal("Output"), this.leftPos + 8, this.topPos + 112, 0x404040, false);
  }
  
  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    this.renderScreen(guiGraphics, mouseX, mouseY, partialTick);
  }
  
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);
    //renderTransparentBackground(guiGraphics);
    //this.renderScreen(guiGraphics, mouseX, mouseY, partialTick);
    //this.renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

    this.renderMyLabels(guiGraphics, mouseX, mouseY);

  }
  //public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
  //  super.render(guiGraphics, mouseX, mouseY, partialTick);
  //  renderBlurredBackground(partialTick);
  //  guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
  //  for (Renderable renderable : this.renderables) {
  //    renderable.render(guiGraphics, mouseX, mouseY, partialTick);
  //  }
  //}
  
  
  public @Nullable IGuiProperties getGuiProperties() {
    var minecraft = Minecraft.getInstance();
    return new IGuiProperties() {
      @Override
      public @NotNull Class<? extends Screen> screenClass() {
        return MechanicalCrafterScreen.class;
      }
      
      @Override
      public int guiLeft() {
        return leftPos;
      }
      
      @Override
      public int guiTop() {
        return topPos;
      }
      
      @Override
      public int guiXSize() {
        return imageWidth;
      }
      
      @Override
      public int guiYSize() {
        return imageHeight;
      }
      
      @Override
      public int screenWidth() {
        return minecraft.getWindow().getScreenWidth();
      }
      
      @Override
      public int screenHeight() {
        return minecraft.getWindow().getScreenHeight();
      }
    };
  }
}
