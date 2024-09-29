package weather2.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;

public class RadarMapRenderer extends MapRenderer {
    public RadarMapRenderer(Minecraft minecraft) {
        super(Minecraft.getInstance().getTextureManager());
    }

    @Override
    public void render(PoseStack p_168772_, MultiBufferSource p_168773_, int p_168774_, MapItemSavedData p_168775_, boolean p_168776_, int p_168777_) {
        super.render(p_168772_, p_168773_, p_168774_, p_168775_, p_168776_, p_168777_);


        float squareSize = 16.0F; // Size of the square
        float centerX = 64.0F;    // Center X-coordinate of the map
        float centerY = 64.0F;    // Center Y-coordinate of the map
        float halfSize = squareSize / 2.0F;

        // Coordinates of the square
        float x1 = centerX - halfSize;
        float y1 = centerY - halfSize;
        float x2 = centerX + halfSize;
        float y2 = centerY + halfSize;
        float z = -0.1F; // Slightly above the map background

        // Color of the square (red)
        int red = 255;
        int green = 0;
        int blue = 0;
        int alpha = 128;

        // Get a vertex consumer for rendering solid colors
        Matrix4f matrix4f = p_168772_.last().pose();
        VertexConsumer squareVertexConsumer = p_168773_.getBuffer(RenderType.gui());

        // Draw the square with correct UV coordinates
        squareVertexConsumer.vertex(matrix4f, x1, y2, z)
                .color(red, green, blue, alpha)
                .uv(0.0F, 0.0F)
                .uv2(p_168774_)
                .endVertex();
        squareVertexConsumer.vertex(matrix4f, x2, y2, z)
                .color(red, green, blue, alpha)
                .uv(1.0F, 0.0F)
                .uv2(p_168774_)
                .endVertex();
        squareVertexConsumer.vertex(matrix4f, x2, y1, z)
                .color(red, green, blue, alpha)
                .uv(1.0F, 1.0F)
                .uv2(p_168774_)
                .endVertex();
        squareVertexConsumer.vertex(matrix4f, x1, y1, z)
                .color(red, green, blue, alpha)
                .uv(0.0F, 1.0F)
                .uv2(p_168774_)
                .endVertex();
    }
}
