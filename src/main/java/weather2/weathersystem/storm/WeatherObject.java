package weather2.weathersystem.storm;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import weather2.util.CachedNBTTagCompound;
import weather2.weathersystem.WeatherManager;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherObject {

	public static long lastUsedStormID = 0; //ID starts from 0 for each game start, no storm nbt disk reload for now
	public long ID; //loosely accurate ID for tracking, but we wanted to persist between world reloads..... need proper UUID??? I guess add in UUID later and dont persist, start from 0 per game run
	public boolean isDead = false;

	/**
	 * used to count up to a threshold to finally remove weather objects,
	 * solves issue of simbox cutoff removing storms for first few ticks as player is joining in singleplayer
	 * helps with multiplayer, requiring 30 seconds of no players near before removal
	 */
	public int ticksSinceNoNearPlayer = 0;
	
	public WeatherManager manager;
	
	public Vec3 pos = Vec3.ZERO;
	public Vec3 posGround = Vec3.ZERO;
	public Vec3 motion = Vec3.ZERO;
	private static final Random random = new Random();

	//used as radius
	public List<CloudDefinition> bounds = new ArrayList<>();

	//unused
	public EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.CLOUD;

	private CachedNBTTagCompound nbtCache;

	public WeatherObject(WeatherManager parManager) {
		manager = parManager;
		nbtCache = new CachedNBTTagCompound();

		int maxIntensity = 40;

		int currentIntensity = 10;
		while (currentIntensity < maxIntensity) {
			// Decreasing size for each consecutive intensity level
			int size = (maxIntensity/random.nextInt(currentIntensity)) * 10;

			// Offset by a small x and y, not usually enough to leave the last cloud, but I suppose its possible
			int x = -size/2 + (random.nextInt(size/2)-size/4);
			int y = -size/2 + (random.nextInt(size/2)-size/4);

			// even smaller offset it width and height. this is just to add a tiny bit of realism but it's not super necessaery
			int width = size + (random.nextInt(size/6)-size/12);
			int height = size + (random.nextInt(size/6)-size/12);

			bounds.add(new CloudDefinition(new Rectangle2D.Double(x, y, width, height), currentIntensity * 2));

			// Update current intensity but a small but non-zero random integer.
			currentIntensity += random.nextInt(5) + 5;
		}

	}
	
	public void initFirstTime() {
		ID = lastUsedStormID++;
	}
	
	public void tick() {
		
	}

	public Point2D generateRandomPointInRectangles() {
		List<CloudDefinition> clouds = bounds;
		if (clouds == null || clouds.isEmpty()) {
			return new Point2D.Double(0, 0);
		}

		List<Rectangle2D> rectangles = new ArrayList<>();
		for (CloudDefinition cloud : clouds) {
			rectangles.add(cloud.bounds);
		}

		// Compute the bounding box of all rectangles
		Rectangle2D boundingBox = getBoundingBox(rectangles);

		double minX = boundingBox.getMinX();
		double minY = boundingBox.getMinY();
		double maxX = boundingBox.getMaxX();
		double maxY = boundingBox.getMaxY();

		Point2D randomPoint;
		int maxAttempts = 10000; // To prevent infinite loops
		int attempts = 0;

		do {
			double x = minX + random.nextDouble() * (maxX - minX);
			double y = minY + random.nextDouble() * (maxY - minY);
			randomPoint = new Point2D.Double(x, y);
			attempts++;

			// If max attempts reached, throw an exception
			if (attempts >= maxAttempts) {
				throw new RuntimeException("Unable to generate a random point within the rectangles after "
						+ maxAttempts + " attempts.");
			}

		} while (!isPointInRectangles(randomPoint, rectangles));

		return randomPoint;
	}

	private static Rectangle2D getBoundingBox(List<Rectangle2D> rectangles) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (Rectangle2D rect : rectangles) {
			if (rect.getMinX() < minX) minX = rect.getMinX();
			if (rect.getMinY() < minY) minY = rect.getMinY();
			if (rect.getMaxX() > maxX) maxX = rect.getMaxX();
			if (rect.getMaxY() > maxY) maxY = rect.getMaxY();
		}

		return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
	}

	private static boolean isPointInRectangles(Point2D point, List<Rectangle2D> rectangles) {
		for (Rectangle2D rect : rectangles) {
			if (rect.contains(point)) {
				return true;
			}
		}
		return false;
	}

	private double signedDistanceToRect(double x, double y, Rectangle2D rect) {
		double minX = rect.getMinX() + pos.x();
		double minY = rect.getMinY() + pos.z();
		double maxX = rect.getMaxX() + pos.x();
		double maxY = rect.getMaxY() + pos.z();

		double dxOutside = Math.max(Math.max(minX - x, 0), x - maxX);
		double dyOutside = Math.max(Math.max(minY - y, 0), y - maxY);

		if (dxOutside > 0 || dyOutside > 0) {
			// Outside the rectangle
			return Math.hypot(dxOutside, dyOutside);
		} else {
			// Inside the rectangle
			double distXToEdges = Math.max(x - minX, maxX - x);
			double distYToEdges = Math.max(y - minY, maxY - y);
			double maxDistToEdge = Math.min(distXToEdges, distYToEdges);
			return -maxDistToEdge;
		}
	}
	public double distanceToEdge(Vec3 point) {
		double minDist = Double.POSITIVE_INFINITY;
		double x = point.x();
		double y = point.z();

		for (CloudDefinition cloud : bounds) {
			double dist = signedDistanceToRect(x, y, cloud.bounds);
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}

	@OnlyIn(Dist.CLIENT)
	public void tickRender(float partialTick) {

	}

	public void reset() {
		remove();
	}
	
	public void remove() {

		isDead = true;
		
		if (EffectiveSide.get().equals(LogicalSide.CLIENT)) {
			cleanupClient();
		}
		
		cleanup();
	}
	
	public void cleanup() {
		manager = null;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void cleanupClient() {
		
	}
	
	public int getUpdateRateForNetwork() {
		return 40;
	}
	
	public void read() {
		
    }
	
	public void write() {

    }
	
	public void nbtSyncFromServer() {
		CachedNBTTagCompound parNBT = this.getNbtCache();
		ID = parNBT.getLong("ID");

		pos = new Vec3(parNBT.getDouble("posX"), parNBT.getDouble("posY"), parNBT.getDouble("posZ"));
		motion = new Vec3(parNBT.getDouble("vecX"), parNBT.getDouble("vecY"), parNBT.getDouble("vecZ"));
		bounds = parNBT.getCloudDefinitionList("bounds");
		this.weatherObjectType = EnumWeatherObjectType.get(parNBT.getInt("weatherObjectType"));
	}
	
	public void nbtSyncForClient() {
		CachedNBTTagCompound nbt = this.getNbtCache();
		nbt.putDouble("posX", pos.x);
		nbt.putDouble("posY", pos.y);
		nbt.putDouble("posZ", pos.z);

		nbt.putDouble("vecX", motion.x);
		nbt.putDouble("vecY", motion.y);
		nbt.putDouble("vecZ", motion.z);

		nbt.putLong("ID", ID);
		//just blind set ID into non cached data so client always has it, no need to check for forced state and restore orig state
		nbt.getNewNBT().putLong("ID", ID);

		nbt.putCloudDefinitionList("bounds", bounds);
		nbt.putInt("weatherObjectType", this.weatherObjectType.ordinal());
	}

	public CachedNBTTagCompound getNbtCache() {
		return nbtCache;
	}

	public void setNbtCache(CachedNBTTagCompound nbtCache) {
		this.nbtCache = nbtCache;
	}

	public List<CloudDefinition> getCollider() {
		return bounds;
	}
	
}
