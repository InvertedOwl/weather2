package weather2.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import weather2.ClientTickHandler;
import weather2.config.ConfigParticle;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class RenderParticlesOverride {

    @Redirect(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    public void renderSnowAndRain(LevelRenderer worldRenderer, LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn) {
        //stopping vanilla from running renderRainSnow
        if (ConfigParticle.Particle_vanilla_precipitation) {
            worldRenderer.renderSnowAndRain(lightmapIn, partialTicks, xIn, yIn, zIn);
        }
    }
}