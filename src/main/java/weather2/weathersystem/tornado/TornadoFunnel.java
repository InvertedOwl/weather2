package weather2.weathersystem.tornado;

import com.corosus.coroutil.util.CoroUtilBlock;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.ParticleTexFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

/**
 * To contain the full funnel, with each component piece
 */
public class TornadoFunnel {

    public Vector3d pos = new Vector3d(0, 0, 0);

    public LinkedList<FunnelPiece> listFunnel = new LinkedList();

    //temp?

    public int amountPerLayer = 30;
    public int particleCount = amountPerLayer * 50;
    public int funnelPieces = 2;

    CubicBezierCurve bezierCurve;

    static class FunnelPiece {

        public List<ParticleTexFX> listParticles = new ArrayList<>();

        public Vector3d posStart = new Vector3d(0, 0, 0);
        public Vector3d posEnd = new Vector3d(0, 20, 0);

        public float vecDirX = 0;
        public float vecDirZ = 0;

        public boolean needInit = true;

        CubicBezierCurve bezierCurve;

    }

    public TornadoFunnel() {

    }

    public void tickGame() {

        amountPerLayer = 30;
        particleCount = amountPerLayer * 50;
        funnelPieces = 10;



        tickGameTestCreate();
        tickUpdateFunnel();
    }

    private void tickGameTestCreate() {

        Player entP = Minecraft.getInstance().player;

        Random rand = new Random();


        while (listFunnel.size() < funnelPieces) {
            addPieceToEnd(new FunnelPiece());
        }

        for (int i = 0; i < listFunnel.size(); i++) {
            FunnelPiece piece = listFunnel.get(i);

            if (piece.needInit) {
                piece.needInit = false;

                int height = 10;
                //temp
                //TODO: LINK TO PREVIOUS OR NEXT PIECE IF THERE IS ONE
                if (i == 0) {
                    piece.posStart = new Vector3d(entP.getX(), entP.getY(), entP.getZ());
                    piece.posEnd = new Vector3d(entP.getX(), entP.getY() + height, entP.getZ());

                } else {
                    Vector3d prev = listFunnel.get(i-1).posEnd;
                    piece.posStart = new Vector3d(prev.x, prev.y, prev.z);
                    piece.posEnd = new Vector3d(piece.posStart.x, piece.posStart.y + height, piece.posStart.z);
                }

                if (i == funnelPieces - 1) {
                    piece.posEnd = new Vector3d(piece.posStart.x, piece.posStart.y + height, piece.posStart.z);
                }

                piece.vecDirX = rand.nextBoolean() ? 1 : -1;
                piece.vecDirZ = rand.nextBoolean() ? 1 : -1;
            }

            double dist = distanceTo(piece.posStart, piece.posEnd);

            double sizeXYParticle = 1;
            double funnelRadius = 3;

            double circumference = funnelRadius * 2D * Math.PI;

            amountPerLayer = (int) (circumference / sizeXYParticle);
            int layers = (int) (dist / sizeXYParticle);

            particleCount = layers * amountPerLayer;

            if (piece.bezierCurve == null || entP.level().getGameTime() % 40 == 0) {
                Vector3f[] vecs = new Vector3f[4];
                for (int ii = 0; ii < vecs.length; ii++) {
                    vecs[ii] = new Vector3f(entP.level().random.nextFloat(), entP.level().random.nextFloat(), entP.level().random.nextFloat());
                }
                piece.bezierCurve = new CubicBezierCurve(vecs);
            }

            if (bezierCurve == null || entP.level().getGameTime() % 40 == 0) {
                Vector3f[] vecs = new Vector3f[4];
                for (int ii = 0; ii < vecs.length; ii++) {
                    vecs[ii] = new Vector3f(entP.level().random.nextFloat(), entP.level().random.nextFloat(), entP.level().random.nextFloat());
                }
                bezierCurve = new CubicBezierCurve(vecs);
            }

            while (piece.listParticles.size() < particleCount) {
                BlockPos pos = CoroUtilBlock.blockPos(piece.posEnd.x, piece.posEnd.y, piece.posEnd.z);

                ClientLevel world = (ClientLevel)entP.level();

                ParticleTexFX particleTest = new ParticleTexFX(world, pos.getX() + rand.nextFloat(),
                        pos.getY(),
                        pos.getZ() + rand.nextFloat(), 0, 0, 0, ParticleRegistry.square16);

                particleTest.setMaxAge(250);
                particleTest.setParticleSpeed(0, 0, 0);
                particleTest.setScale(0.1F);
                particleTest.setColor(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                particleTest.setGravity(0);
                Minecraft.getInstance().particleEngine.add(particleTest);

                piece.listParticles.add(particleTest);
            }
        }
    }

    private void tickUpdateFunnel() {

        Level world = Minecraft.getInstance().level;
        Player player = Minecraft.getInstance().player;

        for (int ii = 0; ii < listFunnel.size(); ii++) {
            FunnelPiece piece = listFunnel.get(ii);

            double rate = 0.2F/* + (ii * 0.1F)*/;
            double distMax = 5 + (listFunnel.size() - ii);

            Random rand = new Random();

            piece.posEnd.add(new Vector3d(rate * piece.vecDirX, 0, rate * piece.vecDirZ * 0.7));

            int offset = 360 / listFunnel.size();
            long timeC = (world.getGameTime() * (ii+1) + (offset * ii)) * 1;
            float range = 35F;

            float speedAmp = 0.3F;

            double xx1 = piece.posEnd.x - piece.posStart.x;
            double zz1 = piece.posEnd.z - piece.posStart.z;
            double xzDist2 = (double) Mth.sqrt((float) (xx1 * xx1 + zz1 * zz1));

            if (xzDist2 > distMax) {
                if (piece.posEnd.x - piece.posStart.x > 0) {
                    piece.vecDirX = -1;
                    piece.vecDirX *= (0.5F + rand.nextFloat()) + (ii * speedAmp);
                }

                if (piece.posEnd.x - piece.posStart.x < 0) {
                    piece.vecDirX = 1;
                    piece.vecDirX *= (0.5F + rand.nextFloat()) + (ii * speedAmp);
                }

                if (piece.posEnd.z - piece.posStart.z > 0) {
                    piece.vecDirZ = -1;
                    piece.vecDirZ *= (0.5F + rand.nextFloat()) + (ii * speedAmp);
                }

                if (piece.posEnd.z - piece.posStart.z < 0) {
                    piece.vecDirZ = 1;
                    piece.vecDirZ *= (0.5F + rand.nextFloat()) + (ii * speedAmp);
                }
            }

            if (ii > 0) {
                Vector3d prev = listFunnel.get(ii-1).posEnd;
                piece.posStart = new Vector3d(prev.x, prev.y, prev.z);
            }

            double dist = distanceTo(piece.posStart, piece.posEnd);

            double x1 = piece.posEnd.x - piece.posStart.x;
            double y1 = piece.posEnd.y - piece.posStart.y;
            double z1 = piece.posEnd.z - piece.posStart.z;
            Vector3d vec = new Vector3d(x1 / dist, y1 / dist, z1 / dist);

            double sizeXYParticle = 1;
            double funnelRadius = 3;

            double circumference = funnelRadius * 2D * Math.PI;

            amountPerLayer = (int) (circumference / sizeXYParticle);
            int layers = (int) (dist / sizeXYParticle);

            particleCount = layers * amountPerLayer;

            Iterator<ParticleTexFX> it = piece.listParticles.iterator();
            int index = 0;
            while (it.hasNext()) {
                ParticleTexFX part = it.next();
                if (!part.isAlive()) {
                    it.remove();
                } else {

                    int particleCountCircle = 20;
                    int particleCountLayers = 40;

                    int yIndex = index / amountPerLayer;
                    int rotIndex = index % amountPerLayer;
                    int yCount = particleCount / amountPerLayer;

                    float x = 0;//((world.getGameTime() * 0.5F) % 360);
                    float y = /*((world.getGameTime() * 3) % 360) + */((index % particleCountCircle) * (360 / particleCountCircle));
                    float y2 = ((world.getGameTime() * 3) % 360) + ((index % particleCountCircle) * (360 / particleCountCircle));
                    float z = 0;//((world.getGameTime() * 0.3F) % 360);


                    int testY = 100;

                    float dist2 = (float)Math.sqrt(distanceTo(piece.posStart, piece.posEnd));

                    Vector3f vecDiff = new Vector3f(
                            (float)(piece.posStart.x - piece.posEnd.x) / dist2,
                            (float)(piece.posStart.y - piece.posEnd.y) / dist2,
                            (float)(piece.posStart.z - piece.posEnd.z) / dist2);
                    Vector3f vecAngles = new Vector3f(
                            (float)Math.atan2(vecDiff.y(), vecDiff.z()),
                            (float)Math.atan2(vecDiff.z(), vecDiff.x()), //invert if needed
                            (float)Math.atan2(vecDiff.x(), vecDiff.y())); //invert if needed

                    //convert to degrees
                    vecAngles = new Vector3f((float)Math.toDegrees(vecAngles.x()), (float)Math.toDegrees(vecAngles.y()), (float)Math.toDegrees(vecAngles.z()));

                    double xx = piece.posStart.x - piece.posEnd.x;
                    double zz = piece.posStart.z - piece.posEnd.z;
                    double xzDist = Math.sqrt(xx * xx + zz * zz);
                    float pitchAngle = (float)Math.toDegrees(Math.atan2(vecDiff.y(), xzDist / dist2));

                    pitchAngle += 90;
                    y = vecAngles.y() - 90;

                    double curvePoint = Math.min(1F, (float)(index / particleCountCircle) / (float)particleCountLayers);
                    double curvePoint2 = (index / particleCountCircle);
                    double yDiff = curvePoint2 * (dist / particleCountLayers)/* - (particleCountLayers / 2)*/;
                    float yDiffDist = 2F;
                    float curveAmp = 1F;

                    Quaternionf quaternionY = new Quaternionf(0.0F, 1.0F, 0.0F, Math.toRadians(-y));
                    Quaternionf quaternionYCircle = new Quaternionf(0.0F, 1.0F, 0.0F, Math.toRadians(-y2));

                    Quaternionf quatPitch = new Quaternionf(1.0F, 0.0F, 0.0F, Math.toRadians(-pitchAngle));
                    Vector3f vecCurve = piece.bezierCurve.getValue((float)curvePoint);
                    Vector3f vecNew = new Vector3f((float)vecCurve.x() * curveAmp, 1 + ((float)yDiff) * yDiffDist, (float)vecCurve.z() * curveAmp);

                    float rotAroundPosX = 0;
                    float rotAroundPosY = 0;
                    float rotAroundPosZ = 0;
                    Matrix3f matrix = new Matrix3f();
                    matrix.rotation(quaternionY);
                    matrix.rotation(quatPitch);
                    //multiply in the radial shape of the tornado
                    matrix.rotation(quaternionYCircle);
                    vecNew.mulTranspose(matrix);

                    rotAroundPosX = vecNew.x();
                    rotAroundPosY = vecNew.y();
                    rotAroundPosZ = vecNew.z();

                    part.setPosition(piece.posStart.x + rotAroundPosX, piece.posStart.y + rotAroundPosY, piece.posStart.z + rotAroundPosZ);
                }

                index++;
            }
        }
    }

    public void addPieceToEnd(FunnelPiece piece) {
        listFunnel.addLast(piece);
    }

    public double distanceTo(Vector3d vec1, Vector3d p_82555_) {
        double d0 = p_82555_.x - vec1.x;
        double d1 = p_82555_.y - vec1.y;
        double d2 = p_82555_.z - vec1.z;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

}
