package weather2.util;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import com.corosus.coroutil.util.CoroUtilEntOrParticle;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexFX;
import weather2.IWindHandler;

public class WeatherUtilParticle {
    public static Map<ParticleRenderType, Queue<Particle>> fxLayers;
    
    public static int effLeafID = 0;
    public static int effRainID = 1;
    public static int effWindID = 2;
    public static int effSnowID = 3;

    public static Random rand = new Random();

    
    
    //weather2: not sure what will happen to this in 1.7, copied over for convenience
    public static int getParticleAge(Particle ent)
    {
        return ent.age;
    }

    //weather2: not sure what will happen to this in 1.7, copied over for convenience
    public static void setParticleAge(Particle ent, int val)
    {
        ent.age = val;
    }

    @OnlyIn(Dist.CLIENT)
    public static void getFXLayers()
    {
        //fxLayers
        Field field = null;

        try
        {
            field = (ParticleEngine.class).getDeclaredField("particles");//ObfuscationReflectionHelper.remapFieldNames("net.minecraft.client.particle.EffectRenderer", new String[] { "fxLayers" })[0]);
            field.setAccessible(true);
            fxLayers = (Map<ParticleRenderType, Queue<Particle>>)field.get(Minecraft.getInstance().particleEngine);
        }
        catch (Exception ex)
        {
            try
            {
                field = (ParticleEngine.class).getDeclaredField("f_107289_");
                field.setAccessible(true);
                fxLayers = (Map<ParticleRenderType, Queue<Particle>>)field.get(Minecraft.getInstance().particleEngine);
            }
            catch (Exception ex2)
            {
                ex2.printStackTrace();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static float getParticleWeight(EntityRotFX entity1)
    {
        if (entity1 instanceof IWindHandler) {
            return ((IWindHandler) entity1).getWindWeight();
        }

        if (entity1 instanceof ParticleTexFX)
        {
            return 5.0F + ((float)entity1.getAge() / 200);
        }

        if (entity1 instanceof Particle)
        {
            return 5.0F + ((float)entity1.getAge() / 200);
        }

        return -1;
    }

    public static BlockPos getPos(Particle particle) {
        return new BlockPos(Mth.floor(particle.x), Mth.floor(particle.y), Mth.floor(Mth.floor(particle.z)));
    }
}
