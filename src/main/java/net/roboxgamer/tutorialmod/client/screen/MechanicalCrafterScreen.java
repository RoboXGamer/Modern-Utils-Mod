package net.roboxgamer.tutorialmod.client.screen;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import net.roboxgamer.tutorialmod.network.GhostSlotTransferPayload;
import net.roboxgamer.tutorialmod.network.RemainItemTogglePayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MechanicalCrafterScreen extends AbstractContainerScreen<MechanicalCrafterMenu> {
  public static final IGhostIngredientHandler<MechanicalCrafterScreen> GHOST_INGREDIENT_HANDLER = new IGhostIngredientHandler<MechanicalCrafterScreen>() {
    @Override
    public <I> @NotNull List<Target<I>> getTargetsTyped(@NotNull MechanicalCrafterScreen gui, @NotNull ITypedIngredient<I> ingredient, boolean doStart) {
      List<Target<I>> targets = new ArrayList<>();
      
      // Loop through crafting grid slots (indexes 1 to 9)
      for (int slotIndex = 1; slotIndex <= 9; slotIndex++) {
        Slot slot = gui.getMenu().getSlot(slotIndex);
        
        // Define the target area for this slot
        Rect2i bounds = new Rect2i(
            gui.getGuiLeft() + slot.x,
            gui.getGuiTop() + slot.y,
            17, 17  // 17x17 for slot size
        );
        
        int finalSlotIndex = slotIndex;
        targets.add(new Target<I>() {
          @Override
          public @NotNull Rect2i getArea() {
            return bounds;
          }
          
          @Override
          public void accept(@NotNull I ingredient) {
            if (ingredient instanceof ItemStack stack) {
              // Set ghost item in the slot (handled server-side)
              ItemStack ghostStack = stack.copy();
              ghostStack.setCount(1);  // Ghost stack has 1 count
              
              // Send packet to the server to update the ghost slot
              PacketDistributor.sendToServer(
                  new GhostSlotTransferPayload(finalSlotIndex, ghostStack, gui.getMenu().getBlockEntity().getBlockPos())
              );
            }
          }
        });
      }
      
      return targets;
    }
    
    @Override
    public void onComplete() {
      // No-op
    }
  };
  
  private static final String location =
      TutorialMod.MODID + ".mechanical_crafter_screen";
  private static final Component TITLE =
      Component.translatable("gui." + location);
  private static final Component BUTTON_TEXT =
      Component.translatable("gui." + location + ".button_text");
  
  private static final ResourceLocation TEXTURE =
      TutorialMod.location("textures/gui/mechanical_crafter_screen.png");
  
  private final BlockPos position;
  private final int imageWidth, imageHeight;
  
  private MechanicalCrafterBlockEntity blockEntity;
  //private int leftPos, topPos;
  
  //Widgets
  private Button button;
  
  
  public MechanicalCrafterScreen(MechanicalCrafterMenu menu, Inventory playerInv, Component title) {
    super(menu,playerInv,title);
    this.position = menu.getBlockEntity().getBlockPos();
    this.imageWidth = 176;
    this.imageHeight = 235;
    this.inventoryLabelY = this.imageHeight - 92;
  }
  
  @Override
  protected void init() {
    super.init();
    
    // Already set in super
    //this.leftPos = this.width / 2 - this.imageWidth / 2;
    //this.topPos = this.height / 2 - this.imageHeight / 2;
    
    if (this.minecraft == null) return;
    Level level = this.minecraft.level;
    if (level == null) return;
    
    BlockEntity be = level.getBlockEntity(this.position);
    if (be instanceof MechanicalCrafterBlockEntity mcbe) {
      this.blockEntity = mcbe;
      
    } else {
      TutorialMod.LOGGER.error("Mechanical Crafter Screen: BlockEntity is not a MechanicalCrafterBlockEntity!");
      return;
    }
    
    //  Widgets to add!
    
    this.button = addRenderableWidget(
        new ImageButton(this.leftPos + this.imageWidth - 56,this.topPos + 34,20,20,
                        new WidgetSprites(
            TutorialMod.location("toggle_remain_btn"),
            TutorialMod.location("toggle_remain_btn_disabled"),
            TutorialMod.location("toggle_remain_btn_highlighted")
    ),this::handleButtonClick,BUTTON_TEXT)
            );
    this.button.setTooltip(Tooltip.create(Component.literal("Toggles the input/output of the remaining items")));
  }
  
  private void handleButtonClick(Button button) {
    var value = this.blockEntity.toggleRemainItemValue();
    TutorialMod.LOGGER.debug("Toggled remainItemToggleValue to {}", value);
    PacketDistributor.sendToServer(new RemainItemTogglePayload(value, this.blockEntity.getBlockPos()));
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

  }
  
  
  public @Nullable IGuiProperties getGuiProperties() {
    var minecraft = Minecraft.getInstance();
    return new IGuiProperties() {
      @Override
      public @NotNull Class<? extends Screen> screenClass() {
        return MechanicalCrafterScreen.class;
      }
      
      @Override
      public int guiLeft() {
        return leftPos;
      }
      
      @Override
      public int guiTop() {
        return topPos;
      }
      
      @Override
      public int guiXSize() {
        return imageWidth;
      }
      
      @Override
      public int guiYSize() {
        return imageHeight;
      }
      
      @Override
      public int screenWidth() {
        return minecraft.getWindow().getScreenWidth();
      }
      
      @Override
      public int screenHeight() {
        return minecraft.getWindow().getScreenHeight();
      }
    };
  }
}
