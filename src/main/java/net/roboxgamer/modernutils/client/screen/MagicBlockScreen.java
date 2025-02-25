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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

public class MagicBlockScreen extends AbstractContainerScreen<MagicBlockMenu> {

    private static final int SHADOW_OFFSET = 4;
    private static final int SHADOW_COLOR = 0x55000000;
    private static final int CONTAINER_COLOR = 0xCC1A1F2C;
    private static final int BORDER_COLOR = 0xFF1A1F2C;
    
    // Layout constants
    private static final int MARGIN = 10;
    private static final int SPACING = 5;
    private static final int BUTTON_WIDTH = 12;
    private static final int BUTTON_HEIGHT = 12;
    private static final int VALUE_HEIGHT = 12;
    private static final int PANEL_COUNT = 4;
    private static final int HEADER_HEIGHT = 20;
    private static final int HIDDEN_LABEL_POS = 1000;
    private static final int LABEL_PADDING = 8; // Padding below labels
    private static final int CONTENT_TOP_MARGIN = 15; // Space between label and first control
    private static final int MIN_OFFSET = -16;
    private static final int MAX_OFFSET = 16;
    private static final int MIN_SPEED = 2;
    private static final int MAX_SPEED = 256;
    private static final int PANEL_BOTTOM_PADDING = 10; // The extra padding at the bottom of panels

    private EditBox offsetXField;
    private EditBox offsetYField;
    private EditBox offsetZField;
    private EditBox speedValueField;
    private ExtendedButton renderOutlineButton;

    public MagicBlockScreen(MagicBlockMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 200;
        this.imageHeight = 120;
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = HIDDEN_LABEL_POS;
        this.titleLabelY = HIDDEN_LABEL_POS;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Calculate content start Y position
        int contentStartY = topPos + HEADER_HEIGHT + LABEL_PADDING + CONTENT_TOP_MARGIN;

        // Setup panels
        for (int i = 0; i < PANEL_COUNT; i++) {
            int centerX = getPanelCenterX(i);
            
            switch (i) {
                case 0 -> setupSpeedControls(centerX, contentStartY, BUTTON_WIDTH, getPanelWidth() - SPACING * 2, VALUE_HEIGHT);
                case 1 -> setupOffsetControls(centerX, contentStartY, BUTTON_WIDTH, getPanelWidth() - SPACING * 2, VALUE_HEIGHT,
                        "X", menu.blockEntity::getOffsetX, menu.blockEntity::setOffsetX,
                        value -> new MagicBlockSettingsUpdatePayload(menu.blockEntity.getBlockPos(), Optional.empty(), Optional.of(value), Optional.empty(), Optional.empty(), Optional.empty()));
                case 2 -> setupOffsetControls(centerX, contentStartY, BUTTON_WIDTH, getPanelWidth() - SPACING * 2, VALUE_HEIGHT,
                        "Y", menu.blockEntity::getOffsetY, menu.blockEntity::setOffsetY,
                        value -> new MagicBlockSettingsUpdatePayload(menu.blockEntity.getBlockPos(), Optional.empty(), Optional.empty(), Optional.of(value), Optional.empty(), Optional.empty()));
                case 3 -> setupOffsetControls(centerX, contentStartY, BUTTON_WIDTH, getPanelWidth() - SPACING * 2, VALUE_HEIGHT,
                        "Z", menu.blockEntity::getOffsetZ, menu.blockEntity::setOffsetZ,
                        value -> new MagicBlockSettingsUpdatePayload(menu.blockEntity.getBlockPos(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(value), Optional.empty()));
            }
        }

        // Render outline button positioned at bottom
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
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.of(newState)
                ));
                button.setMessage(getOutlineButtonText(newState));
            },
            this.minecraft.player));

        renderOutlineButton.setX(leftPos + (imageWidth - 60) / 2);
        renderOutlineButton.setY(topPos + imageHeight - 25);
    }

    // Helper methods for layout calculations
    private int getPanelWidth() {
        return (imageWidth - (MARGIN * 2) - (SPACING * (PANEL_COUNT - 1))) / PANEL_COUNT;
    }
    
    private int getPanelHeight() {
        return imageHeight - HEADER_HEIGHT - (MARGIN * 2) - PANEL_BOTTOM_PADDING;
    }
    
    private int getPanelX(int panelIndex) {
        return leftPos + MARGIN + (panelIndex * (getPanelWidth() + SPACING));
    }
    
    private int getPanelCenterX(int panelIndex) {
        return getPanelX(panelIndex) + (getPanelWidth() / 2);
    }
    
    private int getPanelY() {
        return topPos + HEADER_HEIGHT;
    }

    private void setupSpeedControls(int centerX, int startY, int btnWidth, int valWidth, int valHeight) {
        int spacing = SPACING;
        
        // Speed value field
        speedValueField = createSpeedField(centerX - valWidth / 2, startY + BUTTON_HEIGHT + spacing, valWidth, valHeight);
        addRenderableWidget(speedValueField);
        
        // Speed up button
        SpeedButton speedUpButton = new SpeedButton(
            "Speed", true, speedValueField, btnWidth, BUTTON_HEIGHT
        );
        speedUpButton.setX(centerX - btnWidth / 2);
        speedUpButton.setY(startY);
        addRenderableWidget(speedUpButton);
        
        // Speed down button
        SpeedButton speedDownButton = new SpeedButton(
            "Speed", false, speedValueField, btnWidth, BUTTON_HEIGHT
        );
        speedDownButton.setX(centerX - btnWidth / 2);
        speedDownButton.setY(startY + BUTTON_HEIGHT + spacing + valHeight + spacing);
        addRenderableWidget(speedDownButton);
    }

    private void setupOffsetControls(
            int centerX, int startY, int btnWidth, int valWidth, int valHeight,
            String axis, Supplier<Integer> getter, Consumer<Integer> setter,
            Function<Integer, MagicBlockSettingsUpdatePayload> payloadCreator) {
        
        int spacing = SPACING;
        
        // Value field
        EditBox field = createOffsetField(
            centerX - valWidth / 2, startY + BUTTON_HEIGHT + spacing, 
            valWidth, valHeight, getter, setter, payloadCreator
        );
        addRenderableWidget(field);
        
        // Store field reference
        switch (axis) {
            case "X" -> offsetXField = field;
            case "Y" -> offsetYField = field;
            case "Z" -> offsetZField = field;
        }

        // Plus button
        OffsetButton plusButton = new OffsetButton(
            axis, true, field, setter, payloadCreator, btnWidth, BUTTON_HEIGHT
        );
        plusButton.setX(centerX - btnWidth / 2);
        plusButton.setY(startY);
        addRenderableWidget(plusButton);

        // Minus button
        OffsetButton minusButton = new OffsetButton(
            axis, false, field, setter, payloadCreator, btnWidth, BUTTON_HEIGHT
        );
        minusButton.setX(centerX - btnWidth / 2);
        minusButton.setY(startY + BUTTON_HEIGHT + spacing + valHeight + spacing);
        addRenderableWidget(minusButton);
    }
    
    private EditBox createOffsetField(
            int x, int y, int width, int height,
            Supplier<Integer> getter, Consumer<Integer> setter,
            Function<Integer, MagicBlockSettingsUpdatePayload> payloadCreator) {
        
        EditBox field = new EditBox(this.font, x, y, width, height, Component.empty());
        field.setValue(String.valueOf(getter.get()));
        field.setResponder(s -> {
            try {
                int value = Integer.parseInt(s);
                if (value >= MIN_OFFSET && value <= MAX_OFFSET) {
                    setter.accept(value);
                    PacketDistributor.sendToServer(payloadCreator.apply(value));
                }
            } catch (NumberFormatException ignored) {}
        });
        return field;
    }

    private EditBox createSpeedField(int x, int y, int width, int height) {
        EditBox field = new EditBox(this.font, x, y, width, height, Component.empty());
        field.setValue(String.valueOf(menu.blockEntity.getSpeed()));
        field.setResponder(s -> {
            try {
                int value = Integer.parseInt(s);
                if (value >= MIN_SPEED && value <= MAX_SPEED) {
                    menu.blockEntity.setSpeed(value);
                    PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
                        menu.blockEntity.getBlockPos(),
                        Optional.of(value),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                    ));
                }
            } catch (NumberFormatException ignored) {}
        });
        return field;
    }

    private Component getOutlineButtonText(boolean state) {
        return Component.literal(state ? "Outline: ON" : "Outline: OFF");
    }

    private void renderMyLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title centered at the top
        guiGraphics.drawString(this.font, this.title, 
            leftPos + (imageWidth - font.width(this.title)) / 2, 
            topPos + 6, 0xF6F6F7, false);
        
        // Panel labels
        String[] labels = {"Speed", "X", "Y", "Z"};
        
        for (int i = 0; i < labels.length; i++) {
            int panelX = getPanelX(i);
            int textWidth = this.font.width(labels[i]);
            guiGraphics.drawString(this.font, labels[i], 
                panelX + (getPanelWidth() - textWidth) / 2,
                topPos + HEADER_HEIGHT + 6, 
                0xF6F6F7, false);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderMyLabels(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw shadow
        guiGraphics.fill(leftPos + SHADOW_OFFSET, topPos + SHADOW_OFFSET,
                        leftPos + imageWidth + SHADOW_OFFSET, topPos + imageHeight + SHADOW_OFFSET, 
                        SHADOW_COLOR);
        
        // Draw container
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, CONTAINER_COLOR);
        
        // Draw border
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, BORDER_COLOR);
        guiGraphics.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, BORDER_COLOR);
        guiGraphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, BORDER_COLOR);
        guiGraphics.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, BORDER_COLOR);
        
        // Draw panels
        for (int i = 0; i < PANEL_COUNT; i++) {
            int panelX = getPanelX(i);
            int panelY = getPanelY();
            guiGraphics.fill(panelX, panelY, panelX + getPanelWidth(), panelY + getPanelHeight(), 0x0DFFFFFF);
        }
    }
    
    // Custom button for offset controls
    private class OffsetButton extends ExtendedButton {
        private final boolean isPlus;
        private final EditBox targetField;
        private final Consumer<Integer> setter;
        private final Function<Integer, MagicBlockSettingsUpdatePayload> payloadCreator;

        public OffsetButton(
                String axis, boolean isPlus, EditBox targetField,
                Consumer<Integer> setter,
                Function<Integer, MagicBlockSettingsUpdatePayload> payloadCreator,
                int width, int height) {
            super(
                axis + (isPlus ? "Plus" : "Minus") + "Btn",
                width, height,
                Component.literal(isPlus ? "+" : "-"),
                false,
                ExtendedButton.WidgetPosition.NONE,
                (button, clickAction, mouseX, mouseY) -> {
                    try {
                        int cur = Integer.parseInt(targetField.getValue());
                        if (isPlus ? cur < MAX_OFFSET : cur > MIN_OFFSET) {
                            cur = isPlus ? cur + 1 : cur - 1;
                            targetField.setValue(String.valueOf(cur));
                            setter.accept(cur);
                            PacketDistributor.sendToServer(payloadCreator.apply(cur));
                        }
                    } catch (NumberFormatException ignored) {}
                },
                MagicBlockScreen.this.minecraft.player);
            
            this.isPlus = isPlus;
            this.targetField = targetField;
            this.setter = setter;
            this.payloadCreator = payloadCreator;
        }
    }
    
    // Custom button for speed controls
    private class SpeedButton extends ExtendedButton {
        private final boolean isPlus;
        private final EditBox targetField;

        public SpeedButton(
                String id, boolean isPlus, EditBox targetField,
                int width, int height) {
            super(
                id + (isPlus ? "Up" : "Down") + "Btn",
                width, height,
                Component.literal(isPlus ? "+" : "-"),
                false,
                ExtendedButton.WidgetPosition.NONE,
                (button, clickAction, mouseX, mouseY) -> {
                    int newSpeed = isPlus ? 
                        MagicBlockScreen.this.menu.blockEntity.incrementSpeed() : 
                        MagicBlockScreen.this.menu.blockEntity.decrementSpeed();
                    MagicBlockScreen.this.menu.blockEntity.setSpeed(newSpeed);
                    PacketDistributor.sendToServer(new MagicBlockSettingsUpdatePayload(
                        MagicBlockScreen.this.menu.blockEntity.getBlockPos(),
                        Optional.of(newSpeed),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()
                    ));
                    targetField.setValue(String.valueOf(MagicBlockScreen.this.menu.blockEntity.getSpeed()));
                },
                MagicBlockScreen.this.minecraft.player);
            
            this.isPlus = isPlus;
            this.targetField = targetField;
        }
    }
}