package net.roboxgamer.tutorialmod.block.entity.custom.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
  
  public MechanicalCrafterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}
  
  private static class HologramBufferSource implements MultiBufferSource {
    private final BufferSource bufferSource;
    
    public HologramBufferSource(MultiBufferSource.BufferSource bufferSource) {
      this.bufferSource = bufferSource;
    }
    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
      return this.bufferSource.getBuffer(RenderType.translucent());
    }
    
    public void endBatch() {
      this.bufferSource.endBatch();
    }
  }
  @Override
  public void render(@NotNull MechanicalCrafterBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    ItemStack renderStack = blockEntity.getRenderStack();
    renderStack = ItemStack.EMPTY;
    poseStack.pushPose();
    poseStack.translate(0.5f, 1.25f, 0.5f);
    poseStack.scale(0.5f, 0.5f, 0.5f);
    MultiBufferSource HologramBufferSource;
    if (bufferSource instanceof MultiBufferSource.BufferSource) {
      HologramBufferSource = new HologramBufferSource((MultiBufferSource.BufferSource) bufferSource);
    }
    else {
      HologramBufferSource = bufferSource;
    }
    RenderSystem.setShaderColor(0.6f, 0.6f, 1f, 0.85f);
    itemRenderer.renderStatic(
        renderStack,
        ItemDisplayContext.FIXED,
        getLightLevel(blockEntity.getLevel(), blockEntity.getBlockPos().above()),
        OverlayTexture.NO_OVERLAY,
        poseStack,
        HologramBufferSource,
        blockEntity.getLevel(),
        1
    );
    if (HologramBufferSource instanceof HologramBufferSource) {
      ((HologramBufferSource) HologramBufferSource).endBatch();
    }
    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    poseStack.popPose();
  }
  
  private int getLightLevel(Level level, BlockPos pos) {
    if (level == null) return 0;
    int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
    int skyLight = level.getBrightness(LightLayer.SKY, pos);
    return LightTexture.pack(skyLight, blockLight);
  }
}
