package net.roboxgamer.modernutils.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.PackedButtonData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SideConfigButton extends ExtendedButton {
  private final Constants.Sides side;
  private MechanicalCrafterBlockEntity blockEntity = null;
  private final AbstractContainerScreen<?> screen;
  
  public SideConfigButton(
      String id,
      Constants.Sides side,
      AbstractContainerScreen<?> screen,
      BlockEntity blockEntity,
      Player player
  ) {
    super(
        id,
        24, 24,  // Standard size for all side buttons
        Component.literal(side.toString() + " Side"),
        true,    // Always has icon
        ExtendedButton.WidgetPosition.NONE,
        sendToServerAction(screen, side),
        player
    );
    this.side = side;
    if (blockEntity instanceof MechanicalCrafterBlockEntity mechanicalCrafter) {
      this.blockEntity = mechanicalCrafter;
    }
    this.screen = screen;
  }
  
  @Override
  public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
    // Get the current mode of the side
    Constants.SideState sideMode = this.blockEntity.getSideManager().getSideState(side);
    
    // Determine the background color based on the mode
    int backgroundColor = Constants.getColorForMode(sideMode);
    int margin = 2;
    // Render the background color
    guiGraphics.fill(
        this.getX() + margin,
        this.getY() + margin,
        this.getX() + this.getWidth() - margin,
        this.getY() + this.getHeight() - margin,
        backgroundColor
    );
    
    // Render the icon with scaling and centering
    float scale = 1.15f;
    float offset = (this.getWidth() - (16 * scale)) / 2;
    
    guiGraphics.pose().pushPose();
    guiGraphics.pose().translate(this.getX() + offset, this.getY() + offset, 0);
    guiGraphics.pose().scale(scale, scale, 1);
    ItemStack item = ItemStack.EMPTY;
    var pos = this.blockEntity.getBlockPos().relative(this.blockEntity.getSideManager().getRelativeDirection(side));
    var state = Objects.requireNonNull(this.blockEntity.getLevel()).getBlockState(pos);
    if (state.hasBlockEntity() && this.blockEntity.getLevel().isLoaded(pos)){
      item = state.getBlock().asItem().getDefaultInstance();
    }
    guiGraphics.renderFakeItem(
        item,
        0,
        0
    );
    guiGraphics.pose().popPose();
  }
  
  @Override
  public void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
    if (blockEntity instanceof MechanicalCrafterBlockEntity mechanicalCrafter) {
      Constants.SideState sideState = mechanicalCrafter.getSideManager().getSideState(side);
      Component msg = Component.literal(String.format("%s Side, State: %s", side, sideState));
      guiGraphics.renderTooltip(Minecraft.getInstance().font, msg, mouseX, mouseY);
    }
  }
  
  public Constants.Sides getSide() {
    return side;
  }
  
  public Constants.SideState getSideState() {
    if (blockEntity instanceof MechanicalCrafterBlockEntity mechanicalCrafter) {
      return mechanicalCrafter.getSideManager().getSideState(side);
    }
    return Constants.SideState.NONE;
  }
  
  private static ExtendedButton.OnPressExtended sendToServerAction(
      @NotNull AbstractContainerScreen<?> screen,
      Constants.Sides side
  ) {
    return (button, clickAction, mouseX, mouseY) -> {
      boolean shifted = Screen.hasShiftDown();
      PackedButtonData data = PackedButtonData.create(
          side,
          shifted,
          clickAction
      );
      screen.getMinecraft().gameMode.handleInventoryButtonClick(
          screen.getMenu().containerId, data.toId());
    };
  }
  
  public void updateTooltip() {
    if (blockEntity instanceof MechanicalCrafterBlockEntity) {
      Constants.SideState sideState = getSideState();
      this.setTooltip(Tooltip.create(
          Component.literal(String.format("%s Side, State: %s", side, sideState))
      ));
    }
  }
}
