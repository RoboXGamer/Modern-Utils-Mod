package net.roboxgamer.modernutils.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.item.ModItems;
import net.roboxgamer.modernutils.menu.MechanicalCrafterMenu;
import net.roboxgamer.modernutils.menu.OutputSlotItemHandler;
import net.roboxgamer.modernutils.network.RedstoneModePayload;
import net.roboxgamer.modernutils.network.RemainItemTogglePayload;
import net.roboxgamer.modernutils.network.SlotStatePayload;
import net.roboxgamer.modernutils.util.AddonClientManager;
import net.roboxgamer.modernutils.util.AddonManager;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.RedstoneManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static net.roboxgamer.modernutils.util.RedstoneManager.REDSTONE_MODE_MAP;

public class MechanicalCrafterScreen extends AbstractContainerScreen<MechanicalCrafterMenu> {
  private static final String location = ModernUtilsMod.MODID + ".mechanical_crafter_screen";
  private static final Component BUTTON_TEXT = Component.translatable("gui." + location + ".button_text");

  private static final ResourceLocation TEXTURE = ModernUtilsMod.location("textures/gui/mechanical_crafter_screen.png");

  private static final ResourceLocation[] REDSTONE_MODE_TEXTURES = {
      ModernUtilsMod.location("redstone_mode_0"),
      ModernUtilsMod.location("redstone_mode_1"),
      ModernUtilsMod.location("redstone_mode_2"),
      ModernUtilsMod.location("redstone_mode_3")
  };
  private static final ResourceLocation DISABLED_SLOT_LOCATION_SPRITE = ResourceLocation
      .withDefaultNamespace("container/crafter/disabled_slot");
  private static final Component DISABLED_SLOT_TOOLTIP = Component.translatable("gui.togglable_slot");

  private final BlockPos position;
  private final Player player;

  private MechanicalCrafterBlockEntity blockEntity;
  private RedstoneManager redstoneManager;

  // Widgets
  private Button remainingToggleBtn;
  private ImageButton redstoneModeButton;
  public AnimatedTab SideConfigTab;
  private ExtendedButton autoImportBtn;
  private ExtendedButton sideConfigBtn;
  private ExtendedButton autoExportBtn;
  private ExtendedButton upSideBtn;
  private ExtendedButton downSideBtn;
  private ExtendedButton leftSideBtn;
  private ExtendedButton rightSideBtn;
  private ExtendedButton backSideBtn;
  private ExtendedButton frontSideBtn;
  private Map<Constants.Sides, SideConfigButton> sideButtons = new HashMap<>();

  // Animation variables for smooth progress bar
  private float lastProgress = 0f;
  private float visualProgress = 0f;
  private AddonManager addonManager;
  private AddonClientManager addonClientManager;

  public MechanicalCrafterScreen(MechanicalCrafterMenu menu, Inventory playerInv, Component title) {
    super(menu, playerInv, title);
    this.position = menu.getBlockEntity().getBlockPos();
    this.imageWidth = 176;
    this.imageHeight = 236;
    this.inventoryLabelY = this.imageHeight - 92;
    this.player = playerInv.player;
    this.titleLabelY = this.topPos + 8;
  }

  @Override
  protected void init() {
    this.leftPos = this.width / 2 - this.imageWidth / 2;
    this.topPos = this.height / 2 - this.imageHeight / 2;

    if (this.minecraft == null)
      return;
    Level level = this.minecraft.level;
    if (level == null)
      return;

    BlockEntity be = level.getBlockEntity(this.position);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      this.blockEntity = mcbe;
      this.redstoneManager = this.blockEntity.getRedstoneManager();
      this.addonManager = this.blockEntity.getAddonManager();
      this.addonClientManager = new AddonClientManager(this.addonManager);

    } else {
      ModernUtilsMod.LOGGER.error("Mechanical Crafter Screen: BlockEntity is not a MechanicalCrafterBlockEntity!");
      return;
    }

    // Widgets to add!

    this.remainingToggleBtn = addRenderableWidget(
        new ImageButton(this.leftPos + this.imageWidth - 56, this.topPos + 34, 20, 20,
            new WidgetSprites(
                ModernUtilsMod.location("toggle_remain_btn"),
                ModernUtilsMod.location("toggle_remain_btn_disabled"),
                ModernUtilsMod.location("toggle_remain_btn_highlighted")),
            this::handleButtonClick, BUTTON_TEXT));
    var remainItemToggleValue = this.blockEntity.getRemainItemToggleDisplayValue();
    this.remainingToggleBtn.setTooltip(
        Tooltip.create(
            Component.literal(
                String.format("Remaining Items [%s]", remainItemToggleValue))));

    this.redstoneModeButton = addRenderableWidget(
        new ImageButton(this.leftPos + this.imageWidth - 56, this.topPos + 64, 20, 20,
            new WidgetSprites(
                ModernUtilsMod.location("redstone_mode_btn"),
                ModernUtilsMod.location("redstone_mode_btn_disabled"),
                ModernUtilsMod.location("redstone_mode_btn_highlighted")),
            this::handleRedstoneModeButtonClick, BUTTON_TEXT) {
          @Override
          public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            var sprites = getRedstoneButtonSprites();
            var resourcelocation = sprites.get(this.isActive(), this.isHoveredOrFocused());
            guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
          }
        });
    updateRedstoneButtonTooltip();

    this.SideConfigTab = new AnimatedTab(
        92, 92, Component.empty(), ExtendedButton.WidgetPosition.BOTTOM_LEFT);
    addRenderableWidget(SideConfigTab);

    // Create addon tab and button using the client manager
    this.addonClientManager.createAddonTab(this.player, this);
    addRenderableWidget(this.addonClientManager.getAddonTab());
    addRenderableWidget(this.addonClientManager.getAddonConfigButton());

    this.sideConfigBtn = new ExtendedButton(
        "Config_Btn",
        24, 24,
        Component.literal("Settings"),
        true,
        ExtendedButton.WidgetPosition.BOTTOM_LEFT,
        (button1, clickAction, mouseX, mouseY) -> {
          SideConfigTab.toggleOpen();
        },
        this.player) {
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
          ExtendedButton extendedButton) {
        float scale = 1;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(ModItems.EXAMPLE_ITEM.get().getDefaultInstance(),
            0,
            0);
        guiGraphics.pose().popPose();
      }
    };
    addRenderableWidget(this.sideConfigBtn);

    this.autoImportBtn = new ExtendedButton(
        "AutoImportBtn",
        24, 24,
        Component.empty(),
        true,
        ExtendedButton.WidgetPosition.NONE,
        (button, clickAction, mouseX, mouseY) -> this.handleAutoImportButtonClick(button),
        this.player) {
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
          ExtendedButton extendedButton) {
        // Switch between two textures based on the state of the button
        boolean state = getAutoImportState();
        var sprite = state ? ModernUtilsMod.location("auto_import_on") : ModernUtilsMod.location("auto_import_off");
        guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
      }
    };
    updateAutoImportButtonTooltip();

    this.autoExportBtn = new ExtendedButton(
        "AutoExportBtn",
        24, 24,
        Component.empty(),
        true,
        ExtendedButton.WidgetPosition.NONE,
        (button, clickAction, mouseX, mouseY) -> this.handleAutoExportButtonClick(button),
        this.player) {
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick,
          ExtendedButton extendedButton) {
        // Switch between two textures based on the state of the button
        boolean state = getAutoExportState();
        var sprite = state ? ModernUtilsMod.location("auto_export_on") : ModernUtilsMod.location("auto_export_off");
        guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
      }
    };
    updateAutoExportButtonTooltip();

    initSideButtons();

    this.upSideBtn = sideButtons.get(Constants.Sides.UP);
    this.downSideBtn = sideButtons.get(Constants.Sides.DOWN);
    this.leftSideBtn = sideButtons.get(Constants.Sides.LEFT);
    this.rightSideBtn = sideButtons.get(Constants.Sides.RIGHT);
    this.frontSideBtn = sideButtons.get(Constants.Sides.FRONT);
    this.backSideBtn = sideButtons.get(Constants.Sides.BACK);
    SideConfigTab.addChild(this.autoImportBtn);
    SideConfigTab.addChild(this.upSideBtn);
    SideConfigTab.addChild(this.autoExportBtn);
    SideConfigTab.addChild(this.leftSideBtn);
    SideConfigTab.addChild(this.frontSideBtn);
    SideConfigTab.addChild(this.rightSideBtn);
    SideConfigTab.addChild(this.backSideBtn);
    SideConfigTab.addChild(this.downSideBtn);
  }

  void initSideButtons() {
    // Create buttons for all sides
    for (Constants.Sides side : Constants.Sides.values()) {
      String btnId = side.toString() + "SideBtn";
      SideConfigButton button = new SideConfigButton(
          btnId,
          side,
          this,
          this.blockEntity,
          this.player);
      // Store in our map for easy access
      sideButtons.put(side, button);
    }
  }

  private boolean getAutoImportState() {
    return this.blockEntity.getSideManager().isAutoImportEnabled();
  }

  private boolean getAutoExportState() {
    return this.blockEntity.getSideManager().isAutoExportEnabled();
  }

  private void handleAutoExportButtonClick(Button button) {
    this.blockEntity.getSideManager().autoExportBtnHandler();
    updateAutoExportButtonTooltip();
  }

  private void updateAutoExportButtonTooltip() {
    this.autoExportBtn.setTooltip(Tooltip.create(
        getAutoExportState() ? Component.literal("Disable Auto Export") : Component.literal("Enable Auto Export")));
  }

  private WidgetSprites getRedstoneButtonSprites() {
    int mode = this.redstoneManager.getRedstoneMode().ordinal();
    return new WidgetSprites(
        REDSTONE_MODE_TEXTURES[mode],
        REDSTONE_MODE_TEXTURES[mode], // You might want different textures for disabled/highlighted states
        REDSTONE_MODE_TEXTURES[mode]);
  }

  private void handleRedstoneModeButtonClick(Button button) {
    RedstoneManager.RedstoneMode value = this.redstoneManager.getNextRedstoneMode();
    this.blockEntity.getRedstoneManager().setRedstoneMode(value);
    ModernUtilsMod.LOGGER.debug("Toggled redstoneModeValue to {}", value);
    PacketDistributor.sendToServer(new RedstoneModePayload(value.ordinal(), this.blockEntity.getBlockPos()));
    updateRedstoneButtonTooltip();
  }

  private void handleAutoImportButtonClick(Button button) {
    this.blockEntity.getSideManager().autoImportBtnHandler();
    updateAutoImportButtonTooltip();
  }

  private void updateAutoImportButtonTooltip() {
    this.autoImportBtn.setTooltip(Tooltip.create(
        getAutoImportState() ? Component.literal("Disable Auto Import") : Component.literal("Enable Auto Import")));
  }

  private void updateRedstoneButtonTooltip() {
    RedstoneManager.RedstoneMode redstoneModeValue = this.redstoneManager.getRedstoneMode();
    this.redstoneModeButton.setTooltip(Tooltip.create(Component.literal(
        String.format("Redstone Mode [%s]", REDSTONE_MODE_MAP.get(redstoneModeValue.ordinal())))));
  }

  private void handleButtonClick(Button button) {
    var value = this.blockEntity.toggleRemainItemValue();
    ModernUtilsMod.LOGGER.debug("Toggled remainItemToggleValue to {}", value);
    PacketDistributor.sendToServer(new RemainItemTogglePayload(value, this.blockEntity.getBlockPos()));
    var remainItemToggleValue = this.blockEntity.getRemainItemToggleDisplayValue();
    this.remainingToggleBtn.setTooltip(
        Tooltip.create(
            Component.literal(
                String.format("Remaining Items [%s]", remainItemToggleValue))));
  }

  private void renderScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
  }

  private void renderMyLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.drawString(this.font, Component.literal("Input"), this.leftPos + 8, this.topPos + 80, 0x404040, false);
    guiGraphics.drawString(this.font, Component.literal("Output"), this.leftPos + 8, this.topPos + 112, 0x404040,
        false);
  }

  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    this.renderScreen(guiGraphics, mouseX, mouseY, partialTick);
  }

  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);

    this.renderProgressBar(guiGraphics, partialTick); // Render the progress bar with partialTick
    this.renderMyLabels(guiGraphics, mouseX, mouseY);
    this.renderTooltip(guiGraphics, mouseX, mouseY);
  }

  @Override
  public void renderSlot(@NotNull GuiGraphics guiGraphics, @NotNull Slot slot) {
    // Define the range of slot indices for addon slots
    int addonStartIndex = MechanicalCrafterMenu.INPUT_SLOTS_COUNT + MechanicalCrafterMenu.OUTPUT_SLOTS_COUNT + 10; // 10 is for crafting slots
    int addonEndIndex = addonStartIndex + this.addonManager.ADDON_SLOTS_COUNT;

    // Check if this slot is an addon slot
    if (slot.index >= addonStartIndex && slot.index < addonEndIndex) {
      // Only render addon slots when both the tab is open and the slot is active
      if (this.addonClientManager.getAddonTab().isOpen() && slot.isActive()) {
        this.addonClientManager.renderAddonSlot(guiGraphics, slot);
        super.renderSlot(guiGraphics, slot);
      }
    }
    // Handle disabled output slots
    else if (slot instanceof OutputSlotItemHandler outputSlot && this.menu.isSlotDisabled(slot.getSlotIndex())) {
      this.renderDisabledSlot(guiGraphics, outputSlot);
      super.renderSlot(guiGraphics, outputSlot);
    }
    // Render other slots normally
    else {
      super.renderSlot(guiGraphics, slot);
    }
  }

  private void renderDisabledSlot(GuiGraphics guiGraphics, OutputSlotItemHandler slot) {
    guiGraphics.blitSprite(DISABLED_SLOT_LOCATION_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
  }

  @Override
  protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {
    // Handle output slots with special behavior
    if (slot instanceof OutputSlotItemHandler && !slot.hasItem() && !this.player.isSpectator()) {
      switch (type) {
        case PICKUP:
          if (this.menu.isSlotDisabled(slot.getSlotIndex())) {
            this.enableSlot(slotId);
          } else if (this.menu.getCarried().isEmpty()) {
            this.disableSlot(slotId);
          }
          break;
        case SWAP:
          ItemStack itemstack = this.player.getInventory().getItem(mouseButton);
          if (this.menu.isSlotDisabled(slot.getSlotIndex()) && !itemstack.isEmpty()) {
            this.enableSlot(slotId);
          }
          break;
        default:
          super.slotClicked(slot, slotId, mouseButton, type);
          break;
      }
      super.slotClicked(slot, slotId, mouseButton, type);
    }
    // Normal slots
    else {
      super.slotClicked(slot, slotId, mouseButton, type);
    }
  }

  private void enableSlot(int slot) {
    this.updateSlotState(slot, true);
  }

  private void disableSlot(int slot) {
    this.updateSlotState(slot, false);
  }

  private void updateSlotState(int slot, boolean state) {
    this.menu.setSlotState(slot, state);
    super.handleSlotStateChanged(slot, this.menu.containerId, state);
    float f = state ? 1.0F : 0.75F;
    this.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, f);
    PacketDistributor.sendToServer(
        new SlotStatePayload(this.menu.getSlot(slot).getSlotIndex(), state, this.menu.getBlockEntity().getBlockPos()));
  }

  /**
   * Renders the crafting progress bar at the top of the GUI.
   * 
   * @param guiGraphics The GuiGraphics context
   * @param partialTick The partial tick time for smooth animation
   */
  private void renderProgressBar(GuiGraphics guiGraphics, float partialTick) {
    // Constants for the progress bar
    final int BAR_HEIGHT = 6;
    final int BORDER_THICKNESS = 1;
    final int BAR_Y_POS = this.topPos - BAR_HEIGHT - 2; // Position above the GUI

    // Get the current actual progress
    float currentProgress = this.menu.getCraftingProgress();

    // If progress decreases (e.g., reset to 0 after crafting), snap immediately
    if (currentProgress < lastProgress) {
      visualProgress = currentProgress;
    } else {
      // Use partialTick for smooth interpolation between frames
      float interpolationSpeed = 0.1f; // Lower value = smoother/slower transition
      visualProgress += (currentProgress - visualProgress) * interpolationSpeed * (1.0f + partialTick);
    }

    // Save the current progress for next frame
    lastProgress = currentProgress;

    // Calculate the pixel width based on the interpolated progress
    int progressWidth = (int) (visualProgress * (this.imageWidth - 2)); // -2 for the borders

    // Draw border (dark gray rectangle)
    guiGraphics.fill(this.leftPos, BAR_Y_POS,
        this.leftPos + this.imageWidth, BAR_Y_POS + BAR_HEIGHT,
        0xFF555555); // Dark gray border

    // Draw background (black background behind the progress)
    guiGraphics.fill(this.leftPos + BORDER_THICKNESS, BAR_Y_POS + BORDER_THICKNESS,
        this.leftPos + this.imageWidth - BORDER_THICKNESS, BAR_Y_POS + BAR_HEIGHT - BORDER_THICKNESS,
        0xFF000000); // Black background

    // Draw progress (blue progress bar)
    if (progressWidth > 0) {
      guiGraphics.fill(this.leftPos + BORDER_THICKNESS, BAR_Y_POS + BORDER_THICKNESS,
          this.leftPos + BORDER_THICKNESS + progressWidth, BAR_Y_POS + BAR_HEIGHT - BORDER_THICKNESS,
          0xFF0088FF); // Blue progress
    }
  }
  
  public AddonClientManager getAddonManager() {
    return this.addonClientManager;
  }
}
