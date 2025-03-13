package net.roboxgamer.modernutils.client.screen.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;

public class SlotWidget extends AbstractWidget {
    private static final int SLOT_SIZE = 18;
    private static final int BORDER_SIZE = 1;

    public SlotWidget(int x, int y) {
        super(x, y, SLOT_SIZE, SLOT_SIZE, Component.empty());
        this.active = false; // Make widget non-interactive
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false; // Disable click handling
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // Do nothing to prevent click handling
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw slot background
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF2F2F2F);
        
        // Draw 3D border effect
        drawBorder(guiGraphics, getX(), getY(), width, height, 0xFF4F4F4F, 0xFF1F1F1F);
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int lightColor, int darkColor) {
        // Top border
        guiGraphics.fill(x, y, x + width, y + BORDER_SIZE, darkColor);
        // Left border
        guiGraphics.fill(x, y, x + BORDER_SIZE, y + height, darkColor);
        // Bottom border
        guiGraphics.fill(x, y + height - BORDER_SIZE, x + width, y + height, lightColor);
        // Right border
        guiGraphics.fill(x + width - BORDER_SIZE, y, x + width, y + height, lightColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.modernutils.slot"));
    }
} 