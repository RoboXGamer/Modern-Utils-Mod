package net.roboxgamer.modernutils.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.roboxgamer.modernutils.menu.MagicBlockMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.modernutils.network.MagicBlockSettingsUpdatePayload;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class MagicBlockScreen extends AbstractContainerScreen<MagicBlockMenu> {
    private static final int BG_COLOR_DARK = 0xFF303030;

  private EditBox offsetXField;
  private EditBox offsetYField;
  private EditBox offsetZField;
  private ExtendedButton speedUpButton;
  private ExtendedButton speedDownButton;
  private ExtendedButton renderOutlineButton;

  public MagicBlockScreen(MagicBlockMenu menu, Inventory inventory, Component title) {
    super(menu, inventory, title);
    this.imageWidth = 176;
    this.imageHeight = 100;
  }

  @Override
  protected void init() {
    super.init();
    this.inventoryLabelY = 1000; // Hide inventory label

    this.leftPos = this.width / 2 - this.imageWidth / 2;
    this.topPos = this.height / 2 - this.imageHeight / 2;

    // Add coordinate input fields
    offsetXField = new EditBox(this.font, leftPos + 62, topPos + 20, 30, 12, Component.empty());
    offsetXField.setValue(String.valueOf(menu.blockEntity.getOffsetX()));
    offsetXField.setResponder(s -> {
      try {
        int value = Integer.parseInt(s);
        if (value >= -16 && value <= 16) {
          menu.blockEntity.setOffsetX(value);
          PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
              menu.blockEntity.getBlockPos(),
              Optional.empty(),
              Optional.of(value),
              Optional.empty(),
              Optional.empty(),
              Optional.empty()
          ));
        }
      } catch (NumberFormatException ignored) {
      }
    });

    offsetYField = new EditBox(this.font, leftPos + 62, topPos + 35, 30, 12, Component.empty());
    offsetYField.setValue(String.valueOf(menu.blockEntity.getOffsetY()));
    offsetYField.setResponder(s -> {
      try {
        int value = Integer.parseInt(s);
        if (value >= -16 && value <= 16) {
          menu.blockEntity.setOffsetY(value);
          PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
              menu.blockEntity.getBlockPos(),
              Optional.empty(),
              Optional.empty(),
              Optional.of(value),
              Optional.empty(),
              Optional.empty()
          ));
        }
      } catch (NumberFormatException ignored) {
      }
    });

    offsetZField = new EditBox(this.font, leftPos + 62, topPos + 50, 30, 12, Component.empty());
    offsetZField.setValue(String.valueOf(menu.blockEntity.getOffsetZ()));
    offsetZField.setResponder(s -> {
      try {
        int value = Integer.parseInt(s);
        if (value >= -16 && value <= 16) {
          menu.blockEntity.setOffsetZ(value);
          PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
              menu.blockEntity.getBlockPos(),
              Optional.empty(),
              Optional.empty(),
              Optional.empty(),
              Optional.of(value),
              Optional.empty()
          ));
        }
      } catch (NumberFormatException ignored) {
      }
    });

    // Add speed control buttons using ExtendedButton
    speedUpButton = addRenderableWidget(new ExtendedButton(
        "SpeedUpBtn",
        20, 20,
        Component.literal("+"),
        false,
        ExtendedButton.WidgetPosition.NONE,
        (button, clickAction, mouseX, mouseY) -> {
          int newSpeed = menu.blockEntity.incrementSpeed();
          menu.blockEntity.setSpeed(newSpeed);
          PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
              menu.blockEntity.getBlockPos(),
              Optional.of(newSpeed),
              Optional.empty(),
              Optional.empty(),
              Optional.empty(),
              Optional.empty()
          ));
        },
        this.minecraft.player));
    speedUpButton.setX(leftPos + 100);
    speedUpButton.setY(topPos + 20);

    speedDownButton = addRenderableWidget(new ExtendedButton(
        "SpeedDownBtn",
        20, 20,
        Component.literal("-"),
        false,
        ExtendedButton.WidgetPosition.NONE,
        (button, clickAction, mouseX, mouseY) -> {
          int newSpeed = menu.blockEntity.decrementSpeed();
          menu.blockEntity.setSpeed(newSpeed);
          PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
              menu.blockEntity.getBlockPos(),
              Optional.of(newSpeed),
              Optional.empty(),
              Optional.empty(),
              Optional.empty(),
              Optional.empty()
          ));
        },
        this.minecraft.player));
    speedDownButton.setX(leftPos + 100);
    speedDownButton.setY(topPos + 45);

    // Add render outline toggle button using ExtendedButton
    renderOutlineButton = addRenderableWidget(new ExtendedButton(
      "RenderOutlineBtn",
      60, 20,
      getOutlineButtonText(menu.blockEntity.getRenderOutline()),
      false,
      ExtendedButton.WidgetPosition.NONE,
      (button, clickAction, mouseX, mouseY) -> {
        boolean newState = !menu.blockEntity.getRenderOutline();
        menu.blockEntity.setRenderOutline(newState);
        PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
          menu.blockEntity.getBlockPos(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.empty(),
          Optional.of(newState)
        ));
        button.setMessage(getOutlineButtonText(newState));
      },
      this.minecraft.player));

    renderOutlineButton.setX(leftPos + 62);
    renderOutlineButton.setY(topPos + 65);

    // Add EditBoxes as widgets
    addRenderableWidget(offsetXField);
    addRenderableWidget(offsetYField);
    addRenderableWidget(offsetZField);
  }

  private Component getOutlineButtonText(boolean state) {
    return Component.literal(state ? "Outline: ON" : "Outline: OFF");
    }

  private void renderMyLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    int x = (width - imageWidth) / 2;
    int y = (height - imageHeight) / 2;
    guiGraphics.drawString(this.font, this.title, x + this.titleLabelX, y + this.titleLabelY, 0xFFFFFF, false);
    guiGraphics.drawString(this.font, "X:", x + 50, y + 22, 0xFFFFFF, false);
    guiGraphics.drawString(this.font, "Y:", x + 50, y + 37, 0xFFFFFF, false);
    guiGraphics.drawString(this.font, "Z:", x + 50, y + 52, 0xFFFFFF, false);
    guiGraphics.drawString(this.font, "Speed: " + menu.blockEntity.getSpeed(), x + 68, y + 6, 0xFFFFFF, false);
  }

  @Override
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    super.render(guiGraphics, mouseX, mouseY, delta);
    this.renderMyLabels(guiGraphics, mouseX, mouseY);
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }

  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    int x = (width - imageWidth) / 2;
    int y = (height - imageHeight) / 2;

    // Draw main background
    guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, BG_COLOR_DARK);
    // Draw darker border
    guiGraphics.fill(x, y, x + imageWidth, y + 1, BG_COLOR_DARK); // Top
    guiGraphics.fill(x, y, x + 1, y + imageHeight, BG_COLOR_DARK); // Left
    guiGraphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, BG_COLOR_DARK); // Right
    guiGraphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, BG_COLOR_DARK); // Bottom
  }
}