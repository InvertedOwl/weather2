package weather2.weathersystem.tornado;

import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.ParticleTexFX;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import weather2.weathersystem.tornado.simple.TornadoFunnelSimple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TornadoManager {

    private Class lastScreenClass = null;

    private ParticleTexFX particleTest = null;
    private List<ParticleTexFX> particles = new ArrayList<>();

    private TornadoFunnel funnel;

    private TornadoFunnelSimple funnelSimple;

    //public CubicBezierCurve bezierCurve;
    public List<CubicBezierCurve> curves = new ArrayList<>();

    public Vector3f[] vecSpeeds = new Vector3f[10];

    public void tick(Level world) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (mc.level.getGameTime() % 1 == 0) {
            for (Player playerEntity : mc.level.players()) {
                if (true || mc.player.distanceTo(playerEntity) < 20) {

                    int particleCountCircle = 20;
                    int particleCountLayers = 40;

                    while (particles.size() < particleCountCircle * particleCountLayers) {
                        particleTest = new ParticleTexFX(mc.level, playerEntity.getX(), playerEntity.getY() + 2.2, playerEntity.getZ(), 0, 0, 0, ParticleRegistry.square16);
                        particleTest.setMaxAge(250);
                        particleTest.setScale(0.2F);
                        particleTest.setColor(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                        if (particles.size() < particleCountCircle * 5) {
                            particleTest.setColor(1, 1, 1);
                        }
                        float randGrey = 0.4F + (world.random.nextFloat() * 0.4F);
                        particleTest.setColor(randGrey, randGrey, randGrey);
                        mc.particleEngine.add(particleTest);
                        particles.add(particleTest);
                    }

                    int testY = 100;

                    Vector3f pos1 = new Vector3f(0.5F, 70, 0.5F);
                    Vector3f pos2 = new Vector3f(0.5F, 120, 0.5F);

                    float dist = getDistance(pos1, pos2);
                    Vector3f vecDiff = new Vector3f(
                            (pos1.x() - pos2.x()) / dist,
                            (pos1.y() - pos2.y()) / dist,
                            (pos1.z() - pos2.z()) / dist);
                    Vector3f vecAngles = new Vector3f(
                            (float)Math.atan2(vecDiff.y(), vecDiff.z()),
                            (float)Math.atan2(vecDiff.z(), vecDiff.x()), //invert if needed
                            (float)Math.atan2(vecDiff.x(), vecDiff.y())); //invert if needed

                    //convert to degrees
                    vecAngles = new Vector3f((float)Math.toDegrees(vecAngles.x()), (float)Math.toDegrees(vecAngles.y()), (float)Math.toDegrees(vecAngles.z()));

                    double xx = pos1.x() - pos2.x();
                    double zz = pos1.z() - pos2.z();
                    double xzDist = Math.sqrt(xx * xx + zz * zz);
                    float pitchAngle = (float)Math.toDegrees(Math.atan2(vecDiff.y(), xzDist / dist));

                    pitchAngle += 90;

                    while (curves.size() < 2) {
                        Vector3f[] vecs = new Vector3f[10];
                        for (int i = 0; i < vecs.length; i++) {
                            vecs[i] = new Vector3f(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                        }
                        curves.add(new CubicBezierCurve(vecs));
                    }

                    CubicBezierCurve bezierCurve = curves.get(0);

                    if (bezierCurve != null && true) {
                        float randScale = 0.1F;
                        for (int i = 0; i < bezierCurve.P.length; i++) {
                            if (vecSpeeds[i] == null) {
                                vecSpeeds[i] = new Vector3f(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                            }

                            bezierCurve.P[i].add(vecSpeeds[i].x() * 0.01F, vecSpeeds[i].y() * 0.01F, vecSpeeds[i].z() * 0.01F);

                            float maxY = 1F;
                            float minY = 0F;


                            //maxY += 2;
                            float minXZ = 0;
                            float maxXZ = 1;

                            float randSpeed = 1.5F;

                            if (bezierCurve.P[i].x() > maxXZ) {
                                vecSpeeds[i].set(world.random.nextFloat() * -1 * randSpeed, vecSpeeds[i].y(), vecSpeeds[i].z());
                            } else if (bezierCurve.P[i].x() < minXZ) {
                                vecSpeeds[i].set(world.random.nextFloat() * randSpeed, vecSpeeds[i].y(), vecSpeeds[i].z());
                            }
                            if (bezierCurve.P[i].y() > maxY) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), world.random.nextFloat() * -1 * randSpeed, vecSpeeds[i].z());
                            } else if (bezierCurve.P[i].y() < minY) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), world.random.nextFloat() * randSpeed, vecSpeeds[i].z());
                            }
                            if (bezierCurve.P[i].z() > maxXZ) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), vecSpeeds[i].y(), world.random.nextFloat() * -1 * randSpeed);
                            } else if (bezierCurve.P[i].z() < minXZ) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), vecSpeeds[i].y(), world.random.nextFloat() * randSpeed);
                            }
                        }
                    }

                    Iterator<ParticleTexFX> it = particles.iterator();
                    int index = 0;

                    float adjustedCurvePos = 0;

                    while (it.hasNext()) {
                        ParticleTexFX particle = it.next();
                        if (!particle.isAlive()) {
                            it.remove();
                        } else {
                            float x = 0;
                            float y2 = ((world.getGameTime() * 2) % 360) + ((index % particleCountCircle) * (360 / particleCountCircle));
                            float y = ((index % particleCountCircle) * (360 / particleCountCircle));
                            float z = 0;

                            y = vecAngles.y() - 90;

                            int yDiff = (index / particleCountCircle) - (particleCountLayers / 2);
                            float yDiffDist = 0.01F;

                            int curLayer = (index / particleCountCircle);
                            float curvePoint = (float)curLayer / (float)particleCountLayers * 1F;
                            float curvePoint2 = (float)Math.min(1D, (float)(curLayer+1) / (float)particleCountLayers) * 1F;
                            float stretchCurveY = 4F;
                            float curveAmp = 2F;
                            y2 = ((world.getGameTime() * (7 + (particleCountLayers - curLayer) * (particleCountLayers - curLayer) * 0.02F)) % 360) + ((index % particleCountCircle) * (360 / particleCountCircle));
                            float distFinal = dist / 2F;

                            Vector3f vecCurve1 = getCurveValue(curvePoint);
                            Vector3f vecCurve2 = getCurveValue(curvePoint2);

                            Vec2 curvePointYawPitch = yawPitch(vecCurve2, vecCurve1);
                            float curveDist = getDistance(vecCurve1, vecCurve2);

                            if ((index % particleCountCircle) == 0) {
                                adjustedCurvePos += curveDist;
                            }

                            Quaternionf quaternionY = new Quaternionf(0.0F, 1.0F, 0.0F, Math.toRadians(-curvePointYawPitch.x - 90));
                            Quaternionf quaternionYCircle = new Quaternionf(0.0F, 1.0F, 0.0F, Math.toRadians(-y2 + (curvePointYawPitch.x - 90)));

                            Quaternionf quatPitch = new Quaternionf(1.0F, 0.0F, 0.0F, Math.toRadians(curvePointYawPitch.y));
                            Vector3f vecCurve = getCurveValue(curvePoint);
                            Vector3f vecNew = new Vector3f(1/* + (curLayer * 0.05F)*/, 0F, 0);

                            float rotAroundPosX = 0;
                            float rotAroundPosY = 0;
                            float rotAroundPosZ = 0;
                            Matrix3f matrix = new Matrix3f();
                            matrix.rotation(quaternionY);
                            matrix.rotation(quatPitch);
                            matrix.rotation(quaternionYCircle);
                            vecNew.mulTranspose(matrix);

                            rotAroundPosX = vecNew.x();
                            rotAroundPosY = vecNew.y();
                            rotAroundPosZ = vecNew.z();

                            float tiltAdj = 1F;
                            particle.setPosition(pos1.x() + (vecCurve1.x()*distFinal) + rotAroundPosX, pos1.y() + (vecCurve1.y()*distFinal) + (rotAroundPosY * (tiltAdj)), pos1.z() + (vecCurve1.z()*distFinal) + rotAroundPosZ);
                            particle.setMotionX(0);
                            particle.setMotionY(0);
                            particle.setMotionZ(0);
                        }
                        index++;
                    }
                }
            }
        }

        if (funnel == null) {
            funnel = new TornadoFunnel();
            funnel.pos = new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }
    }

    public float getDistance(Vector3f vec1, Vector3f vec2) {
        float f = (vec1.x() - vec2.x());
        float f1 = (vec1.y() - vec2.y());
        float f2 = (vec1.z() - vec2.z());
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    /**
     *
     * @param pos2
     * @param pos1
     * @return yaw and pitch in degrees
     */
    public Vec2 yawPitch(Vector3f pos2, Vector3f pos1) {
        float dist = getDistance(pos1, pos2);
        Vector3f vecDiff = new Vector3f(
                (pos1.x() - pos2.x()) / dist,
                (pos1.y() - pos2.y()) / dist,
                (pos1.z() - pos2.z()) / dist);
        Vector3f vecAngles = new Vector3f(
                (float)Math.atan2(vecDiff.y(), vecDiff.z()),
                (float)Math.atan2(vecDiff.z(), vecDiff.x()), //invert if needed
                (float)Math.atan2(vecDiff.x(), vecDiff.y())); //invert if needed

        double xx = pos1.x() - pos2.x();
        double zz = pos1.z() - pos2.z();
        double xzDist = Math.sqrt(xx * xx + zz * zz);
        double wat = xzDist / dist;
        float pitchAngle = (float)Math.toDegrees(Math.atan2(vecDiff.y(), xzDist / dist));

        vecAngles = new Vector3f((float)Math.toDegrees(vecAngles.x()), (float)Math.toDegrees(vecAngles.y()), (float)Math.toDegrees(vecAngles.z()));

        pitchAngle += 90;

        return new Vec2(vecAngles.y(), pitchAngle);
    }

    public Vector3f getCurveValue(float val) {
        int arrayEntry = (int)Math.floor(val);
        if (arrayEntry > curves.size()-1) {
            System.out.println("out of bounds on curve lookup, val: " + val + " curves: - " + curves.size());
            return new Vector3f(1F, 1F, 1F);
        }
        CubicBezierCurve curve = curves.get(arrayEntry);
        return curve.getValue(val % 1F);
    }

}
