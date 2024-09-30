package weather2.mixin.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weather2.ClientTickHandler;
import weather2.WeatherItems;
import weather2.item.WeatherItem;
import weather2.weathersystem.storm.WeatherObject;

import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static weather2.util.WeatherUtilRender.renderRadar;

@Mixin(ItemInHandRenderer.class)
public abstract class GameRendererOverride {
    private float xOff = 0;

    Random random = new Random();

    private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));

    @Shadow
    private void renderPlayerArm(PoseStack p_109347_, MultiBufferSource p_109348_, int p_109349_, float p_109350_, float p_109351_, HumanoidArm p_109352_) {
    }

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    private void renderTwoHandedMap(PoseStack p_109340_, MultiBufferSource p_109341_, int p_109342_, float p_109343_, float p_109344_, float p_109345_) {
    }

    @Shadow
    private void renderOneHandedMap(PoseStack p_109354_, MultiBufferSource p_109355_, int p_109356_, float p_109357_, HumanoidArm p_109358_, float p_109359_, ItemStack p_109360_) {
    }

    @Shadow
    private void applyItemArmTransform(PoseStack p_109383_, HumanoidArm p_109384_, float p_109385_) {
    }

    @Shadow
    private void applyItemArmAttackTransform(PoseStack p_109336_, HumanoidArm p_109337_, float p_109338_) {
    }

    @Shadow
    public void renderItem(LivingEntity p_270072_, ItemStack p_270793_, ItemDisplayContext p_270837_, boolean p_270203_, PoseStack p_270974_, MultiBufferSource p_270686_, int p_270103_) {
    }

    @Shadow
    private void applyEatTransform(PoseStack p_109331_, float p_109332_, HumanoidArm p_109333_, ItemStack p_109334_) {
    }

    @Shadow
    private void applyBrushTransform(PoseStack p_273513_, float p_273245_, HumanoidArm p_273726_, ItemStack p_272809_, float p_273333_) {
    }


    @Inject(method = "renderArmWithItem", cancellable = true, at = @At("HEAD"))
    void onRenderArmWithItem(AbstractClientPlayer p_109372_, float p_109373_, float p_109374_, InteractionHand p_109375_, float p_109376_, ItemStack p_109377_, float p_109378_, PoseStack p_109379_, MultiBufferSource p_109380_, int p_109381_, CallbackInfo ci) {
        ci.cancel();
        if (!p_109372_.isScoping()) {
            boolean flag = p_109375_ == net.minecraft.world.InteractionHand.MAIN_HAND;
            HumanoidArm humanoidarm = flag ? p_109372_.getMainArm() : p_109372_.getMainArm().getOpposite();
            p_109379_.pushPose();
            if (p_109377_.isEmpty()) {
                if (flag && !p_109372_.isInvisible()) {
                    this.renderPlayerArm(p_109379_, p_109380_, p_109381_, p_109378_, p_109376_, humanoidarm);
                }
            } else if (p_109377_.is(Items.FILLED_MAP) || p_109377_.is(WeatherItems.RADAR_MAP.get())) {
                if (flag && this.offHandItem.isEmpty()) {
                    this.renderTwoHandedMap(p_109379_, p_109380_, p_109381_, p_109374_, p_109378_, p_109376_);
                } else {
                    this.renderOneHandedMap(p_109379_, p_109380_, p_109381_, p_109378_, humanoidarm, p_109376_, p_109377_);
                }
            } else if (p_109377_.getItem() instanceof CrossbowItem) {
                boolean flag1 = CrossbowItem.isCharged(p_109377_);
                boolean flag2 = humanoidarm == HumanoidArm.RIGHT;
                int i = flag2 ? 1 : -1;
                if (p_109372_.isUsingItem() && p_109372_.getUseItemRemainingTicks() > 0 && p_109372_.getUsedItemHand() == p_109375_) {
                    this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                    p_109379_.translate((float) i * -0.4785682F, -0.094387F, 0.05731531F);
                    p_109379_.mulPose(Axis.XP.rotationDegrees(-11.935F));
                    p_109379_.mulPose(Axis.YP.rotationDegrees((float) i * 65.3F));
                    p_109379_.mulPose(Axis.ZP.rotationDegrees((float) i * -9.785F));
                    float f9 = (float) p_109377_.getUseDuration() - ((float) Minecraft.getInstance().player.getUseItemRemainingTicks() - p_109373_ + 1.0F);
                    float f13 = f9 / (float) CrossbowItem.getChargeDuration(p_109377_);
                    if (f13 > 1.0F) {
                        f13 = 1.0F;
                    }

                    if (f13 > 0.1F) {
                        float f16 = Mth.sin((f9 - 0.1F) * 1.3F);
                        float f3 = f13 - 0.1F;
                        float f4 = f16 * f3;
                        p_109379_.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
                    }

                    p_109379_.translate(f13 * 0.0F, f13 * 0.0F, f13 * 0.04F);
                    p_109379_.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
                    p_109379_.mulPose(Axis.YN.rotationDegrees((float) i * 45.0F));
                } else {
                    float f = -0.4F * Mth.sin(Mth.sqrt(p_109376_) * (float) Math.PI);
                    float f1 = 0.2F * Mth.sin(Mth.sqrt(p_109376_) * ((float) Math.PI * 2F));
                    float f2 = -0.2F * Mth.sin(p_109376_ * (float) Math.PI);
                    p_109379_.translate((float) i * f, f1, f2);
                    this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                    this.applyItemArmAttackTransform(p_109379_, humanoidarm, p_109376_);
                    if (flag1 && p_109376_ < 0.001F && flag) {
                        p_109379_.translate((float) i * -0.641864F, 0.0F, 0.0F);
                        p_109379_.mulPose(Axis.YP.rotationDegrees((float) i * 10.0F));
                    }
                }

                this.renderItem(p_109372_, p_109377_, flag2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag2, p_109379_, p_109380_, p_109381_);
            } else {
                boolean flag3 = humanoidarm == HumanoidArm.RIGHT;
                if (!net.minecraftforge.client.extensions.common.IClientItemExtensions.of(p_109377_).applyForgeHandTransform(p_109379_, Minecraft.getInstance().player, humanoidarm, p_109377_, p_109373_, p_109378_, p_109376_)) // FORGE: Allow items to define custom arm animation
                    if (p_109372_.isUsingItem() && p_109372_.getUseItemRemainingTicks() > 0 && p_109372_.getUsedItemHand() == p_109375_) {
                        int k = flag3 ? 1 : -1;
                        switch (p_109377_.getUseAnimation()) {
                            case NONE:
                                this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                                break;
                            case EAT:
                            case DRINK:
                                this.applyEatTransform(p_109379_, p_109373_, humanoidarm, p_109377_);
                                this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                                break;
                            case BLOCK:
                                this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                                break;
                            case BOW:
                                this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                                p_109379_.translate((float) k * -0.2785682F, 0.18344387F, 0.15731531F);
                                p_109379_.mulPose(Axis.XP.rotationDegrees(-13.935F));
                                p_109379_.mulPose(Axis.YP.rotationDegrees((float) k * 35.3F));
                                p_109379_.mulPose(Axis.ZP.rotationDegrees((float) k * -9.785F));
                                float f8 = (float) p_109377_.getUseDuration() - ((float) Minecraft.getInstance().player.getUseItemRemainingTicks() - p_109373_ + 1.0F);
                                float f12 = f8 / 20.0F;
                                f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
                                if (f12 > 1.0F) {
                                    f12 = 1.0F;
                                }

                                if (f12 > 0.1F) {
                                    float f15 = Mth.sin((f8 - 0.1F) * 1.3F);
                                    float f18 = f12 - 0.1F;
                                    float f20 = f15 * f18;
                                    p_109379_.translate(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);
                                }

                                p_109379_.translate(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
                                p_109379_.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
                                p_109379_.mulPose(Axis.YN.rotationDegrees((float) k * 45.0F));
                                break;
                            case SPEAR:
                                this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                                p_109379_.translate((float) k * -0.5F, 0.7F, 0.1F);
                                p_109379_.mulPose(Axis.XP.rotationDegrees(-55.0F));
                                p_109379_.mulPose(Axis.YP.rotationDegrees((float) k * 35.3F));
                                p_109379_.mulPose(Axis.ZP.rotationDegrees((float) k * -9.785F));
                                float f7 = (float) p_109377_.getUseDuration() - ((float) Minecraft.getInstance().player.getUseItemRemainingTicks() - p_109373_ + 1.0F);
                                float f11 = f7 / 10.0F;
                                if (f11 > 1.0F) {
                                    f11 = 1.0F;
                                }

                                if (f11 > 0.1F) {
                                    float f14 = Mth.sin((f7 - 0.1F) * 1.3F);
                                    float f17 = f11 - 0.1F;
                                    float f19 = f14 * f17;
                                    p_109379_.translate(f19 * 0.0F, f19 * 0.004F, f19 * 0.0F);
                                }

                                p_109379_.translate(0.0F, 0.0F, f11 * 0.2F);
                                p_109379_.scale(1.0F, 1.0F, 1.0F + f11 * 0.2F);
                                p_109379_.mulPose(Axis.YN.rotationDegrees((float) k * 45.0F));
                                break;
                            case BRUSH:
                                this.applyBrushTransform(p_109379_, p_109373_, humanoidarm, p_109377_, p_109378_);
                        }
                    } else if (p_109372_.isAutoSpinAttack()) {
                        this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                        int j = flag3 ? 1 : -1;
                        p_109379_.translate((float) j * -0.4F, 0.8F, 0.3F);
                        p_109379_.mulPose(Axis.YP.rotationDegrees((float) j * 65.0F));
                        p_109379_.mulPose(Axis.ZP.rotationDegrees((float) j * -85.0F));
                    } else {
                        float f5 = -0.4F * Mth.sin(Mth.sqrt(p_109376_) * (float) Math.PI);
                        float f6 = 0.2F * Mth.sin(Mth.sqrt(p_109376_) * ((float) Math.PI * 2F));
                        float f10 = -0.2F * Mth.sin(p_109376_ * (float) Math.PI);
                        int l = flag3 ? 1 : -1;
                        p_109379_.translate((float) l * f5, f6, f10);
                        this.applyItemArmTransform(p_109379_, humanoidarm, p_109378_);
                        this.applyItemArmAttackTransform(p_109379_, humanoidarm, p_109376_);
                    }

                this.renderItem(p_109372_, p_109377_, flag3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag3, p_109379_, p_109380_, p_109381_);
            }

            p_109379_.popPose();
        }
    }

    @Inject(method = "renderMap", cancellable = true, at = @At("HEAD"))
    void onRenderMap(PoseStack p_109367_, MultiBufferSource p_109368_, int p_109369_, ItemStack p_109370_, CallbackInfo ci) {
        ci.cancel();
        p_109367_.mulPose(Axis.YP.rotationDegrees(180.0F));
        p_109367_.mulPose(Axis.ZP.rotationDegrees(180.0F));
        p_109367_.scale(0.38F, 0.38F, 0.38F);
        p_109367_.translate(-0.5F, -0.5F, 0.0F);
        p_109367_.scale(0.0078125F, 0.0078125F, 0.0078125F);
        Integer integer = MapItem.getMapId(p_109370_);
        MapItemSavedData mapitemsaveddata = MapItem.getSavedData(integer, Minecraft.getInstance().level);
        VertexConsumer vertexconsumer = p_109368_.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = p_109367_.last().pose();
        vertexconsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(p_109369_).endVertex();
        vertexconsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(p_109369_).endVertex();
        vertexconsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(p_109369_).endVertex();
        vertexconsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(p_109369_).endVertex();
        if (mapitemsaveddata != null) {
            Minecraft.getInstance().gameRenderer.getMapRenderer().render(p_109367_, p_109368_, integer, mapitemsaveddata, false, p_109369_);
        }

        if (p_109370_.is(WeatherItems.RADAR_MAP.get())) {
            renderRadar(p_109367_, p_109368_, p_109369_, p_109370_, ci, matrix4f);
        }

    }
}