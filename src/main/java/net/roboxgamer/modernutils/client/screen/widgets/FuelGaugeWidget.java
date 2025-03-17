package net.roboxgamer.modernutils.client.screen.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.roboxgamer.modernutils.util.Constants;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;

public class FuelGaugeWidget extends AbstractWidget {
    private static final int GAUGE_WIDTH = 18;
    private static final int GAUGE_HEIGHT = 16;
    private final FuelSupplier fuelSupplier;

    public interface FuelSupplier {
        int getFuelLevel();
        boolean hasFuel();
    }

    public FuelGaugeWidget(int x, int y, FuelSupplier fuelSupplier) {
        super(x, y, GAUGE_WIDTH, GAUGE_HEIGHT, Component.empty());
        this.fuelSupplier = fuelSupplier;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!fuelSupplier.hasFuel()) return;

        // Draw fuel background
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, Constants.Colors.FUEL_BACKGROUND);
        
        // Draw fuel level
        int fuelLevel = fuelSupplier.getFuelLevel();
        if (fuelLevel > 0) {
            int fuelHeight = Mth.ceil((fuelLevel / 100f) * height);
            guiGraphics.fill(getX(), getY() + height - fuelHeight,
                getX() + width, getY() + height, Constants.Colors.FUEL_FILL);
        }
        
        // Draw border
        drawBorder(guiGraphics, getX(), getY(), width, height, Constants.Colors.BORDER_LIGHT, Constants.Colors.BORDER_DARK);

        // Render tooltip if hovered
        if (isHovered() && fuelSupplier.hasFuel()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font,
                Component.translatable("screen.modernutils.mechanical_furnace.fuel_remaining", fuelSupplier.getFuelLevel()),
                mouseX, mouseY);
        }
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int lightColor, int darkColor) {
      // Top border
        guiGraphics.fill(x, y, x + width, y + 1, darkColor);
        // Left border
        guiGraphics.fill(x, y, x + 1, y + height, darkColor);
        // Bottom border
        guiGraphics.fill(x, y + height - 1, x + width, y + height, darkColor);
        // Right border
        guiGraphics.fill(x + width - 1, y, x + width, y + height, darkColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.modernutils.fuel_gauge", fuelSupplier.getFuelLevel()));
    }
} 