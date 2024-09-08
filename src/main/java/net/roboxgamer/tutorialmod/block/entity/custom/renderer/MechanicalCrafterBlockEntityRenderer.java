package net.roboxgamer.tutorialmod.block.entity.custom.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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
import net.roboxgamer.tutorialmod.block.entity.custom.MechanicalCrafterBlockEntity;
import org.jetbrains.annotations.NotNull;

public class MechanicalCrafterBlockEntityRenderer implements BlockEntityRenderer<MechanicalCrafterBlockEntity> {
  public MechanicalCrafterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
  
  }
  
  @Override
  public void render(@NotNull MechanicalCrafterBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
      ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
      ItemStack renderStack = blockEntity.getRenderStack();
      if (renderStack.isEmpty()) return;
      
      poseStack.pushPose();
      poseStack.translate(0.5f, 1.25f, 0.5f);
      poseStack.scale(0.5f, 0.5f, 0.5f);
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
  
  private int getLightLevel(Level level, BlockPos pos) {
    int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
    int skyLight = level.getBrightness(LightLayer.SKY, pos);
    return LightTexture.pack(skyLight, blockLight);
  }
}
