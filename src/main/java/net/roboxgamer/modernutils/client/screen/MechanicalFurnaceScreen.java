package net.roboxgamer.modernutils.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.roboxgamer.modernutils.ModernUtilsMod;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalFurnaceBlockEntity;
import net.roboxgamer.modernutils.client.screen.widgets.FuelGaugeWidget;
import net.roboxgamer.modernutils.client.screen.widgets.ProgressArrowWidget;
import net.roboxgamer.modernutils.client.screen.widgets.RedstoneButton;
import net.roboxgamer.modernutils.client.screen.widgets.SlotWidget;
import net.roboxgamer.modernutils.item.ModItems;
import net.roboxgamer.modernutils.menu.MechanicalFurnaceMenu;
import net.roboxgamer.modernutils.util.AddonClientManager;
import net.roboxgamer.modernutils.util.AddonManager;
import net.roboxgamer.modernutils.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MechanicalFurnaceScreen extends AbstractContainerScreen<MechanicalFurnaceMenu> {
  private static final int TITLE_HEIGHT = 22;
  private static final int CONTAINER_PADDING = 4;
  private static final int SLOT_SPACING = 2;
  private static final int SLOT_SIZE = 18;
  
  private final Player player;
  private final BlockPos position;
  
  private List<ProgressArrowWidget> progressArrows = new ArrayList<>();
  public AnimatedTab sideConfigTab;
  private ExtendedButton sideConfigBtn;
  private ExtendedButton autoImportBtn;
  private ExtendedButton autoExportBtn;
  private ExtendedButton upSideBtn;
  private ExtendedButton downSideBtn;
  private ExtendedButton leftSideBtn;
  private ExtendedButton rightSideBtn;
  private ExtendedButton backSideBtn;
  private ExtendedButton frontSideBtn;
  private Map<Constants.Sides, SideConfigButton> sideButtons = new HashMap<>();
  private MechanicalFurnaceBlockEntity blockEntity;
  private AddonManager addonManager;
  private AddonClientManager addonClientManager;
  
  
  public MechanicalFurnaceScreen(MechanicalFurnaceMenu menu, Inventory playerInv, Component title) {
    super(menu, playerInv, title);
    this.position = menu.getBlockEntity().getBlockPos();
    this.imageWidth = 190;
    this.imageHeight = 182;
    this.titleLabelY = 8;
    this.player = playerInv.player;
  }
  
  @Override
  protected void init() {
    try {
      super.init();
      Level level = this.player.level();
      BlockEntity blockEntity = level.getBlockEntity(this.position);
      
      if (blockEntity instanceof MechanicalFurnaceBlockEntity be) {
        this.blockEntity = be;
        this.addonManager = this.blockEntity.getAddonManager();
        this.addonClientManager = new AddonClientManager(this.addonManager);
      } else {
        return;
      }
      
      this.inventoryLabelY = this.imageHeight - 96;
      
      // Create redstone button
      this.addRenderableWidget(new RedstoneButton(
          leftPos + imageWidth - 16 - CONTAINER_PADDING, topPos + CONTAINER_PADDING,
          this,
          this.blockEntity
      ));
      
      // Create progress arrows with bounds checking
      int maxArrows = Math.min(8, menu.getSlots());  // Ensure we don't exceed available slots
      for (int i = 0; i < maxArrows; i++) {
        int arrowX = leftPos + 2 + SLOT_SIZE + CONTAINER_PADDING * 2 + (i * (SLOT_SIZE + SLOT_SPACING));
        int arrowY = topPos + TITLE_HEIGHT + CONTAINER_PADDING + SLOT_SIZE + CONTAINER_PADDING / 2;
        final int slot = i;
        progressArrows.add(
            this.addRenderableWidget(new ProgressArrowWidget(arrowX, arrowY, () -> menu.getScaledProgress(slot))));
      }
      // Create fuel gauge
      this.addRenderableWidget(new FuelGaugeWidget(
          leftPos + CONTAINER_PADDING + SLOT_SPACING,
          topPos + TITLE_HEIGHT + CONTAINER_PADDING + SLOT_SIZE * 2 + SLOT_SPACING,
          new FuelGaugeWidget.FuelSupplier() {
            @Override
            public int getFuelLevel() {
              return menu.getFuelScaled();
            }
            
            @Override
            public boolean hasFuel() {
              return menu.hasFuel();
            }
          }));
      
      // Create side config tab
      this.sideConfigTab = new AnimatedTab(
          92, 92, Component.empty(), ExtendedButton.WidgetPosition.BOTTOM_LEFT
      );
      addRenderableWidget(sideConfigTab);
      
      // Create addon tab and button using the client manager
      this.addonClientManager.createAddonTab(this.player, this);
      addRenderableWidget(this.addonClientManager.getAddonTab());
      addRenderableWidget(this.addonClientManager.getAddonConfigButton());
      
      // Create side config button
      this.sideConfigBtn = new ExtendedButton(
          "Config_Btn",
          24, 24,
          Component.literal("Settings"),
          true,
          ExtendedButton.WidgetPosition.BOTTOM_LEFT,
          (button, clickAction, mouseX, mouseY) -> {
            sideConfigTab.toggleOpen();
          },
          this.player
      ) {
        @Override
        public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
          float scale = 1;
          float offset = (extendedButton.getWidth() - (16 * scale)) / 2;
          
          guiGraphics.pose().pushPose();
          guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
          guiGraphics.pose().scale(scale, scale, 1);
          guiGraphics.renderFakeItem(ModItems.EXAMPLE_ITEM.get().getDefaultInstance(),
                                     0,
                                     0
          );
          guiGraphics.pose().popPose();
        }
      };
      addRenderableWidget(this.sideConfigBtn);
      
      // Create auto import/export buttons
      this.autoImportBtn = new ExtendedButton(
          "AutoImportBtn",
          24, 24,
          Component.empty(),
          true,
          ExtendedButton.WidgetPosition.NONE,
          (button, clickAction, mouseX, mouseY) -> this.handleAutoImportButtonClick(button),
          this.player
      ) {
        @Override
        public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
          boolean state = getAutoImportState();
          var sprite = state ?
              ModernUtilsMod.location("auto_import_on") :
              ModernUtilsMod.location("auto_import_off");
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
          this.player
      ) {
        @Override
        public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
          boolean state = getAutoExportState();
          var sprite = state ?
              ModernUtilsMod.location("auto_export_on") :
              ModernUtilsMod.location("auto_export_off");
          guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
        }
      };
      updateAutoExportButtonTooltip();
      
      // Initialize side buttons
      initSideButtons();
      
      // Only add buttons if they were successfully created
      if (!sideButtons.isEmpty()) {
        this.upSideBtn = sideButtons.get(Constants.Sides.UP);
        this.downSideBtn = sideButtons.get(Constants.Sides.DOWN);
        this.leftSideBtn = sideButtons.get(Constants.Sides.LEFT);
        this.rightSideBtn = sideButtons.get(Constants.Sides.RIGHT);
        this.frontSideBtn = sideButtons.get(Constants.Sides.FRONT);
        this.backSideBtn = sideButtons.get(Constants.Sides.BACK);
        
        // Add buttons to side config tab only if they exist
        if (sideConfigTab != null) {
          sideConfigTab.addChild(this.autoImportBtn);
          sideConfigTab.addChild(this.upSideBtn);
          sideConfigTab.addChild(this.autoExportBtn);
          sideConfigTab.addChild(this.leftSideBtn);
          sideConfigTab.addChild(this.frontSideBtn);
          sideConfigTab.addChild(this.rightSideBtn);
          sideConfigTab.addChild(this.backSideBtn);
          sideConfigTab.addChild(this.downSideBtn);
        }
      }
      int slotX;
      int slotY;
      
      for (int i = 0; i < 8; i++) {
        // Create input slots
        slotX = leftPos + SLOT_SIZE + CONTAINER_PADDING * 2 + (i * (SLOT_SIZE + SLOT_SPACING));
        slotY = topPos + TITLE_HEIGHT + CONTAINER_PADDING;
        this.addRenderableWidget(new SlotWidget(slotX, slotY));
        
        // Create output slots
        int outputSlotY = slotY + SLOT_SIZE + SLOT_SPACING + 14 + SLOT_SPACING;
        this.addRenderableWidget(new SlotWidget(slotX, outputSlotY));
      }
      
      // Create fuel slot
      this.addRenderableWidget(new SlotWidget(leftPos + CONTAINER_PADDING + SLOT_SPACING,
                                              topPos + TITLE_HEIGHT + CONTAINER_PADDING + SLOT_SIZE));
      
      int tW = SLOT_SIZE * 9;
      int rW = imageWidth - tW - CONTAINER_PADDING * 2;
      int s = rW / 2;
      int tH = SLOT_SIZE * 3;
      // Create player inventory slots
      for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 9; col++) {
          int x = leftPos + CONTAINER_PADDING + s + col * (SLOT_SIZE);
          int y = topPos + inventoryLabelY + 12 + row * 18;
          this.addRenderableWidget(new SlotWidget(x, y));
        }
      }
      
      // Create hotbar slots
      for (int col = 0; col < 9; col++) {
        int x = leftPos + CONTAINER_PADDING + s + col * (SLOT_SIZE);
        int y = topPos + inventoryLabelY + 12 + tH + SLOT_SPACING * 2;
        this.addRenderableWidget(new SlotWidget(x, y));
      }
      
    } catch (Exception e) {
      ModernUtilsMod.LOGGER.error("Error during screen initialization: " + e.getMessage());
    }
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
          this.player
      );
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
  
  private void handleAutoImportButtonClick(net.minecraft.client.gui.components.Button button) {
    this.blockEntity.getSideManager().autoImportBtnHandler();
    updateAutoImportButtonTooltip();
  }
  
  private void handleAutoExportButtonClick(net.minecraft.client.gui.components.Button button) {
    this.blockEntity.getSideManager().autoExportBtnHandler();
    updateAutoExportButtonTooltip();
  }
  
  private void updateAutoImportButtonTooltip() {
    this.autoImportBtn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
        getAutoImportState() ? Component.literal("Disable Auto Import") : Component.literal("Enable Auto Import")
    ));
  }
  
  private void updateAutoExportButtonTooltip() {
    this.autoExportBtn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
        getAutoExportState() ? Component.literal("Disable Auto Export") : Component.literal("Enable Auto Export")
    ));
  }
  
  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    int x = this.leftPos;
    int y = this.topPos;
    
    // Draw main background
    guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, Constants.Colors.BACKGROUND_MAIN);
    
    // Draw furnace area background
    guiGraphics.fill(x + CONTAINER_PADDING, y + TITLE_HEIGHT, x + imageWidth - CONTAINER_PADDING,
                     y + imageHeight - CONTAINER_PADDING, Constants.Colors.BACKGROUND_SECONDARY);
  }
  
  @Override
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);
    renderTooltip(guiGraphics, mouseX, mouseY);
  }
  
  @Override
  protected void renderSlot(@NotNull GuiGraphics guiGraphics, @NotNull Slot slot) {
    // Define the range of slot indices for addon slots
    int addonStartIndex = MechanicalFurnaceMenu.SLOT_COUNT;
    int addonEndIndex = addonStartIndex + this.addonManager.ADDON_SLOTS_COUNT;
    
    // Check if this slot is an addon slot
    if (slot.index >= addonStartIndex && slot.index < addonEndIndex) {
      // Only render addon slots when both the tab is open and the slot is active
      if (this.addonClientManager.getAddonTab().isOpen() && slot.isActive()) {
        this.addonClientManager.renderAddonSlot(guiGraphics, slot);
        super.renderSlot(guiGraphics, slot);
      }
    } else {
      super.renderSlot(guiGraphics, slot);
    }
  }
  
  public AddonClientManager getAddonManager() {
    return this.addonClientManager;
  }
}