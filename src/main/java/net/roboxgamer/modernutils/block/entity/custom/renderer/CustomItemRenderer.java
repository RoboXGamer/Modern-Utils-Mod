package net.roboxgamer.modernutils.block.entity.custom.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.math.MatrixUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.jetbrains.annotations.NotNull;

public class CustomItemRenderer extends ItemRenderer {
  private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.inventory(
      ResourceLocation.withDefaultNamespace("trident"));
  private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.inventory(ResourceLocation.withDefaultNamespace("spyglass"));
  
  public CustomItemRenderer(Minecraft minecraft, TextureManager textureManager, ModelManager modelManager, ItemColors itemColors, BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer) {
    super(minecraft, textureManager, modelManager, itemColors, blockentitywithoutlevelrenderer);
  }
  
  @Override
  public void render(ItemStack itemStack, @NotNull ItemDisplayContext displayContext, boolean leftHand, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int combinedLight, int combinedOverlay, @NotNull BakedModel p_model) {
    if (!itemStack.isEmpty()) {
      poseStack.pushPose();
      boolean flag = displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED;
      if (flag) {
        if (itemStack.is(Items.TRIDENT)) {
          p_model = this.getItemModelShaper().getModelManager().getModel(TRIDENT_MODEL);
        } else if (itemStack.is(Items.SPYGLASS)) {
          p_model = this.getItemModelShaper().getModelManager().getModel(SPYGLASS_MODEL);
        }
      }
      
      p_model = net.neoforged.neoforge.client.ClientHooks.handleCameraTransforms(poseStack, p_model, displayContext, leftHand);
      poseStack.translate(-0.5F, -0.5F, -0.5F);
      if (!p_model.isCustomRenderer() && (!itemStack.is(Items.TRIDENT) || flag)) {
        boolean flag1;
        if (displayContext != ItemDisplayContext.GUI && !displayContext.firstPerson() && itemStack.getItem() instanceof BlockItem blockitem) {
          Block block = blockitem.getBlock();
          flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
        } else {
          flag1 = true;
        }
        
        for (var model : p_model.getRenderPasses(itemStack, flag1)) {
          for (var rendertype : model.getRenderTypes(itemStack, flag1)) {
            VertexConsumer vertexconsumer;
            if (hasAnimatedTexture(itemStack) && itemStack.hasFoil()) {
              PoseStack.Pose posestack$pose = poseStack.last().copy();
              if (displayContext == ItemDisplayContext.GUI) {
                MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.5F);
              } else if (displayContext.firstPerson()) {
                MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.75F);
              }
              
              vertexconsumer = getCompassFoilBuffer(bufferSource, rendertype, posestack$pose);
            } else if (flag1) {
              vertexconsumer = getFoilBufferDirect(bufferSource, rendertype, true, itemStack.hasFoil());
            } else {
              vertexconsumer = getFoilBuffer(bufferSource, rendertype, true, itemStack.hasFoil());
            }
            
            this.renderModelLists(model, itemStack, combinedLight, combinedOverlay, poseStack, vertexconsumer);
          }
        }
      } else {
        net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(itemStack).getCustomRenderer().renderByItem(itemStack, displayContext, poseStack, bufferSource, combinedLight, combinedOverlay);
      }
      
      poseStack.popPose();
    }
  }
  
  private static boolean hasAnimatedTexture(ItemStack stack) {
    return stack.is(ItemTags.COMPASSES) || stack.is(Items.CLOCK);
  }
  
  public static @NotNull VertexConsumer getFoilBuffer(MultiBufferSource bufferSource, @NotNull RenderType renderType, boolean isItem, boolean glint) {
    if (glint) {
      return Minecraft.useShaderTransparency() && renderType == Sheets.translucentItemSheet()
          ? VertexMultiConsumer.create(bufferSource.getBuffer(RenderType.glintTranslucent()), bufferSource.getBuffer(renderType))
          : VertexMultiConsumer.create(bufferSource.getBuffer(isItem ? RenderType.glint() : RenderType.entityGlint()), bufferSource.getBuffer(renderType));
    } else {
      return bufferSource.getBuffer(RenderType.translucent());
    }
  }
  
  public static @NotNull VertexConsumer getFoilBufferDirect(MultiBufferSource bufferSource, @NotNull RenderType renderType, boolean noEntity, boolean withGlint) {
    return withGlint
        ? VertexMultiConsumer.create(bufferSource.getBuffer(noEntity ? RenderType.glint() : RenderType.entityGlintDirect()), bufferSource.getBuffer(RenderType.translucent()))
        : bufferSource.getBuffer(RenderType.translucent());
  }
}
