package net.roboxgamer.tutorialmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnimatedTab extends AbstractWidget {
  
  private boolean isOpen = false;
  private final ExtendedButton.WidgetPosition position;
  private float animatedWidth = 0;
  private float animatedHeight = 0;
  private final int targetWidth;
  private final int targetHeight;
  private final float animationSpeed = 0.2f;  // Adjust this value for faster/slower animations
  
  // Add a list for child widgets
  private final List<AbstractWidget> children = new ArrayList<>();
  
  public AnimatedTab(int width, int height, Component message, ExtendedButton.WidgetPosition position) {
    super(0, 0, width, height, message);
    this.position = position;
    this.targetWidth = width;
    this.targetHeight = height;
    this.setPosition();
  }
  // Method to add child widgets
  public void addChild(AbstractWidget child) {
    children.add(child);
  }
  
  private void setPosition() {
    Minecraft mc = Minecraft.getInstance();
    if (mc.screen instanceof AbstractContainerScreen<?> containerScreen) {
      int guiLeft = containerScreen.getGuiLeft(); // left x-coordinate of the GUI
      int guiTop = containerScreen.getGuiTop();   // top y-coordinate of the GUI
      int guiWidth = containerScreen.getXSize();  // width of the GUI texture
      int guiHeight = containerScreen.getYSize(); // height of the GUI texture
      
      switch (this.position) {
        case TOP_LEFT:
          this.setX(guiLeft - this.targetWidth);
          this.setY(guiTop);
          break;
        case TOP_RIGHT:
          this.setX(guiLeft + guiWidth);
          this.setY(guiTop);
          break;
        case BOTTOM_LEFT:
          this.setX(guiLeft - this.targetWidth);
          this.setY(guiTop + guiHeight - this.targetHeight);
          break;
        case BOTTOM_RIGHT:
          this.setX(guiLeft + guiWidth);
          this.setY(guiTop + guiHeight - this.targetHeight);
          break;
      }
    }
  }
  
  // Toggle the tab state
  public void toggleOpen() {
    isOpen = !isOpen;
  }
  
  // Exponential easing function for smoother transition
  private float ease(float current, float target, float speed) {
    return current + (target - current) * (1 - (float) Math.exp(-speed));
  }
  
  @Override
  protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    RenderSystem.enableBlend();
    if (!isOpen && animatedWidth <= 0 && animatedHeight <= 0) return;
    
    // Render the animated background
    this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    
    // Use an epsilon check to determine if the tab is fully opened
    if (Math.abs(animatedWidth - width) < 0.1F && Math.abs(animatedHeight - height) < 0.1F) {
      renderChildren(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    RenderSystem.disableBlend();
  }
  
  private void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    if (animatedHeight != height) {
      animatedHeight = ease(animatedHeight, isOpen ? this.targetHeight : 0, animationSpeed);
    }
    if (animatedWidth != width) {
      animatedWidth = ease(animatedWidth, isOpen ? this.targetWidth : 0, animationSpeed);
    }
    
    // Adjust start positions based on the widget position
    int startX = this.getX();
    int startY = this.getY();
    
    switch (this.position) {
      case TOP_LEFT:
        // Expand rightward and downward
        // No changes needed; this is the default behavior
        break;
      
      case TOP_RIGHT:
        // Expand leftward and downward from the top-right corner
        startX = (int) (this.getX() + this.width - animatedWidth);  // Start expanding leftwards
        break;
      
      case BOTTOM_RIGHT:
        // Expand rightward and upward from the bottom-left corner
        startY = (int) (this.getY() + this.height - animatedHeight);  // Start expanding upwards
        break;
      
      case BOTTOM_LEFT:
        // Expand leftward and upward from the bottom-right corner
        startX = (int) (this.getX() + this.width - animatedWidth);  // Expand leftwards
        startY = (int) (this.getY() + this.height - animatedHeight);  // Expand upwards
        break;
    }
    
    // Render the background with the adjusted start position and size
    guiGraphics.fill(
        startX,
        startY,
        (int) (startX + animatedWidth),
        (int) (startY + animatedHeight),
        0,
        0xFFFFFFF
    );
  }
  
  // Render children only when the tab is fully opened
  // Render children in a 3x3 grid
  private void renderChildren(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    if (!isOpen) return; // Only render when the tab is open
    int padding = 10;
    int spaceX = (int) (animatedWidth - padding);
    int spaceY = (int) (animatedHeight - padding);
    // Define grid dimensions
    int cols = 3; // Number of columns
    int rows = 3; // Number of rows
    int buttonWidth = Math.min((spaceX / cols), 24);
    int buttonHeight = Math.min(spaceY / rows, 24);
    
    for (int i = 0; i < children.size(); i++) {
      AbstractWidget child = children.get(i);
      int col = i % cols; // Column index
      int row = i / cols; // Row index
      
      // Calculate the position for the button
      int childX = this.getX() + padding + col * buttonWidth;
      int childY = this.getY() + padding + row * buttonHeight;
      
      child.setPosition(childX, childY);
      child.setSize(buttonWidth,buttonHeight);
      
      child.render(guiGraphics, mouseX, mouseY, partialTick);
    }
  }
  
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return false;
  }
  
  @Override
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
