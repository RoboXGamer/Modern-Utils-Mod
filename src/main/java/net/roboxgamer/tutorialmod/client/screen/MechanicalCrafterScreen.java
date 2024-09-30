package net.roboxgamer.tutorialmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import net.roboxgamer.tutorialmod.network.RedstoneModePayload;
import net.roboxgamer.tutorialmod.network.RemainItemTogglePayload;
import net.roboxgamer.tutorialmod.util.RedstoneManager;
import org.jetbrains.annotations.NotNull;

import static net.roboxgamer.tutorialmod.util.RedstoneManager.REDSTONE_MODE_MAP;


public class MechanicalCrafterScreen extends AbstractContainerScreen<MechanicalCrafterMenu> {
  private static final String location =
      TutorialMod.MODID + ".mechanical_crafter_screen";
  private static final Component TITLE =
      Component.translatable("gui." + location);
  private static final Component BUTTON_TEXT =
      Component.translatable("gui." + location + ".button_text");
  
  private static final ResourceLocation TEXTURE =
      TutorialMod.location("textures/gui/mechanical_crafter_screen.png");
  
  private static final ResourceLocation[] REDSTONE_MODE_TEXTURES = {
      TutorialMod.location("redstone_mode_0"),
      TutorialMod.location("redstone_mode_1"),
      TutorialMod.location("redstone_mode_2")
  };
  
  private final BlockPos position;
  private final int imageWidth, imageHeight;
  
  private MechanicalCrafterBlockEntity blockEntity;
  private RedstoneManager redstoneManager;
  //private int leftPos, topPos;
  
  //Widgets
  private Button button;
  private ImageButton redstoneModeButton;
  
  
  public MechanicalCrafterScreen(MechanicalCrafterMenu menu, Inventory playerInv, Component title) {
    super(menu,playerInv,title);
    this.position = menu.getBlockEntity().getBlockPos();
    this.imageWidth = 176;
    this.imageHeight = 236;
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
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      this.blockEntity = mcbe;
      this.redstoneManager = this.blockEntity.getRedstoneManager();
      
    } else {
      TutorialMod.LOGGER.error("Mechanical Crafter Screen: BlockEntity is not a MechanicalCrafterBlockEntity!");
      return;
    }
    
    //  Widgets to add!
    
    this.button = addRenderableWidget(
        new ImageButton(this.leftPos + this.imageWidth - 56,this.topPos + 34,20,20,
                        new WidgetSprites(
            TutorialMod.location("toggle_remain_btn"),
            TutorialMod.location("toggle_remain_btn_disabled"),
            TutorialMod.location("toggle_remain_btn_highlighted")
    ),this::handleButtonClick,BUTTON_TEXT)
            );
    var remainItemToggleValue = this.blockEntity.getRemainItemToggleDisplayValue();
    this.button.setTooltip(
        Tooltip.create(
            Component.literal(
                String.format("Remaining Items [%s]", remainItemToggleValue)
            )
        )
    );
    
    this.redstoneModeButton = addRenderableWidget(
        new ImageButton(this.leftPos + this.imageWidth - 56,this.topPos + 64,20,20,
                        new WidgetSprites(
                            TutorialMod.location("redstone_mode_btn"),
                            TutorialMod.location("redstone_mode_btn_disabled"),
                            TutorialMod.location("redstone_mode_btn_highlighted")
                        ),this::handleRedstoneModeButtonClick,BUTTON_TEXT){
          @Override
          public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            var sprites = getRedstoneButtonSprites();
            var resourcelocation = sprites.get(this.isActive(), this.isHoveredOrFocused());
            guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
          }
        }
    );
    updateRedstoneButtonTooltip();
  }
  
  private WidgetSprites getRedstoneButtonSprites() {
    int mode = this.redstoneManager.getRedstoneMode().ordinal();
    return new WidgetSprites(
        REDSTONE_MODE_TEXTURES[mode],
        REDSTONE_MODE_TEXTURES[mode], // You might want different textures for disabled/highlighted states
        REDSTONE_MODE_TEXTURES[mode]
    );
  }
  
  private void handleRedstoneModeButtonClick(Button button) {
    RedstoneManager.RedstoneMode value = this.redstoneManager.getNextRedstoneMode();
    this.blockEntity.getRedstoneManager().setRedstoneMode(value);
    TutorialMod.LOGGER.debug("Toggled redstoneModeValue to {}", value);
    PacketDistributor.sendToServer(new RedstoneModePayload(value.ordinal(), this.blockEntity.getBlockPos()));
    updateRedstoneButtonTooltip();
  }
  
  private void updateRedstoneButtonTooltip() {
    RedstoneManager.RedstoneMode redstoneModeValue = this.redstoneManager.getRedstoneMode();
    this.redstoneModeButton.setTooltip(Tooltip.create(Component.literal(
        String.format("Redstone Mode [%s]", REDSTONE_MODE_MAP.get(redstoneModeValue.ordinal()))
    )));
  }
  
  private void handleButtonClick(Button button) {
    var value = this.blockEntity.toggleRemainItemValue();
    TutorialMod.LOGGER.debug("Toggled remainItemToggleValue to {}", value);
    PacketDistributor.sendToServer(new RemainItemTogglePayload(value, this.blockEntity.getBlockPos()));
    var remainItemToggleValue = this.blockEntity.getRemainItemToggleDisplayValue();
    this.button.setTooltip(
        Tooltip.create(
            Component.literal(
                String.format("Remaining Items [%s]", remainItemToggleValue)
            )
        )
    );
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
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }
}
