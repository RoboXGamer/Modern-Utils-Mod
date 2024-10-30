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
import net.roboxgamer.modernutils.block.ModBlocks;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.modernutils.item.ModItems;
import net.roboxgamer.modernutils.menu.MechanicalCrafterMenu;
import net.roboxgamer.modernutils.menu.OutputSlotItemHandler;
import net.roboxgamer.modernutils.network.RedstoneModePayload;
import net.roboxgamer.modernutils.network.RemainItemTogglePayload;
import net.roboxgamer.modernutils.network.SlotStatePayload;
import net.roboxgamer.modernutils.util.Constants;
import net.roboxgamer.modernutils.util.RedstoneManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static net.roboxgamer.modernutils.util.RedstoneManager.REDSTONE_MODE_MAP;


public class MechanicalCrafterScreen extends AbstractContainerScreen<MechanicalCrafterMenu> {
  private static final String location =
      ModernUtilsMod.MODID + ".mechanical_crafter_screen";
  private static final Component TITLE =
      Component.translatable("gui." + location);
  private static final Component BUTTON_TEXT =
      Component.translatable("gui." + location + ".button_text");
  
  private static final ResourceLocation TEXTURE =
      ModernUtilsMod.location("textures/gui/mechanical_crafter_screen.png");
  
  private static final ResourceLocation[] REDSTONE_MODE_TEXTURES = {
      ModernUtilsMod.location("redstone_mode_0"),
      ModernUtilsMod.location("redstone_mode_1"),
      ModernUtilsMod.location("redstone_mode_2")
  };
  private static final ResourceLocation DISABLED_SLOT_LOCATION_SPRITE = ResourceLocation.withDefaultNamespace("container/crafter/disabled_slot");
  private static final Component DISABLED_SLOT_TOOLTIP = Component.translatable("gui.togglable_slot");
  
  private final BlockPos position;
  //public final int imageWidth, imageHeight;
  private final Player player;
  
  private MechanicalCrafterBlockEntity blockEntity;
  private RedstoneManager redstoneManager;
  //private int leftPos, topPos;
  
  //Widgets
  private Button button;
  private ImageButton redstoneModeButton;
  private AnimatedTab SideConfigTab;
  private Button autoImportBtn;
  private ExtendedButton sideConfigBtn;
  private Button autoExportBtn;
  private ExtendedButton upSideBtn;
  private ExtendedButton downSideBtn;
  private ExtendedButton leftSideBtn;
  private ExtendedButton rightSideBtn;
  private ExtendedButton backSideBtn;
  private ExtendedButton frontSideBtn;
  private ArrayList<ExtendedButton> sideBtns;
  
  
  public MechanicalCrafterScreen(MechanicalCrafterMenu menu, Inventory playerInv, Component title) {
    super(menu,playerInv,title);
    this.position = menu.getBlockEntity().getBlockPos();
    this.imageWidth = 176;
    this.imageHeight = 236;
    this.inventoryLabelY = this.imageHeight - 92;
    this.player = playerInv.player;
    this.titleLabelY = 10000;
  }
  
  @Override
  protected void init() {
    this.leftPos = this.width / 2 - this.imageWidth / 2;
    this.topPos = this.height / 2 - this.imageHeight / 2;
    
    if (this.minecraft == null) return;
    Level level = this.minecraft.level;
    if (level == null) return;
    
    BlockEntity be = level.getBlockEntity(this.position);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      this.blockEntity = mcbe;
      this.redstoneManager = this.blockEntity.getRedstoneManager();
      
    } else {
      ModernUtilsMod.LOGGER.error("Mechanical Crafter Screen: BlockEntity is not a MechanicalCrafterBlockEntity!");
      return;
    }
    
    //  Widgets to add!
    
    this.button = addRenderableWidget(
        new ImageButton(this.leftPos + this.imageWidth - 56,this.topPos + 34,20,20,
                        new WidgetSprites(
                            ModernUtilsMod.location("toggle_remain_btn"),
                            ModernUtilsMod.location("toggle_remain_btn_disabled"),
                            ModernUtilsMod.location("toggle_remain_btn_highlighted")
    ),this::handleButtonClick,BUTTON_TEXT)
            );
    var remainItemToggleValue = this.blockEntity.getRemainItemToggleDisplayValue();
    this.button.setTooltip(
        Tooltip.create(
            Component.literal(
                String.format("Remaining Items [%s]", remainItemToggleValue)
            )
        )
    );
    
    this.redstoneModeButton = addRenderableWidget(
        new ImageButton(this.leftPos + this.imageWidth - 56,this.topPos + 64,20,20,
                        new WidgetSprites(
                            ModernUtilsMod.location("redstone_mode_btn"),
                            ModernUtilsMod.location("redstone_mode_btn_disabled"),
                            ModernUtilsMod.location("redstone_mode_btn_highlighted")
                        ),this::handleRedstoneModeButtonClick,BUTTON_TEXT){
          @Override
          public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            var sprites = getRedstoneButtonSprites();
            var resourcelocation = sprites.get(this.isActive(), this.isHoveredOrFocused());
            guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
          }
        }
    );
    updateRedstoneButtonTooltip();
    
    this.SideConfigTab = new AnimatedTab(
        92, 92, Component.empty(), ExtendedButton.WidgetPosition.BOTTOM_LEFT
    );
    addRenderableWidget(SideConfigTab);
    
    this.sideConfigBtn = new ExtendedButton(
        "Config_Btn",
        24, 24,
        Component.literal("Settings"),
        true,  // Optional icon
        ExtendedButton.WidgetPosition.BOTTOM_LEFT,
        button -> {
          //TutorialMod.LOGGER.debug("You pressed: {}", button.getMessage().getString());
          SideConfigTab.toggleOpen();
        }
    ){
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
        float scale = 1;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering
        
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
    
    this.autoImportBtn = Button.builder(Component.empty(), this::handleAutoImportButtonClick).build();
    updateAutoImportButtonTooltip();
    SideConfigTab.addChild(this.autoImportBtn);
    
    this.upSideBtn = new ExtendedButton(
        "UpSideBtn",
        24, 24,
        Component.literal("Up Side"),
        true,  // Optional icon
        ExtendedButton.WidgetPosition.NONE,
        (button) -> this.blockEntity.handleSideBtnClick(Constants.Sides.UP, button)
    ){
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
        float scale = 1.15f;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(
            ModBlocks.MECHANICAL_CRAFTER_BLOCK.get().asItem().getDefaultInstance(),
            0,
            0
        );
        guiGraphics.pose().popPose();
      }
    };
    SideConfigTab.addChild(this.upSideBtn);
    
    this.autoExportBtn = Button.builder(Component.empty(), this::handleAutoExportButtonClick).build();
    updateAutoExportButtonTooltip();
    SideConfigTab.addChild(this.autoExportBtn);
    
    this.leftSideBtn = new ExtendedButton(
        "LeftSideBtn",
        24, 24,
        Component.literal("Left Side"),
        true,  // Optional icon
        ExtendedButton.WidgetPosition.NONE,
        (button) -> this.blockEntity.handleSideBtnClick(Constants.Sides.LEFT, button)
    ){
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
        float scale = 1.15f;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(
            ModBlocks.MECHANICAL_CRAFTER_BLOCK.get().asItem().getDefaultInstance(),
            0,
            0
        );
        guiGraphics.pose().popPose();
      }
    };
    SideConfigTab.addChild(this.leftSideBtn);
    
    this.frontSideBtn = new ExtendedButton(
        "FrontSideBtn",
        24, 24,
        Component.literal("Front Side"),
        true,  // Optional icon
        ExtendedButton.WidgetPosition.NONE,
        (button) -> this.blockEntity.handleSideBtnClick(Constants.Sides.FRONT, button)
    ) {
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
        float scale = 1.15f;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(
            ModBlocks.MECHANICAL_CRAFTER_BLOCK.get().asItem().getDefaultInstance(),
            0,
            0
        );
        guiGraphics.pose().popPose();
      }
    };
    SideConfigTab.addChild(this.frontSideBtn);
    
    this.rightSideBtn = new ExtendedButton(
        "RightSideBtn",
        24, 24,
        Component.literal("Right Side"),
        true,  // Optional icon
        ExtendedButton.WidgetPosition.NONE,
        (button) -> this.blockEntity.handleSideBtnClick(Constants.Sides.RIGHT, button)
    ){
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
        float scale = 1.15f;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(
            ModBlocks.MECHANICAL_CRAFTER_BLOCK.get().asItem().getDefaultInstance(),
            0,
            0
        );
        guiGraphics.pose().popPose();
      }
    };
    SideConfigTab.addChild(this.rightSideBtn);
    
    this.backSideBtn = new ExtendedButton(
        "BackSideBtn",
        24, 24,
        Component.literal("Back Side"),
        true,  // Optional icon
        ExtendedButton.WidgetPosition.NONE,
        (button) -> this.blockEntity.handleSideBtnClick(Constants.Sides.BACK, button)
    ){
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
        float scale = 1.15f;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(
            ModBlocks.MECHANICAL_CRAFTER_BLOCK.get().asItem().getDefaultInstance(),
            0,
            0
        );
        guiGraphics.pose().popPose();
      }
    };
    SideConfigTab.addChild(this.backSideBtn);
    
    this.downSideBtn = new ExtendedButton(
        "DownSideBtn",
        24, 24,
        Component.literal("Down Side"),
        true,  // Optional icon
        ExtendedButton.WidgetPosition.NONE,
        (button) -> this.blockEntity.handleSideBtnClick(Constants.Sides.DOWN, button)
    ){
      @Override
      public void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, ExtendedButton extendedButton) {
        float scale = 1.15f;
        float offset = (extendedButton.getWidth() - (16 * scale)) / 2; // Calculate offset for centering
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(extendedButton.getX() + offset, extendedButton.getY() + offset, 0);
        guiGraphics.pose().scale(scale, scale, 1);
        guiGraphics.renderFakeItem(
            ModBlocks.MECHANICAL_CRAFTER_BLOCK.get().asItem().getDefaultInstance(),
            0,
            0
        );
        guiGraphics.pose().popPose();
      }
    };
    SideConfigTab.addChild(this.downSideBtn);
    
    this.sideBtns = new ArrayList<>();
    this.sideBtns.add(this.upSideBtn);
    this.sideBtns.add(this.downSideBtn);
    this.sideBtns.add(this.leftSideBtn);
    this.sideBtns.add(this.rightSideBtn);
    this.sideBtns.add(this.backSideBtn);
    this.sideBtns.add(this.frontSideBtn);
    
    updateSideBtnsTooltip();
  }
  
  private void updateSideBtnsTooltip() {
    for (Constants.Sides side : Constants.Sides.values()) {
      var sideState = this.blockEntity.getSideState(side);
      this.sideBtns.get(side.ordinal()).setMessage(Component.literal(String.format("%s Side,State: %s",side,sideState)));
    }
  }
  
  
  private void handleAutoExportButtonClick(Button button) {
    this.blockEntity.autoExportBtnHandler();
    updateAutoExportButtonTooltip();
  }
  
  private void updateAutoExportButtonTooltip() {
    this.autoExportBtn.setTooltip(Tooltip.create(blockEntity.isAutoExportEnabled() ? Component.literal("Disable Auto Export") : Component.literal("Enable Auto Export")));
  }
  
  private WidgetSprites getRedstoneButtonSprites() {
    int mode = this.redstoneManager.getRedstoneMode().ordinal();
    return new WidgetSprites(
        REDSTONE_MODE_TEXTURES[mode],
        REDSTONE_MODE_TEXTURES[mode], // You might want different textures for disabled/highlighted states
        REDSTONE_MODE_TEXTURES[mode]
    );
  }
  
  private void handleRedstoneModeButtonClick(Button button) {
    RedstoneManager.RedstoneMode value = this.redstoneManager.getNextRedstoneMode();
    this.blockEntity.getRedstoneManager().setRedstoneMode(value);
    ModernUtilsMod.LOGGER.debug("Toggled redstoneModeValue to {}", value);
    PacketDistributor.sendToServer(new RedstoneModePayload(value.ordinal(), this.blockEntity.getBlockPos()));
    updateRedstoneButtonTooltip();
  }
  
  private void handleAutoImportButtonClick(Button button) {
    this.blockEntity.autoImportBtnHandler();
    updateAutoImportButtonTooltip();
  }
  
  private void updateAutoImportButtonTooltip() {
    this.autoImportBtn.setTooltip(Tooltip.create(blockEntity.isAutoImportEnabled() ? Component.literal("Disable Auto Import") : Component.literal("Enable Auto Import")));
  }
  
  private void updateRedstoneButtonTooltip() {
    RedstoneManager.RedstoneMode redstoneModeValue = this.redstoneManager.getRedstoneMode();
    this.redstoneModeButton.setTooltip(Tooltip.create(Component.literal(
        String.format("Redstone Mode [%s]", REDSTONE_MODE_MAP.get(redstoneModeValue.ordinal()))
    )));
  }
  
  private void handleButtonClick(Button button) {
    var value = this.blockEntity.toggleRemainItemValue();
    ModernUtilsMod.LOGGER.debug("Toggled remainItemToggleValue to {}", value);
    PacketDistributor.sendToServer(new RemainItemTogglePayload(value, this.blockEntity.getBlockPos()));
    var remainItemToggleValue = this.blockEntity.getRemainItemToggleDisplayValue();
    this.button.setTooltip(
        Tooltip.create(
            Component.literal(
                String.format("Remaining Items [%s]", remainItemToggleValue)
            )
        )
    );
  }
  
  private void renderScreen(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    guiGraphics.drawString(this.font, TITLE, this.leftPos + 8, this.topPos + 6, 0x404040, false);
  }
  
  private void renderWidgets(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    for (Renderable renderable : this.renderables) {
      renderable.render(guiGraphics, mouseX, mouseY, partialTick);
    }
  }
  
  private void renderMyLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    guiGraphics.drawString(this.font, Component.literal("Input"), this.leftPos + 8, this.topPos + 80, 0x404040, false);
    guiGraphics.drawString(this.font, Component.literal("Output"), this.leftPos + 8, this.topPos + 112, 0x404040, false);
  }
  
  @Override
  protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    this.renderScreen(guiGraphics, mouseX, mouseY, partialTick);
  }
  
  public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);
    //renderTransparentBackground(guiGraphics);
    //this.renderScreen(guiGraphics, mouseX, mouseY, partialTick);
    //this.renderWidgets(guiGraphics, mouseX, mouseY, partialTick);

    this.renderMyLabels(guiGraphics, mouseX, mouseY);
    this.renderTooltip(guiGraphics, mouseX, mouseY);
    
    //TutorialMod.LOGGER.debug("Mouse X: {}, Mouse Y: {}", mouseX, mouseY);
    
    if (this.hoveredSlot instanceof OutputSlotItemHandler
        && !this.menu.isSlotDisabled(this.hoveredSlot.getSlotIndex())
        && this.menu.getCarried().isEmpty()
        && !this.hoveredSlot.hasItem()
        && !this.player.isSpectator()) {
      guiGraphics.renderTooltip(this.font, DISABLED_SLOT_TOOLTIP, mouseX, mouseY);
    }
  }
  
  @Override
  public void renderSlot(@NotNull GuiGraphics guiGraphics, @NotNull Slot slot) {
    if (slot instanceof OutputSlotItemHandler outputSlot && this.menu.isSlotDisabled(slot.getSlotIndex())) {
      this.renderDisabledSlot(guiGraphics, outputSlot);
      return;
    }
    
    super.renderSlot(guiGraphics, slot);
  }
  
  private void renderDisabledSlot(GuiGraphics guiGraphics, OutputSlotItemHandler slot) {
    guiGraphics.blitSprite(DISABLED_SLOT_LOCATION_SPRITE, slot.x - 1, slot.y - 1, 18, 18);
  }
  
  @Override
  protected void slotClicked(@NotNull Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {
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
      }
    }
    super.slotClicked(slot, slotId, mouseButton, type);
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
    PacketDistributor.sendToServer(new SlotStatePayload(this.menu.getSlot(slot).getSlotIndex(),state,this.menu.getBlockEntity().getBlockPos()));
  }
}
