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

    // Simplified constants for layout
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

    private EditBox offsetXField;
    private EditBox offsetYField;
    private EditBox offsetZField;
    private ExtendedButton speedUpButton;
    private ExtendedButton speedDownButton;
    private ExtendedButton renderOutlineButton;
    private EditBox speedValueField;
    private ExtendedButton xPlusButton;
    private ExtendedButton xMinusButton;
    private ExtendedButton yPlusButton;
    private ExtendedButton yMinusButton;
    private ExtendedButton zPlusButton;
    private ExtendedButton zMinusButton;

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

        // Calculate panel dimensions
        int panelWidth = (imageWidth - (MARGIN * 2) - (SPACING * (PANEL_COUNT - 1))) / PANEL_COUNT;
        int contentStartY = topPos + HEADER_HEIGHT + LABEL_PADDING + CONTENT_TOP_MARGIN;

        // Calculate column centers
        for (int i = 0; i < PANEL_COUNT; i++) {
            int centerX = leftPos + MARGIN + (i * (panelWidth + SPACING)) + (panelWidth / 2);
            
            switch (i) {
                case 0 -> setupSpeedControls(centerX, contentStartY, BUTTON_WIDTH, panelWidth - SPACING * 2, VALUE_HEIGHT);
                case 1 -> setupOffsetControls(centerX, contentStartY, BUTTON_WIDTH, panelWidth - SPACING * 2, VALUE_HEIGHT,
                        offsetXField, xPlusButton, xMinusButton, menu.blockEntity::getOffsetX, menu.blockEntity::setOffsetX,
                        value -> new MagicBlockSettingsUpdatePayload(menu.blockEntity.getBlockPos(), Optional.empty(), Optional.of(value), Optional.empty(), Optional.empty(), Optional.empty()));
                case 2 -> setupOffsetControls(centerX, contentStartY, BUTTON_WIDTH, panelWidth - SPACING * 2, VALUE_HEIGHT,
                        offsetYField, yPlusButton, yMinusButton, menu.blockEntity::getOffsetY, menu.blockEntity::setOffsetY,
                        value -> new MagicBlockSettingsUpdatePayload(menu.blockEntity.getBlockPos(), Optional.empty(), Optional.empty(), Optional.of(value), Optional.empty(), Optional.empty()));
                case 3 -> setupOffsetControls(centerX, contentStartY, BUTTON_WIDTH, panelWidth - SPACING * 2, VALUE_HEIGHT,
                        offsetZField, zPlusButton, zMinusButton, menu.blockEntity::getOffsetZ, menu.blockEntity::setOffsetZ,
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

    private void setupSpeedControls(int centerX, int startY, int btnWidth, int valWidth, int valHeight) {
        int spacing = SPACING;
        
        // Speed up button
        speedUpButton = addRenderableWidget(new ExtendedButton(
            "SpeedUpBtn",
            btnWidth, MARGIN,
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
                speedValueField.setValue(String.valueOf(menu.blockEntity.getSpeed()));
            },
            this.minecraft.player));
        speedUpButton.setX(centerX - btnWidth / 2);
        speedUpButton.setY(startY);
        
        // Speed value field
        speedValueField = new EditBox(this.font, centerX - valWidth / 2, 
            startY + MARGIN + spacing, valWidth, valHeight, Component.empty());
        speedValueField.setValue(String.valueOf(menu.blockEntity.getSpeed()));
        speedValueField.setResponder(s -> {
            try {
                int value = Integer.parseInt(s);
                if (value >= 0 && value <= 20) {
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
        addRenderableWidget(speedValueField);

        // Speed down button
        speedDownButton = addRenderableWidget(new ExtendedButton(
            "SpeedDownBtn", 
            btnWidth, MARGIN,
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
                speedValueField.setValue(String.valueOf(menu.blockEntity.getSpeed()));
            },
            this.minecraft.player));
        speedDownButton.setX(centerX - btnWidth / 2);
        speedDownButton.setY(startY + MARGIN + spacing + valHeight + spacing);
    }

    private void setupOffsetControls(
            int centerX, int startY, int btnWidth, int valWidth, int valHeight,
            EditBox field, ExtendedButton plusBtn, ExtendedButton minusBtn,
            Supplier<Integer> getter, Consumer<Integer> setter,
            Function<Integer, MagicBlockSettingsUpdatePayload> payloadCreator) {
        
        int spacing = SPACING;

        // Plus button
        EditBox finalField = field;
        plusBtn = addRenderableWidget(new ExtendedButton(
            "PlusBtn",
            btnWidth, MARGIN,
            Component.literal("+"),
            false,
            ExtendedButton.WidgetPosition.NONE,
            (button, clickAction, mouseX, mouseY) -> {
                try {
                    int cur = Integer.parseInt(finalField.getValue());
                    if (cur < 16) {
                        cur++;
                        finalField.setValue(String.valueOf(cur));
                        setter.accept(cur);
                        PacketDistributor.sendToServer(payloadCreator.apply(cur));
                    }
                } catch (NumberFormatException ignored) {}
            },
            this.minecraft.player));
        plusBtn.setX(centerX - btnWidth / 2);
        plusBtn.setY(startY);

        // Value field
        field = new EditBox(this.font, centerX - valWidth / 2, 
            startY + MARGIN + spacing, valWidth, valHeight, Component.empty());
        field.setValue(String.valueOf(getter.get()));
        field.setResponder(s -> {
            try {
                int value = Integer.parseInt(s);
                if (value >= -16 && value <= 16) {
                    setter.accept(value);
                    PacketDistributor.sendToServer(payloadCreator.apply(value));
                }
            } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(field);

        // Minus button
        EditBox finalField1 = field;
        minusBtn = addRenderableWidget(new ExtendedButton(
            "MinusBtn",
            btnWidth, MARGIN,
            Component.literal("-"),
            false,
            ExtendedButton.WidgetPosition.NONE,
            (button, clickAction, mouseX, mouseY) -> {
                try {
                    int cur = Integer.parseInt(finalField1.getValue());
                    if (cur > -16) {
                        cur--;
                        finalField1.setValue(String.valueOf(cur));
                        setter.accept(cur);
                        PacketDistributor.sendToServer(payloadCreator.apply(cur));
                    }
                } catch (NumberFormatException ignored) {}
            },
            this.minecraft.player));
        minusBtn.setX(centerX - btnWidth / 2);
        minusBtn.setY(startY + MARGIN + spacing + valHeight + spacing);
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
        int panelWidth = (imageWidth - (MARGIN * 2) - (SPACING * (PANEL_COUNT - 1))) / PANEL_COUNT;
        
        for (int i = 0; i < labels.length; i++) {
            int panelX = leftPos + MARGIN + (i * (panelWidth + SPACING));
            int textWidth = this.font.width(labels[i]);
            guiGraphics.drawString(this.font, labels[i], 
                panelX + (panelWidth - textWidth) / 2,
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
        int panelWidth = (imageWidth - (MARGIN * 2) - (SPACING * (PANEL_COUNT - 1))) / PANEL_COUNT;
        int panelHeight = imageHeight - HEADER_HEIGHT - (MARGIN * 2) - 10;
        
        for (int i = 0; i < PANEL_COUNT; i++) {
            int panelX = leftPos + MARGIN + (i * (panelWidth + SPACING));
            int panelY = topPos + HEADER_HEIGHT;
            guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x0DFFFFFF);
        }
    }
}