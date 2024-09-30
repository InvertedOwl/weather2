package weather2.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import weather2.weathersystem.storm.CloudDefinition;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Caches nbt data to remove redundant data sending over network
 *
 * @author cosmicdan
 *
 * revisions made to further integrate it into the newer design of WeatherObjects
 */
public class CachedNBTTagCompound {
	private CompoundTag newData;
	private CompoundTag cachedData;
	private boolean forced = false;

	public CachedNBTTagCompound() {
		this.newData = new CompoundTag();
		this.cachedData = new CompoundTag();
	}

	public void setCachedNBT(CompoundTag cachedData) {
		if (cachedData == null)
			cachedData = new CompoundTag();
		this.cachedData = cachedData;
	}

	public CompoundTag getCachedNBT() {
		return cachedData;
	}

	public CompoundTag getNewNBT() {
		return newData;
	}

	public void setNewNBT(CompoundTag newData) {
		this.newData = newData;
	}

	public void setUpdateForced(boolean forced) {
		this.forced = forced;
	}

	// Rectangle2D-related methods

	/**
	 * Saves a list of Rectangle2D objects to the NBT.
	 */
	public void putRectangleList(String key, List<Rectangle2D> rectangles) {
		ListTag listTag = new ListTag();
		for (Rectangle2D rect : rectangles) {
			CompoundTag rectTag = new CompoundTag();
			rectTag.putDouble("x", rect.getX());
			rectTag.putDouble("y", rect.getY());
			rectTag.putDouble("width", rect.getWidth());
			rectTag.putDouble("height", rect.getHeight());
			listTag.add(rectTag);
		}
		newData.put(key, listTag);
		cachedData.put(key, listTag);
	}

	/**
	 * Reads a list of Rectangle2D objects from the NBT.
	 */
	public List<Rectangle2D> getRectangleList(String key) {
		List<Rectangle2D> rectangles = new ArrayList<>();
		if (!newData.contains(key)) {
			return rectangles; // Return empty list if key not found
		}

		ListTag listTag = newData.getList(key, Tag.TAG_COMPOUND);
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag rectTag = listTag.getCompound(i);
			double x = rectTag.getDouble("x");
			double y = rectTag.getDouble("y");
			double width = rectTag.getDouble("width");
			double height = rectTag.getDouble("height");
			rectangles.add(new Rectangle2D.Double(x, y, width, height));
		}

		return rectangles;
	}

	public long getLong(String key) {
		if (!newData.contains(key))
			newData.putLong(key, cachedData.getLong(key));
		return newData.getLong(key);
	}

	public void putLong(String key, long newVal) {
		if (!cachedData.contains(key) || cachedData.getLong(key) != newVal || forced) {
			newData.putLong(key, newVal);
		}
		cachedData.putLong(key, newVal);
	}

	public int getInt(String key) {
		if (!newData.contains(key))
			newData.putInt(key, cachedData.getInt(key));
		return newData.getInt(key);
	}

	public void putInt(String key, int newVal) {
		if (!cachedData.contains(key) || cachedData.getInt(key) != newVal || forced) {
			newData.putInt(key, newVal);
		}
		cachedData.putInt(key, newVal);
	}

	public short getShort(String key) {
		if (!newData.contains(key))
			newData.putShort(key, cachedData.getShort(key));
		return newData.getShort(key);
	}

	public void putShort(String key, short newVal) {
		if (!cachedData.contains(key) || cachedData.getShort(key) != newVal || forced) {
			newData.putShort(key, newVal);
		}
		cachedData.putShort(key, newVal);
	}

	public String getString(String key) {
		if (!newData.contains(key))
			newData.putString(key, cachedData.getString(key));
		return newData.getString(key);
	}

	public void putString(String key, String newVal) {
		if (!cachedData.contains(key) || !cachedData.getString(key).equals(newVal) || forced) {
			newData.putString(key, newVal);
		}
		cachedData.putString(key, newVal);
	}

	public boolean getBoolean(String key) {
		if (!newData.contains(key))
			newData.putBoolean(key, cachedData.getBoolean(key));
		return newData.getBoolean(key);
	}

	public void putBoolean(String key, boolean newVal) {
		if (!cachedData.contains(key) || cachedData.getBoolean(key) != newVal || forced) {
			newData.putBoolean(key, newVal);
		}
		cachedData.putBoolean(key, newVal);
	}

	public float getFloat(String key) {
		if (!newData.contains(key))
			newData.putFloat(key, cachedData.getFloat(key));
		return newData.getFloat(key);
	}

	public void putFloat(String key, float newVal) {
		if (!cachedData.contains(key) || cachedData.getFloat(key) != newVal || forced) {
			newData.putFloat(key, newVal);
		}
		cachedData.putFloat(key, newVal);
	}

	public double getDouble(String key) {
		if (!newData.contains(key))
			newData.putDouble(key, cachedData.getDouble(key));
		return newData.getDouble(key);
	}

	public void putDouble(String key, double newVal) {
		if (!cachedData.contains(key) || cachedData.getDouble(key) != newVal || forced) {
			newData.putDouble(key, newVal);
		}
		cachedData.putDouble(key, newVal);
	}

	public CompoundTag get(String key) {
		return newData.getCompound(key);
	}

	/** warning, not cached **/
	public void put(String key, CompoundTag tag) {
		newData.put(key, tag);
		cachedData.put(key, tag);
	}

	public boolean contains(String key) {
		return newData.contains(key);
	}

	public void updateCacheFromNew() {
		this.cachedData = this.newData;
	}

	/**
	 * Saves a list of CloudDefinition objects to the NBT.
	 */
	public void putCloudDefinitionList(String key, List<CloudDefinition> clouds) {
		ListTag listTag = new ListTag();
		for (CloudDefinition cloud : clouds) {
			CompoundTag cloudTag = new CompoundTag();

			// Save the Rectangle2D part
			cloudTag.putDouble("x", cloud.bounds.getX());
			cloudTag.putDouble("y", cloud.bounds.getY());
			cloudTag.putDouble("width", cloud.bounds.getWidth());
			cloudTag.putDouble("height", cloud.bounds.getHeight());

			// Save the intensity part
			cloudTag.putInt("intensity", cloud.intensity);

			listTag.add(cloudTag);
		}
		newData.put(key, listTag);
		cachedData.put(key, listTag);
	}

	/**
	 * Reads a list of CloudDefinition objects from the NBT.
	 */
	public List<CloudDefinition> getCloudDefinitionList(String key) {
		List<CloudDefinition> clouds = new ArrayList<>();
		if (!newData.contains(key)) {
			return clouds; // Return empty list if key not found
		}

		ListTag listTag = newData.getList(key, Tag.TAG_COMPOUND);
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag cloudTag = listTag.getCompound(i);

			// Read the Rectangle2D part
			double x = cloudTag.getDouble("x");
			double y = cloudTag.getDouble("y");
			double width = cloudTag.getDouble("width");
			double height = cloudTag.getDouble("height");
			Rectangle2D bounds = new Rectangle2D.Double(x, y, width, height);

			// Read the intensity part
			int intensity = cloudTag.getInt("intensity");

			clouds.add(new CloudDefinition(bounds, intensity));
		}

		return clouds;
	}
}
