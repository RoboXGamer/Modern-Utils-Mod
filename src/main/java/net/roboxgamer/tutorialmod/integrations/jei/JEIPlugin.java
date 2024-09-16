package net.roboxgamer.tutorialmod.integrations.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.network.PacketDistributor;
import net.roboxgamer.tutorialmod.TutorialMod;
import net.roboxgamer.tutorialmod.block.ModBlocks;
import net.roboxgamer.tutorialmod.client.screen.MechanicalCrafterScreen;
import net.roboxgamer.tutorialmod.menu.CraftingGhostSlotItemHandler;
import net.roboxgamer.tutorialmod.menu.MechanicalCrafterMenu;
import net.roboxgamer.tutorialmod.menu.ModMenuTypes;
import net.roboxgamer.tutorialmod.network.GhostSlotTransferPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  private static final ResourceLocation UID = TutorialMod.location("jei_plugin");
  
  public JEIPlugin() {}
  
  @Override
  public @NotNull ResourceLocation getPluginUid() {
    return UID;
  }
  
  @Override
  public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
    IModPlugin.super.registerRecipeCatalysts(registration);
    registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_CRAFTER_BLOCK.get()), RecipeTypes.CRAFTING);
  }
  
  @Override
  public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
    IModPlugin.super.registerRecipeTransferHandlers(registration);
    registration.addRecipeTransferHandler(
        new MechanicalCrafterRecipeTransferHandler(registration.getTransferHelper())
        ,RecipeTypes.CRAFTING);
    //registration.addRecipeTransferHandler(MechanicalCrafterMenu.class, ModMenuTypes.MECHANICAL_CRAFTER_MENU.get(),RecipeTypes.CRAFTING, 1, 9, 28, 36);
  }
  
  @Override
  public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
    IModPlugin.super.registerGuiHandlers(registration);
    registration.addGhostIngredientHandler(
        MechanicalCrafterScreen.class,MechanicalCrafterRecipeTransferHandler.GHOST_INGREDIENT_HANDLER
    );
  }
  
  public static class MechanicalCrafterRecipeTransferHandler implements IRecipeTransferHandler<MechanicalCrafterMenu, RecipeHolder<CraftingRecipe>> {
    private final IRecipeTransferHandlerHelper handlerHelper;
    
    public static final IGhostIngredientHandler<MechanicalCrafterScreen> GHOST_INGREDIENT_HANDLER = new IGhostIngredientHandler<>() {
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
          targets.add(new Target<>() {
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
                    new GhostSlotTransferPayload(finalSlotIndex, ghostStack,
                                                 gui.getMenu().getBlockEntity().getBlockPos())
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
    
    public MechanicalCrafterRecipeTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
      this.handlerHelper = handlerHelper;
    }
    
    @Override
    public @NotNull Class<? extends MechanicalCrafterMenu> getContainerClass() {
      return MechanicalCrafterMenu.class;
    }
    
    @Override
    public @NotNull Optional<MenuType<MechanicalCrafterMenu>> getMenuType() {
      return Optional.of(ModMenuTypes.MECHANICAL_CRAFTER_MENU.get());
    }
    
    @Override
    public @NotNull RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
      return RecipeTypes.CRAFTING;
    }
    
    @Override
    public @Nullable IRecipeTransferError transferRecipe(@NotNull MechanicalCrafterMenu container,
                                                         @NotNull RecipeHolder<CraftingRecipe> recipe,
                                                         @NotNull IRecipeSlotsView recipeSlots,
                                                         @NotNull Player player,
                                                         boolean maxTransfer,
                                                         boolean doTransfer) {
      CraftingRecipe craftingRecipe = recipe.value();
      
      NonNullList<Ingredient> ingredients = craftingRecipe.getIngredients();
      
      if (ingredients.size() > 9) {
        return handlerHelper.createUserErrorWithTooltip(
            Component.literal("The recipe is too large for the available crafting slots."));
      }
      
      if (doTransfer) {
        int[] slotMap;
        if (craftingRecipe instanceof ShapedRecipe shapedRecipe) {
          slotMap = getSlotMap((shapedRecipe.getWidth()),(shapedRecipe.getHeight()));
        }
        else {
          slotMap = getSlotMap();
        }
        clearGhostSlots(container);  // Clear existing ghost items before transfer
        
        for (int i = 0; i < ingredients.size(); i++) {
          Ingredient ingredient = ingredients.get(i);
          ItemStack[] ingredientItems = ingredient.getItems();
          if (ingredientItems.length > 0) {
            ItemStack matchingStack = ingredientItems[0].copy();
            matchingStack.setCount(1);  // Set count to 1 for ghost slots
            Slot ghostSlot = container.getSlot(slotMap[i]);
            if (ghostSlot instanceof CraftingGhostSlotItemHandler) {
              ghostSlot.set(matchingStack);  // Place the item in the ghost slot
              // Send packet to server to synchronize the ghost slot
              PacketDistributor.sendToServer(
                  new GhostSlotTransferPayload(slotMap[i], matchingStack, container.getBlockEntity().getBlockPos())
              );
            }
          }
        }
      }
      
      return null;  // Success, no error
    }
    
    private static int @NotNull [] getSlotMap() {
      return getSlotMap(0,0); // Shapeless recipe
    }
    
    private static int @NotNull [] getSlotMap(int width,int height) {
      if (width == 0 && height == 0) {
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
      }
      if (width == 2 && height == 2) {
        return new int[]{1, 2, 4, 5};
      } else if (width == 3 && height == 3) {
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
      } else if (width == 1) {
        return new int[]{2,5,8};
      }
      else if (height == 1) {
        return new int[]{4,5,6};
      }
      return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};  // Default to 3x3 recipe
    }
    
    private void clearGhostSlots(MechanicalCrafterMenu container) {
      for (int i = 1; i <= 9; i++) {
        Slot ghostSlot = container.getSlot(i);
        if (ghostSlot instanceof CraftingGhostSlotItemHandler) {
          ghostSlot.set(ItemStack.EMPTY);
          PacketDistributor.sendToServer(
              new GhostSlotTransferPayload(i, ItemStack.EMPTY, container.getBlockEntity().getBlockPos())
          );
        }
      }
    }
  }
}