package net.roboxgamer.modernutils.block.entity.custom.renderer;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.roboxgamer.modernutils.block.entity.custom.MechanicalCrafterBlockEntity;
import org.jetbrains.annotations.NotNull;

public class MechanicalCrafterBlockEntityRenderer implements BlockEntityRenderer<MechanicalCrafterBlockEntity> {
  float rotation = 0f;
  CustomItemRenderer customItemRenderer;
  public MechanicalCrafterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}
  
  @Override
  public void render(@NotNull MechanicalCrafterBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    ItemStack renderStack = blockEntity.getRenderStack();
    poseStack.pushPose();
    poseStack.translate(0.5f, 1.25f, 0.5f);
    poseStack.scale(0.5f, 0.5f, 0.5f);
    poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
    rotation += .5f;
    if (rotation > 360) rotation = 0;
    itemRenderer.renderStatic(
        renderStack,
        ItemDisplayContext.FIXED,
        getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos().above()),
        OverlayTexture.NO_OVERLAY,
        poseStack,
        bufferSource,
        blockEntity.getLevel(),
        1
    );
    poseStack.popPose();
  }
  
  private ItemRenderer getCustomItemRenderer() {
    Minecraft minecraft = Minecraft.getInstance();
    BlockEntityWithoutLevelRenderer blockentitywithoutlevelrenderer = new BlockEntityWithoutLevelRenderer(
        Minecraft.getInstance().getBlockEntityRenderDispatcher(),
        minecraft.getEntityModels()
    );
    if (customItemRenderer == null) {
      customItemRenderer = new CustomItemRenderer(minecraft, minecraft.getTextureManager(), minecraft.getModelManager(), minecraft.getItemColors(), blockentitywithoutlevelrenderer);
    }
    return customItemRenderer;
  }
  
  private int getLightLevel(Level level, BlockPos pos) {
    if (level == null) return 0;
    int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
    int skyLight = level.getBrightness(LightLayer.SKY, pos);
    return LightTexture.pack(skyLight, blockLight);
  }
}
