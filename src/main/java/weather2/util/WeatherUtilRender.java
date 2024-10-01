package weather2.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weather2.ClientTickHandler;
import weather2.weathersystem.storm.CloudDefinition;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class WeatherUtilRender {
    public static int map(double value, double inMin, double inMax, double outMin, double outMax) {
        return (int) ((value - inMin) / (inMax - inMin) * (outMax - outMin) + outMin);
    }

    public static void renderRadar(PoseStack p_109367_, MultiBufferSource p_109368_, int p_109369_, ItemStack p_109370_, CallbackInfo ci, Matrix4f matrix4f) {
        try {
            List<WeatherObject> weatherObjects = ClientTickHandler.getClientWeather().getStormObjects();

            // Obtain the map's saved data
            Integer mapId = MapItem.getMapId(p_109370_);
            MapItemSavedData mapData = Minecraft.getInstance().level.getMapData(MapItem.makeKey(mapId));

            if (mapData == null) {
                return; // No map data, so we can't render anything
            }

            // Get the map's scaling and center position
            int scale = 1 << mapData.scale; // The scale factor (2^scale)
            double centerX = p_109370_.getOrCreateTag().getInt("centerX"); // X coordinate of the map's center in the world
            double centerY = p_109370_.getOrCreateTag().getInt("centerZ"); // Z coordinate of the map's center in the world

            for (WeatherObject weatherObject : weatherObjects) {
                for (CloudDefinition cloud : weatherObject.bounds) {
                    if (cloud.intensity < 20) {
                        continue;
                    }


                    Rectangle2D rectangle2D = cloud.bounds;
                    double realBlockX1 = rectangle2D.getMinX() + weatherObject.pos.x;
                    double realBlockY1 = rectangle2D.getMinY() + weatherObject.pos.z;
                    double realBlockX2 = rectangle2D.getMaxX() + weatherObject.pos.x;
                    double realBlockY2 = rectangle2D.getMaxY() + weatherObject.pos.z;

                    // Convert real block coordinates to map coordinates relative to the map center
                    double mapX1 = (realBlockX1 - centerX) / scale + 64.0; // Map is centered at (64,64) on the GUI
                    double mapY1 = (realBlockY1 - centerY) / scale + 64.0;
                    double mapX2 = (realBlockX2 - centerX) / scale + 64.0;
                    double mapY2 = (realBlockY2 - centerY) / scale + 64.0;

                    // Offset storms
                    float z = -0.1F - (weatherObjects.indexOf(weatherObject)*0.1f);
                    // Offset clouds
                    z -= weatherObject.bounds.indexOf(cloud) * 0.01f;



                    // Call renderRectangle to draw the rectangle with clipping
                    Color color = getRadarColor(map(cloud.intensity, 20, 100, 0, 100));
                    renderRectangle(p_109367_, p_109368_, p_109369_, matrix4f, (float)mapX1, (float)mapY1, (float)mapX2, (float)mapY2, z, color.getRed(), color.getGreen(), color.getBlue(), 150);

                }
                double xCenter = weatherObject.pos.x;
                double yCenter = weatherObject.pos.z;
                double realBlockX1 = xCenter - 5;
                double realBlockY1 = yCenter - 5;
                double realBlockX2 = xCenter + 5;
                double realBlockY2 = yCenter + 5;

                // Convert real block coordinates to map coordinates relative to the map center
                double mapX1 = (realBlockX1 - centerX) / scale + 64.0; // Map is centered at (64,64) on the GUI
                double mapY1 = (realBlockY1 - centerY) / scale + 64.0;
                double mapX2 = (realBlockX2 - centerX) / scale + 64.0;
                double mapY2 = (realBlockY2 - centerY) / scale + 64.0;

                // Offset storms
                float z = -0.1F - (weatherObjects.indexOf(weatherObject)*0.1f) - 0.1f;
                Color def = new Color(255, 255, 255, 255);

                if (weatherObject instanceof StormObject) {
                    if (((StormObject) weatherObject).isGrowing) {
                        def = new Color(255, 0, 0, 255);
                    }
                }
                renderRectangle(p_109367_, p_109368_, p_109369_, matrix4f, (float)mapX1, (float)mapY1, (float)mapX2, (float)mapY2, z, def.getRed(), def.getGreen(), def.getBlue(), def.getAlpha());

            }




        } catch (Exception e) {

        }
    }

    public static  void renderRectangle(PoseStack p_109367_, MultiBufferSource p_109368_, int p_109369_, Matrix4f matrix4f,
                                float x1, float y1, float x2, float y2, float z,
                                int red, int green, int blue, int alpha) {
        // Map boundaries for clipping
        float mapMinX = 0.0F;
        float mapMaxX = 128.0F;
        float mapMinY = 0.0F;
        float mapMaxY = 128.0F;

        // Clip the rectangle coordinates to the map boundaries
        float xMin = Math.max(x1, mapMinX);
        float xMax = Math.min(x2, mapMaxX);
        float yMin = Math.max(y1, mapMinY);
        float yMax = Math.min(y2, mapMaxY);

        // Check if there is an intersection (i.e., the rectangle is within the map boundaries)
        if (xMin < xMax && yMin < yMax) {
            // Use the same RenderType as the map background for consistency
            VertexConsumer rectangleVertexConsumer = p_109368_.getBuffer(RenderType.gui());

            // Draw the rectangle (clipped to map boundaries)
            rectangleVertexConsumer.vertex(matrix4f, xMin, yMax, z)
                    .color(red, green, blue, alpha)
                    .uv(0.0F, 1.0F)
                    .uv2(p_109369_)
                    .endVertex();
            rectangleVertexConsumer.vertex(matrix4f, xMax, yMax, z)
                    .color(red, green, blue, alpha)
                    .uv(1.0F, 1.0F)
                    .uv2(p_109369_)
                    .endVertex();
            rectangleVertexConsumer.vertex(matrix4f, xMax, yMin, z)
                    .color(red, green, blue, alpha)
                    .uv(1.0F, 0.0F)
                    .uv2(p_109369_)
                    .endVertex();
            rectangleVertexConsumer.vertex(matrix4f, xMin, yMin, z)
                    .color(red, green, blue, alpha)
                    .uv(0.0F, 0.0F)
                    .uv2(p_109369_)
                    .endVertex();
        }
    }

    public static Color getRadarColor(int intensity) {
        // Clamp intensity between 0 and 100
        intensity = Math.max(0, Math.min(100, intensity));

        // Transition from Green (0) -> Yellow (50) -> Red (100)
        if (intensity <= 50) {
            // Green to Yellow transition
            int red = (int)(255 * (intensity / 50.0));
            return new Color(red, 255, 0);  // RGB for green to yellow
        } else {
            // Yellow to Red transition
            int green = (int)(255 * ((100 - intensity) / 50.0));
            return new Color(255, green, 0);  // RGB for yellow to red
        }
    }
}
