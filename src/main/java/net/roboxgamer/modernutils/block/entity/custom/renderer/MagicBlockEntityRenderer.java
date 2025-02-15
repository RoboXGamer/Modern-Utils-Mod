package net.roboxgamer.modernutils.block.entity.custom.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.roboxgamer.modernutils.block.entity.custom.MagicBlockBlockEntity;

public class MagicBlockEntityRenderer implements BlockEntityRenderer<MagicBlockBlockEntity> {

  public MagicBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
  }

  @Override
    public void render(MagicBlockBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        int x = blockEntity.getOffsetX();
        int y = blockEntity.getOffsetY();
        int z = blockEntity.getOffsetZ();
        boolean showOutline = blockEntity.getRenderOutline();
        if (!showOutline) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        // Define the color (green with alpha)
        float red = 0.0F;
        float green = 1.0F;
        float blue = 0.2F;
        float alpha = 1.0F;

        // Render each edge of the cube
        // Bottom face
        renderLine(lines, poseStack, 0, 0, 0, 1, 0, 0, red, green, blue, alpha);
        renderLine(lines, poseStack, 1, 0, 0, 1, 0, 1, red, green, blue, alpha);
        renderLine(lines, poseStack, 1, 0, 1, 0, 0, 1, red, green, blue, alpha);
        renderLine(lines, poseStack, 0, 0, 1, 0, 0, 0, red, green, blue, alpha);

        // Top face
        renderLine(lines, poseStack, 0, 1, 0, 1, 1, 0, red, green, blue, alpha);
        renderLine(lines, poseStack, 1, 1, 0, 1, 1, 1, red, green, blue, alpha);
        renderLine(lines, poseStack, 1, 1, 1, 0, 1, 1, red, green, blue, alpha);
        renderLine(lines, poseStack, 0, 1, 1, 0, 1, 0, red, green, blue, alpha);

        // Vertical edges
        renderLine(lines, poseStack, 0, 0, 0, 0, 1, 0, red, green, blue, alpha);
        renderLine(lines, poseStack, 1, 0, 0, 1, 1, 0, red, green, blue, alpha);
        renderLine(lines, poseStack, 1, 0, 1, 1, 1, 1, red, green, blue, alpha);
        renderLine(lines, poseStack, 0, 0, 1, 0, 1, 1, red, green, blue, alpha);
        poseStack.popPose();
    }

  private void renderLine(VertexConsumer builder, PoseStack stack, float x1, float y1, float z1,
      float x2, float y2, float z2, float red, float green, float blue, float alpha) {
    Matrix4f pose = stack.last().pose();
    builder.addVertex(pose, x1, y1, z1)
        .setColor(red, green, blue, alpha)
        .setNormal(1.0F, 1.0F, 1.0F);
    builder.addVertex(pose, x2, y2, z2)
        .setColor(red, green, blue, alpha)
        .setNormal(1.0F, 1.0F, 1.0F);
  }
}
