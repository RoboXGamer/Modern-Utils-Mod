package net.roboxgamer.tutorialmod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.roboxgamer.tutorialmod.TutorialMod;
import org.jetbrains.annotations.NotNull;

public class ExtendedButton extends AbstractWidget {
  
  private final String name;
  private final Button.OnPress onPress;
  private final WidgetPosition position;
  private final boolean icon;
  private Button button;
  
  // Enum for predefined positions
  public enum WidgetPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
  }
  
  // Constructor
  public ExtendedButton(String name, int width, int height, Component text, boolean icon, WidgetPosition position, Button.OnPress onPress) {
    super(0, 0, width, height, text);  // Super constructor for AbstractWidget
    this.name = name;
    this.onPress = onPress;
    this.position = position;
    this.icon = icon;
    
    // Set the position of the button based on enum
    this.setPosition();
  }
  
  // Set button position based on predefined enum
  private void setPosition() {
    Minecraft mc = Minecraft.getInstance();
    if (mc.screen instanceof AbstractContainerScreen<?> containerScreen) {
      int guiLeft = containerScreen.getGuiLeft(); // left x-coordinate of the GUI
      int guiTop = containerScreen.getGuiTop();   // top y-coordinate of the GUI
      int guiWidth = containerScreen.getXSize();  // width of the GUI texture
      int guiHeight = containerScreen.getYSize(); // height of the GUI texture
      
      switch (this.position) {
        case TOP_LEFT:
          this.setX(guiLeft - this.width);
          this.setY(guiTop);
          break;
        case TOP_RIGHT:
          this.setX(guiLeft + guiWidth);
          this.setY(guiTop);
          break;
        case BOTTOM_LEFT:
          this.setX(guiLeft - this.width);
          this.setY(guiTop + guiHeight - this.height);
          break;
        case BOTTOM_RIGHT:
          this.setX(guiLeft + guiWidth);
          this.setY(guiTop + guiHeight - this.height);
          break;
      }
    }
  }
  
  // Log button click and execute the onPress action
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (this.isHovered()) {
      TutorialMod.LOGGER.debug("Button clicked: {}", this.name);
      this.button = Button.builder(Component.nullToEmpty(this.name), this.onPress).size(this.width, this.height).build();
      this.onPress.onPress(this.button);
      return true;
    }
    return false;
  }
  
  public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    if (this.button != null) {
      guiGraphics.fill(this.getX(), this.getY(), this.getX() + button.getWidth(), this.getY() + button.getHeight(),
                       0xFFFFFFF);
      return;
    }
    guiGraphics.fill(this.getX(),this.getY(),this.getX() + this.width,this.getY() + this.height,
                     0xFFFFFFF);
  }
  
  public void renderText(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xFFFFFF);
  }
  
  @Override
  protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    // draw a background of semi black
    this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    if (icon){
      this.renderIcon(guiGraphics,mouseX,mouseY,partialTick);
    } else {
      this.renderText(guiGraphics, mouseX, mouseY, partialTick);
    }
  }
  
  public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}
  
  @Override
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
