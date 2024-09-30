package weather2.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weather2.ClientTickHandler;
import weather2.weathersystem.storm.WeatherObject;

import java.awt.geom.Rectangle2D;
import java.util.List;

import static weather2.util.WeatherUtilRender.renderRadar;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererOverride<T extends ItemFrame> {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)

    public void onRender(T itemFrame, float p_115077_, float p_115078_, PoseStack p_115079_, MultiBufferSource p_115080_, int p_115081_, CallbackInfo ci) {
//        renderRadar(p_115079_, p_115080_, p_115081_, itemFrame.getItem(), ci, p_115079_.last().pose());
    }
}
