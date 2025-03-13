package net.roboxgamer.modernutils.client.screen.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;

public class ProgressArrowWidget extends AbstractWidget {
    private static final int SLOT_SIZE = 14;
    private static final int PROGRESS_HEIGHT = 14;

    private final ProgressSupplier progressSupplier;

    public interface ProgressSupplier {
        int getProgress();
    }

    public ProgressArrowWidget(int x, int y, ProgressSupplier progressSupplier) {
        super(x, y, SLOT_SIZE, PROGRESS_HEIGHT, Component.empty());
        this.progressSupplier = progressSupplier;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw progress background
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF3F3F3F);
        
        // Draw progress bar from top to bottom
        int progress = progressSupplier.getProgress();
        if (progress > 0) {
            int progressHeight = Mth.ceil((progress / 100f) * height);
            guiGraphics.fill(getX(), getY(), 
                getX() + width, getY() + progressHeight, 0xFF00FF00);
        }
        
        // Draw border
        drawBorder(guiGraphics, getX(), getY(), width, height, 0xFF4F4F4F, 0xFF1F1F1F);

        // Render tooltip if hovered
        if (isHovered()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, 
                Component.translatable("screen.modernutils.mechanical_furnace.progress", progressSupplier.getProgress()), 
                mouseX, mouseY);
        }
    }

    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int lightColor, int darkColor) {
        // Top border
        guiGraphics.fill(x, y, x + width, y + 1, lightColor);
        // Left border
        guiGraphics.fill(x, y, x + 1, y + height, lightColor);
        // Bottom border
        guiGraphics.fill(x, y + height - 1, x + width, y + height, darkColor);
        // Right border
        guiGraphics.fill(x + width - 1, y, x + width, y + height, darkColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.modernutils.progress_arrow", progressSupplier.getProgress()));
    }
} 