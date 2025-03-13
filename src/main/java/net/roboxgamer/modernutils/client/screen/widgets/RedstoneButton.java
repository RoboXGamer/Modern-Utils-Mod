package net.roboxgamer.modernutils.client.screen.widgets;

import static net.roboxgamer.modernutils.util.RedstoneManager.REDSTONE_MODE_MAP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.modernutils.util.RedstoneManager.RedstoneMode;
import net.roboxgamer.modernutils.util.RedstoneManager;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.network.RedstoneModePayload;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.Constants.IRedstoneConfigurable;

public class RedstoneButton extends AbstractWidget {
  private static final int BUTTON_SIZE = 16;
  private final AbstractContainerScreen<?> screen;
  private IRedstoneConfigurable configurable = null;
  private BlockEntity blockEntity = null;

  public RedstoneButton(int x, int y, AbstractContainerScreen<?> screen, BlockEntity blockEntity) {
    super(x, y, BUTTON_SIZE, BUTTON_SIZE, Component.empty());
    this.screen = screen;
    this.blockEntity = blockEntity;
    if (blockEntity instanceof Constants.IRedstoneConfigurable machine) {
      this.configurable = machine;
    }
  }

  private RedstoneMode getCurrentMode() {
    return configurable.getRedstoneManager().getRedstoneMode();
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    RedstoneManager.RedstoneMode value = configurable.getRedstoneManager().getNextRedstoneMode();
    configurable.getRedstoneManager().setRedstoneMode(value);
    ModernUtilsMod.LOGGER.debug("Toggled redstoneModeValue to {}", value);
    PacketDistributor.sendToServer(new RedstoneModePayload(value.ordinal(), this.blockEntity.getBlockPos()));
  }

  @Override
  public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    RedstoneMode currentMode = getCurrentMode();

    // Draw button background
    int bgColor = switch (currentMode) {
      case ALWAYS_ON -> 0xFF404040;
      case REDSTONE_ON -> 0xFF804040;
      case REDSTONE_OFF -> 0xFF404080;
      case PULSE -> 0xFF808040;
    };
    guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, bgColor);

    // Draw border
    int borderLight = 0xFF606060;
    int borderDark = 0xFF202020;
    guiGraphics.fill(getX(), getY(), getX() + width, getY() + 1, borderLight); // Top border
    guiGraphics.fill(getX(), getY(), getX() + 1, getY() + height, borderLight); // Left border
    guiGraphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, borderDark); // Bottom border
    guiGraphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, borderDark); // Right border

    // Draw redstone symbol
    int symbolColor = isHovered() ? 0xFFFF0000 : 0xFFC00000;
    guiGraphics.fill(getX() + 6, getY() + 4, getX() + 10, getY() + 12, symbolColor);
    guiGraphics.fill(getX() + 4, getY() + 6, getX() + 12, getY() + 10, symbolColor);

    // Render tooltip if hovered
    if (isHovered()) {
      String modeText = switch (currentMode) {
        case ALWAYS_ON -> "Redstone Mode: Always On";
        case REDSTONE_ON -> "Redstone Mode: Requires Signal";
        case REDSTONE_OFF -> "Redstone Mode: No Signal";
        case PULSE -> "Redstone Mode: Pulse";
      };
      guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.literal(modeText), mouseX, mouseY);
    }
  }

  @Override
  protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    narrationElementOutput.add(NarratedElementType.TITLE,
        Component.translatable("narration.modernutils.redstone_mode", getCurrentMode().toString()));
  }
}